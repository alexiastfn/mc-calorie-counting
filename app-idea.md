# App idea

## The basics

- **App name:** Calorie Counter
- **One-line pitch:** An offline-first calorie counter that lets people log meals by searching for a
  food, scanning a barcode in a photo, or recognizing the food in a photo, even with no network.
- **The problem:** Most calorie apps need a connection to search foods or log a meal and send meal
  photos to the cloud. People eat in places with bad or no signal (the metro, travel, a basement
  canteen), and meal photos are personal data they may not want to upload.
- **Target users:** People who track what they eat during the day, logging right after a meal,
  often on the move and not always online.

## The prototype scope

- **Core screen(s):** Today (meals grouped by breakfast / lunch / dinner / snack, total kcal vs. the
  daily goal) and Search → Food detail (type the grams, pick the meal type, "Log"). Also Meal detail
  (edit/delete an entry), History, Settings, and a placeholder for "Add from photo".
- **The main "thing" the app manages:** a meal entry: a portion of a food, logged at a time.
- **A few fields of that thing:** id, food id, grams, meal type (breakfast / lunch / dinner / snack),
  logged-at time. Each food has a name, brand, kcal / protein / carbs / fat per 100 g and an image URL.
  Calories are computed (`grams × kcalPer100g / 100`), never stored.

## The data

- **What records does the app create or collect?** Meal entries created by the user, a daily calorie
  goal, cached food items, and (later) photo-recognition jobs with their results and latency.
- **Where does data come from?** User input (grams, meal type, goal); Open Food Facts (food names,
  nutrition, product images, barcode lookups); the camera roll via the Photo Picker; an on-device or
  cloud food-recognition model.
- **Where does data need to go?** The meal log lives on the device and is backed up to a remote store
  (Firestore behind a repository, or a small REST server) when there is a connection. Food data is
  only read from Open Food Facts and cached locally. Nothing is shared with other users.

## Mobile-computing framing (required)

- **What must persist on the device** (survive a restart)? Every logged meal (with its sync state),
  the daily goal history, the cached foods the user has searched or logged, and any pending photo
  recognition job, so nothing the user entered is lost if the app or phone is restarted. Room is the
  single source of truth; settings go in DataStore.
- **What crosses the network** (comes from or goes to a server or API)? Food search and barcode
  lookups from Open Food Facts (rate-limited: 10 searches/min and 15 barcode lookups/min per user);
  product images (cached on disk by Coil); meal-log uploads to the backup store; and, only if the
  user opts in, a downscaled photo sent to the cloud recognizer.
- **What must still work with no signal**, and how should it behave offline? Logging, editing and
  deleting meals, viewing Today and History, searching foods already in the local cache, and on-device
  recognition all work fully offline. New entries are marked "pending" and uploaded later by
  WorkManager; a cloud recognition request is queued and shows "waiting for network".
- **The placement question:** Food recognition from a photo can run on-device (ML Kit / LiteRT with
  a food model) or in the cloud (Gemini). On-device keeps working offline, keeps the photo private,
  costs nothing per request and should answer in tens to a few hundred ms, but it is less accurate,
  adds model size to the APK and cannot estimate portions. The cloud is more accurate and can roughly
  estimate grams, but needs a connection, uploads a personal photo, adds about 1–3 s plus upload time,
  and costs money per request. My choice: on-device by default; cloud only if the user enabled it in
  Settings, the on-device confidence is below a threshold (starting at 0.6) and the user taps "Try
  cloud". I will set the threshold from measured latency and top-1 / top-3 accuracy on ~30 test photos.

## What comes next

Each upcoming lab makes one part of this real; the roadmap for this app is in `reflection.md`.
