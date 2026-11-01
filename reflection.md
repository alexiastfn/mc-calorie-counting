# Prototype reflection: where AI stopped

## What AI did well

- Reproduced the exact course toolchain by reading the lab project first (Gradle, AGP, Kotlin,
  Compose BOM, version catalog), so the prototype started from known-good versions.
- Generated the boilerplate fast: the type-safe navigation graph with a bottom bar, seven screens,
  a ViewModel per screen, and the stateless/stateful composable split.
- Produced believable sample data, including real Open Food Facts products with images (and one
  product with missing nutrition data, which exercises the "incomplete" path).
- Wrote the pure `NutritionCalculator` with unit tests and a ViewModel test that uses fake repositories.
- Drove the app on the emulator with `adb` to check the flows and the process-death demo.

## What broke when you stressed it

- **Rotate the device:** state survived. Typed grams in Food detail and the logged meals are still
  there after rotating, because state lives in ViewModels and `SavedStateHandle`. But the layout is
  not adapted to landscape: on Food detail the image and the bottom bar take the whole screen height,
  so the grams field and the "Log" button are only reachable by scrolling.
- **Kill and reopen the app:** partly. After `adb shell am kill`, the typed grams, the selected meal
  type and the back stack (Today → Search → Food detail) come back. Every meal logged during the
  session is gone and the seed data reappears, because the repositories are in-memory fakes: nothing
  is saved to disk.
- **Airplane mode:** graceful. Search, logging and Today keep working because all data is local;
  product images that are not cached show the error placeholder instead of crashing. But this only
  works because there is no network code at all: there is no real food search, no backup, and no
  notion of "pending, not yet synced".
- **500 items in a list:** not tested; the seed data has only 7 foods and 6 meals. All lists use
  `LazyColumn` with stable keys, so it should scroll fine, but search filters the whole list in
  memory on every keystroke, which will not hold for a real food database.
- **Two people editing the same data:** there is no sync and no conflict handling. Each phone has its
  own in-memory log, so two devices would simply disagree and nothing would be backed up.

## What is missing for production

| Gap in the prototype | Lab that fixes it |
|---|---|
| UI and logic are separated (ViewModels, `StateFlow<UiState>`, `SavedStateHandle`), but the data layer is only fakes | Lab 4 (architecture) |
| No data from a real server: food search and barcode lookup must come from Open Food Facts | Lab 5 (networking) |
| Nothing is saved on the device: logged meals are lost when the process dies | Lab 6 (persistence) |
| Does not work offline in the real sense: no "pending" state, no upload, no background sync | Lab 7 (offline-first, WorkManager) |
| Dependencies wired by hand in `AppContainer` | Lab 8 (dependency injection) |
| Only two test classes and no CI; nothing stops a broken change | Lab 9 (testing, CI) |
| No mobile-computing concept yet: photo recognition is a placeholder | Lab 10 (integrated concept) |

Gaps specific to this app:

| Gap | Where it is addressed |
|---|---|
| Open Food Facts limits search to 10 requests/min per user: search must be debounced and served from Room first | M2 (networking + persistence) |
| Crowd-sourced nutrition data can be missing or wrong; the app must show "incomplete" rather than crash | M2 (`FoodMapper`, null nutrients) |
| Deletes are hard deletes; offline deletes would never reach the backup, so they need soft-delete tombstones and a client uuid per entry | M2 (Room + sync) |
| Doze can delay uploads for hours; the UI must show a "pending" badge instead of waiting for the network | M2 (WorkManager) |
| Notification permission flow (rationale after the first log, graceful denial, re-request from Settings) | M2 (POST_NOTIFICATIONS) |
| The recognition threshold (0.6 to start) must come from measuring both engines on ~30 photos (latency, top-1/top-3 accuracy, bytes uploaded) | M3 (integrated concept, SYSTEMS.md) |
| Landscape layout of Food detail hides the grams field | Later polish (not graded) |

## One sentence

AI gave me a prototype that looks and navigates like the real app, but not the decisions about where
meal data lives, what happens with no signal, and where food recognition runs, because those depend
on my users' privacy and connectivity constraints and have to be measured and defended by me, not
pattern-matched.
