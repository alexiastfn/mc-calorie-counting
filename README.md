# Calorie Counter (mc-calorie-counting)

Mobile Computing project, MSc PDCS, UPB, 2026–2027.

An **offline-first Android calorie counter**. You log meals by searching for a food, by scanning a
barcode in a photo, or by recognizing the food in a photo, and **every log works with no network**:
the network only refreshes food data and backs up the meal log.

**The systems decision:** where food recognition runs. Meal photos are personal data and logging
must work offline, so recognition runs **on-device by default**. A cloud model is only an **opt-in
fallback** for low-confidence results, and the confidence threshold is chosen from measured latency
and accuracy of both engines.

AI use is logged in [`AI_USE_LOG.md`](AI_USE_LOG.md).

**Goals**
- Logging a meal works fully offline.
- Food recognition from a gallery photo, with an explicit, measured on-device vs. cloud placement decision.
- Every item of the handout's technical floor is met.

**Non-goals:** UI polish and animations, medical-grade nutrition accuracy, diet plans, accounts with
passwords, social features.

## How the app works

| Screen | What it does |
|---|---|
| **Today** (start) | Today's meals grouped by breakfast / lunch / dinner / snack; total kcal vs. daily goal |
| **Search** | Search foods (cached locally, refreshed from Open Food Facts) |
| **FoodDetail** | Nutrition per 100 g, product image, grams input, "Log" |
| **MealDetail** | Edit grams / meal type, delete, sync status |
| **AddFromPhoto** | Pick a photo → barcode match or food recognition → confirm |
| **History** | Past days with totals; tap a day to open it |
| **Settings** | Daily goal, "allow cloud recognition", notification permission |

There are two list→detail flows: Search → FoodDetail and Today → MealDetail. Everything the user
types or selects survives process death. Calories are always derived (`kcal = grams × kcalPer100g / 100`),
never stored.

**Architecture:** Kotlin, Jetpack Compose + Material 3, MVVM with unidirectional data flow. ViewModels
expose `StateFlow<UiState>` and depend only on repository interfaces in `domain/`. Only the
repositories and workers touch Room, Retrofit, Firebase or ML Kit.

```
UI (Compose) ──events──▶ ViewModel ──▶ Repository interfaces (domain/)
     ▲                       │                 │
     └──── StateFlow ◀───────┘        Room (source of truth) ◀── WorkManager sync ──▶ remote
```

**External data:** [Open Food Facts](https://world.openfoodfacts.org) (read-only food data and images,
no API key, rate-limited: 10 searches/min and 15 barcode lookups/min per user) and a backup store
for the meal log (Firestore behind a repository, or a small REST server; chosen in M2).

## Milestone plan

### M1: Prototype (due 1 Nov 2026), current state

An app skeleton that can grow into the full spec without a rewrite.

- [x] All screens on one type-safe Navigation Compose graph, with a bottom bar (Today, History, Settings)
- [x] Both list→detail flows working; logging a meal from FoodDetail updates Today's total vs. goal
- [x] MVVM + UDF: one `UiState` per screen, stateless composables, user input in `SavedStateHandle`
- [x] Process-death demo works: typed grams and the back stack come back after `adb shell am kill`
- [x] Domain interfaces (`MealRepository`, `FoodRepository`, `FoodRecognizer`, `SettingsRepository`)
      with **in-memory fakes**, wired by hand in `di/AppContainer`
- [x] Pure `NutritionCalculator` with JVM unit tests, plus a `TodayViewModel` test with fakes
- [x] Product images with Coil (placeholder / error states)
- [x] `AI_USE_LOG.md` and this README
- AddFromPhoto is a placeholder. Meals live in memory, so they are lost when the process dies
  (Room fixes this in M2).

### M2: Data layer (due 6 Dec 2026)

Make the app genuinely offline-first.

- [ ] **Room as the single source of truth:** `FoodItem`, `MealEntry` (client uuid, `syncState`
      PENDING / SYNCED / FAILED, soft-delete tombstone), `DailyGoal`, `RecognitionJob`; every read is
      a `Flow`, and the UI observes only Room. Schema export + migrations from here on.
- [ ] **Open Food Facts via Retrofit:** text search (debounced 400 ms, Room first, network results
      upserted into Room) and barcode lookup; custom User-Agent; missing nutrients shown as "incomplete".
- [ ] **Meal backup remote** (Firestore behind a repository, or REST), idempotent uploads by uuid,
      last-write-wins on `updatedAt`.
