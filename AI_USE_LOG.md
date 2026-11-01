# AI-use log

Append-only log of AI use on this project, one section per team member. One row per meaningful AI
interaction: what I was trying to do, what I prompted, what the model produced, and what I accepted
or rejected.

## Member A (alexiastfn)

| Date | What you were trying to do | What you prompted (short) | What the model produced | What you accepted or rejected, and WHY |
|---|---|---|---|---|
| 2026-10-31 | Start the M1 prototype on exactly the same Android toolchain as the course labs | Read the handout and the spec, inspect `lab2-compose` (read-only) and report the Gradle/AGP/Kotlin/SDK versions and the dependency style before writing any code; then build the prototype in small steps, asking me at each decision point | A summary of the lab setup: Gradle 9.7.0, AGP 9.4.1, Kotlin 2.4.21, Compose BOM 2026.09.00, Navigation 2.10.2, compileSdk 37 / minSdk 26, JVM 17, a version catalog. It also pointed out what was missing (ViewModel, test libraries, Coil) and two conflicts between my plan and the PDFs (Hilt and the `m1` tag are both deferred for now) | Accepted: the lab2 catalog plus the lifecycle/coroutines/junit versions from the lab4 catalog; add Coil 3.6.0 now; package `mobile.caloriecounter`; branch `feature/m1-prototype` |
| 2026-10-31 | An empty app that builds before any screens exist | Scaffold the project from the lab toolchain | The wrapper and Gradle files copied from the lab, the extended version catalog, `app/build.gradle.kts`, the manifest, a placeholder `MainActivity`, `.gitignore`, README | Kept all of it |
| 2026-10-31 | Domain interfaces, sample data and the calorie math, before any UI | Domain layer with in-memory fakes and manual dependency wiring | `domain/` (models, `FoodRepository`, `MealRepository`, `SettingsRepository`, `FoodRecognizer`, `NutritionCalculator`), in-memory fakes in `data/fake/` seeded with real Open Food Facts products, `FakeFoodRecognizer`, `AppContainer` + `CalorieApp`, `NutritionCalculatorTest` | Chose "AppContainer in Application" rather than a global service locator, because it maps directly onto Hilt in M3. From this point I commit the changes myself |
| 2026-10-31 | All screens and the two list→detail flows, with state that survives process death | Navigation graph, screens and ViewModels | `@Serializable` routes and the `AppNavHost` with a bottom bar; for each screen a ViewModel with one `StateFlow<UiState>` plus a stateful `XxxScreen` and a stateless `XxxContent`; user input in `SavedStateHandle`; Coil `FoodImage`; `TodayViewModelTest` | Chose "One data class" per screen for UI state, instead of a sealed Loading/Content |
| 2026-11-01 | Open the project in my Android Studio | Pasted the error: Studio 2025.3.2 supports AGP only up to 9.1.x, and the lab pins 9.4.1 | Two options: update Android Studio, or lower AGP | First chose to update Studio, then went with lowering AGP so I could keep my current IDE. I set it to 9.1.1 myself |
| 2026-11-01 | The Lab 3 documents for M1 | Put this log in the Lab 3 table format and fill in `app-idea.md` and `reflection.md` from the Lab 3 templates | This table, `app-idea.md` from the spec, and `reflection.md` based on stress checks it ran on the emulator (rotation, kill/reopen, airplane mode) | To review and edit in my own words before submitting |

## Prompts worth remembering

- Asking it to inspect the lab project and report the versions before writing any code, so the
  project reused the exact course toolchain instead of guessed versions.
- Asking it to stop at each decision point and offer options, so the choices (dependency versions,
  DI wiring, UI state shape) stayed mine.

## One thing AI got wrong, or could not tell me, on this project

_Draft, to rewrite in my own words:_ At the start, the AI said my installed JDK 21 was fine, based
only on `java -version`. That installation turned out to be a JRE without `javac`, and the first
build failed. Later, while testing on the emulator, it blamed the Settings screen for losing a digit
when the real cause was its own `adb` tap hitting the on-screen keyboard. Both times, building and
running the app caught the mistake, not the AI's explanation.