- [ ] **WorkManager sync:** `UploadEntriesWorker` (unique, `CONNECTED`, retry with backoff) and
      `RefreshFoodCacheWorker` (periodic 24 h, `UNMETERED` + battery not low).
- [ ] **POST_NOTIFICATIONS:** rationale after the first log, graceful denial (sync badge instead),
      re-request from Settings; the "N meals backed up" notification after a sync.
- [ ] Settings stored in DataStore; process-death demo still works with real data.

### M3: Final (due 11 Jan 2027)

DI, tests/CI and the integrated concept with its placement decision.

- [ ] **Hilt everywhere:** `AppContainer` is replaced by modules (database, network, repositories,
      recognition with `@OnDevice` / `@Cloud`, dispatchers); `@HiltWorker` for the workers.
- [ ] **Photo pipeline:** Photo Picker (no permission) → copy and downscale to app storage → ML Kit
      barcode scan first → otherwise food recognition → user picks a label → FoodDetail.
- [ ] **Two recognizers behind `FoodRecognizer`:** on-device (ML Kit / LiteRT with a food model) and
      cloud (Gemini via Firebase AI Logic), selected by a unit-tested `RecognitionPolicy`. Cloud only
      if the user opted in **and** on-device confidence < threshold (starting at 0.6) **and** the user
      taps "Try cloud". Offline → queued `RecognitionJob` handled by `CloudRecognitionWorker`.
- [ ] **Measurements** on ~30 food photos: latency (median, p90), top-1 / top-3 accuracy and bytes
      uploaded per engine; the threshold is chosen from these numbers.
- [ ] **Tests + CI:** JVM tests with fakes (calculator, policy, repositories, ViewModels, worker),
      an instrumented `MealDaoTest`, and GitHub Actions (`lint testDebugUnitTest assembleDebug`) required on every PR.
- [ ] **`SYSTEMS.md`** (the five questions: what binds, what will you do, what are you giving up,
      what breaks, what would change your mind), contribution statements, peer evaluations.

## Build and run

Toolchain (from the course labs): Gradle 9.7.0, Kotlin 2.4.21, Compose BOM 2026.09.00 (Material 3),
Navigation Compose 2.10.2, compileSdk/targetSdk 37, minSdk 26, JVM 17. AGP is 9.1.1 (the labs use
9.4.1) so that Android Studio 2025.3.2 can open the project. All versions are in `gradle/libs.versions.toml`.

You need a full **JDK 17** (a JRE is not enough) and **Android SDK Platform 37**. Open the folder in
Android Studio and press Run, or from a terminal:

```bash
./gradlew assembleDebug          # build the debug APK
./gradlew testDebugUnitTest      # JVM unit tests
./gradlew installDebug           # install on a running emulator/device
```

### Process-death demo

1. Today → Add food → search "nut" → Nutella → type `150` in Grams.
2. Press Home, then run:
   ```bash
   adb shell am kill mobile.caloriecounter
   ```
3. Reopen the app from recents: FoodDetail shows 150 g again, and Back returns to Search with "nut"
   (the back stack Today → Search → FoodDetail is restored).

## File map (`app/src/main/kotlin/mobile/caloriecounter/`)

| Path | What it is |
|---|---|
| `CalorieApp.kt` | `Application`; creates the `AppContainer` |
| `MainActivity.kt` | Single activity; hosts `AppNavHost` |
| `di/AppContainer.kt` | Manual DI: repositories typed by interface (becomes Hilt modules in M3) |
| `domain/` | Models, repository interfaces, `FoodRecognizer`, `NutritionCalculator` |
| `data/fake/` | In-memory fakes + `SampleData` (real Open Food Facts products); replaced by Room/Retrofit in M2 |
| `recognition/FakeFoodRecognizer.kt` | Fixed-result recognizer (real engines in M3) |
| `ui/navigation/` | `Routes.kt` (`@Serializable` routes), `AppNavHost.kt` (graph + bottom bar) |
| `ui/<screen>/` | `XxxViewModel` + `XxxScreen` (stateful) / `XxxContent` (stateless) |
| `ui/common/Common.kt` | `FoodImage` (Coil), `MealTypeChips`, formatting/parsing helpers |
| `ui/theme/AppTheme.kt` | Material 3 defaults |

Tests are in `app/src/test/`: `NutritionCalculatorTest`, `TodayViewModelTest`.

Food data and images: Open Food Facts, under the Open Database License.
