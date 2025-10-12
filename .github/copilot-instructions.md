# Health Calendar Android App - AI Agent Instructions

This document contains repository-specific guidance for automated agents and contributors. It focuses on the project's critical build workflow, runtime constraints (OneDrive), and key conventions.

## Quick summary

- Language: Kotlin
- UI: Jetpack Compose (Material3)
- DI: Hilt
- DB: Room
- Time API: kotlinx-datetime
- Build: Gradle (invoked via the repository Python wrapper)

## Mandatory build & install workflow

This project is built with Gradle, but the supported developer workflow uses the provided Python wrapper script. The wrapper performs any required pre-steps and will auto-install the APK to a connected device via ADB.

DO THIS (from repository root):

```powershell
& ".venv\Scripts\python.exe" scripts\build_apk.py debug
```

For a clean release build:

```powershell
& ".venv\Scripts\python.exe" scripts\build_apk.py release --clean
```

Important rules (read carefully):
- Always rebuild and install the APK after making any code changes OR after updating these instructions. This is required — tests and device verification rely on running the updated APK on a real device.
- Do not call Gradle directly for day-to-day development (the Python wrapper handles environment nuances and ADB install behavior).

### OneDrive file-locking (Windows)

If your workspace is under OneDrive sync, OneDrive may lock files in `app/build/` and cause Gradle errors. Before building on Windows:

1. Pause OneDrive sync for the project folder.
2. Delete `app\build\` (if present).
3. Run the Python build script (commands above).
4. Resume OneDrive sync after APK generation and installation.

Failure to follow these steps commonly causes "Access denied" or locked `.lock` files during Gradle tasks.

## Development conventions (high-level)

- ViewModels expose `StateFlow<T>`; UI collects with `.collectAsState()`.
- Use `kotlinx.datetime` types across code and Room converters.
- Add proper Room migrations in `DatabaseModule.kt` and increment `HealthDatabase` version for schema changes.
- Use `FileProvider` for sharing internal/cache files and register paths in `res/xml/file_paths.xml`.

## Quick verification checklist after edits

1. Update code.
2. Rebuild & install with `scripts\build_apk.py debug` (see above).
3. Launch the app on the device and exercise changed areas.
4. Capture `adb logcat` if a crash or unexpected behavior occurs.

## Where to find things

- App entry: `app/src/main/java/com/healthcalendar/app/MainActivity.kt`
- Room DB: `app/src/main/java/com/healthcalendar/app/data/database/`
- Build helper: `scripts/build_apk.py`
- FileProvider paths: `app/src/main/res/xml/file_paths.xml`

---

If an automated agent is working in this repository, follow the mandatory build/install workflow after applying edits and report the build and install results before concluding the change.
# Health Calendar Android App - AI Agent Instructions# Health Calendar Android App - AI Agent Instructions



## Project Overview## Project Overview

Android health management app built with **Jetpack Compose + Room + Hilt** for medication tracking, appointment scheduling, and document scanning. Target: foldable tablets (Vivo X Fold 3 Pro).Android health management app built with **Jetpack Compose + Room + Hilt** for medication tracking, appointment scheduling, and document scanning. Target: foldable tablets (Vivo X Fold 3 Pro).



## Critical Build Workflow## Critical Build Workflow



### Building APKs### Building APKs

**ALWAYS use the Python build script**, not direct Gradle commands:**ALWAYS use the Python build script**, not direct Gradle commands:

```powershell```powershell

python scripts\build_apk.py debugpython scripts\build_apk.py debug

python scripts\build_apk.py release --cleanpython scripts\build_apk.py release --clean

``````



**Auto-installation:** Build script automatically installs APK via ADB if device is connected.### OneDrive File Locking Issue ⚠️

**Critical:** Project is in OneDrive which locks build files. Before ANY build:

### OneDrive File Locking Issue ⚠️1. Pause OneDrive sync

**Critical:** Project is in OneDrive which locks build files. Before ANY build:2. Delete `app\build\` directory if exists

1. Pause OneDrive sync3. Run build script

2. Delete `app\build\` directory if exists4. Resume OneDrive after APK generation

3. Run build script

4. Resume OneDrive after APK generationSymptoms: `Access denied` errors during Gradle builds, locked `.lock` files in `app\build\`.



Symptoms: `Access denied` errors during Gradle builds, locked `.lock` files in `app\build\`.## Architecture & Data Flow



## Architecture & Data Flow### MVVM + Repository Pattern

```

### MVVM + Repository PatternUI (Composables) → ViewModel → Repository → DAO → Room Database

```                     ↓

UI (Composables) → ViewModel → Repository → DAO → Room Database                  StateFlow

                     ↓```

                  StateFlow

```**Key Pattern:** ViewModels expose `StateFlow<T>`, UI collects with `.collectAsState()`:

```kotlin

**Key Pattern:** ViewModels expose `StateFlow<T>`, UI collects with `.collectAsState()`:val medications by viewModel.medications.collectAsState()

```kotlin```

val medications by viewModel.medications.collectAsState()

```### Database Schema (Room v2)

**Location:** `app/src/main/java/com/healthcalendar/app/data/database/`

### Database Schema (Room v10)

**Location:** `app/src/main/java/com/healthcalendar/app/data/database/`**Entities:**

**Current Version:** 10- `Medication` - Core medication info (name, dosage, form)

- `MedicationReminder` - Daily medication alarms (NEW - not yet in HealthDatabase)

**Entities:**- `MedicationSchedule` - Frequency patterns

- `Medication` - Core medication info (name, dosage, **amount**, unit, form)- `MedicationLog` - Taken/missed tracking

  - `dosage`: Dose per tablet (e.g., "500" for 500mg tablets)- `Appointment` - Calendar events (with category, color)

  - `amount`: Number of tablets to take (e.g., "2" for 2 tablets)- `ScannedDocumentEntity` - ML Kit document storage

  - Display format: "2 x 500 mg" = Take 2 tablets of 500mg each

- `MedicationReminder` - Daily medication alarms**Type Converters:** `Converters.kt` handles `kotlinx.datetime.LocalDateTime`, `LocalTime`, enums. Always use `kotlinx.datetime.TimeZone` (fully qualified).

- `MedicationSchedule` - Frequency patterns

- `MedicationLog` - Taken/missed tracking### Dependency Injection (Hilt)

- `Appointment` - Calendar events (with category, color, **medicationIds**)**Module Structure:**

  - `medicationId`: Deprecated single medication link- `DatabaseModule.kt` - Room database + all DAOs

  - `medicationIds`: List<Long> for multiple medication links- `AppModule.kt` - App-level singletons

- `ScannedDocumentEntity` - ML Kit document storage

- `NHSMedicationBookmark` - Saved NHS medications**DAO Provision Pattern:** Each DAO gets `@Provides` function calling `database.daoName()`.

- `Note` - General notes

**Missing DAO Warning:** If you create a new DAO (like `MedicationReminderDao`), MUST add to:

**Type Converters:** `Converters.kt` handles:1. `HealthDatabase.kt` - Add abstract function

- `kotlinx.datetime.LocalDateTime`, `LocalTime`, `LocalDate`2. `DatabaseModule.kt` - Add `@Provides` function

- `List<Int>`, `List<Long>`, `List<String>` (comma-separated)3. Update database version + migration

- Enums (MedicationForm, AppointmentCategory, etc.)

- Always use `kotlinx.datetime.TimeZone` (fully qualified)## UI Development Rules



### Database Migrations### Responsive Design (Tablets/Foldables)

**CRITICAL:** Always add proper migrations when changing schema. Current migrations in `DatabaseModule.kt`:**ALWAYS use `ResponsiveLayout.kt` utilities** (`app/src/main/java/com/healthcalendar/app/ui/util/ResponsiveLayout.kt`):

- **MIGRATION_8_9:** Added `medicationIds` to Appointment

- **MIGRATION_9_10:** Added `amount` field to Medication```kotlin

val windowSize = getWindowSizeClass()

**Pattern for new migrations:**when (windowSize) {

```kotlin    WindowSizeClass.COMPACT -> /* Phone: single column */

private val MIGRATION_X_Y = object : Migration(X, Y) {    WindowSizeClass.MEDIUM -> /* Small tablet: 2 columns */

    override fun migrate(database: SupportSQLiteDatabase) {    WindowSizeClass.EXPANDED -> /* Large tablet: 2-3 columns */

        // Add ALTER TABLE statements here}

        database.execSQL("ALTER TABLE table_name ADD COLUMN column_name TYPE DEFAULT value")```

    }

}**Breakpoints:** <600dp = COMPACT, 600-840dp = MEDIUM, >840dp = EXPANDED

```

**Layout Pattern for Tablets:** Use `Row` with weight distribution (e.g., CalendarScreen uses 45/55 split in landscape).

**Steps when adding new field:**

1. Add field to entity with default value### Material3 Patterns

2. Create migration in `DatabaseModule.kt`- **Always annotate time/date pickers:** `@OptIn(ExperimentalMaterial3Api::class)`

3. Add migration to `.addMigrations()`- **Card clicks:** Use `Card(onClick = {})`, not `clickable` modifier

4. Increment version in `HealthDatabase.kt`- **Icons:** `Icons.Filled.*` from `androidx.compose.material.icons-extended`

5. Update CSV/JSON importers with backward compatibility (`optString()` with defaults)

6. Update exporters to include new field### Navigation

**Sealed class routes** in `Navigation.kt`. Pattern for parameterized routes:

### Dependency Injection (Hilt)```kotlin

**Module Structure:**object EditAppointment : Screen("edit_appointment/{appointmentId}") {

- `DatabaseModule.kt` - Room database + all DAOs + migrations    fun createRoute(appointmentId: Long) = "edit_appointment/$appointmentId"

- `AppModule.kt` - App-level singletons}

```

**DAO Provision Pattern:** Each DAO gets `@Provides` function calling `database.daoName()`.

**Navigate:** `navController.navigate(Screen.EditAppointment.createRoute(id))`

**Adding New DAO:**

1. Create DAO interface in `data/database/dao/`## NHS Medication Search

2. Add abstract function to `HealthDatabase.kt`

3. Add `@Provides` function in `DatabaseModule.kt`**Architecture:** Lightweight browser-based (NOT local database).

4. Inject into repository- **Index:** `app/src/main/assets/nhs_medications.json` (31KB, 297 medications)

- **Data Model:** `NHSMedicationInfo(name: String, url: String)` - ONLY name and NHS URL

## UI Development Rules- **Search:** Local index search in `LocalNHSMedicationService.kt`

- **Details:** Opens browser to NHS website (don't store descriptions/side effects)

### Responsive Design (Tablets/Foldables)

**ALWAYS use `ResponsiveLayout.kt` utilities** (`app/src/main/java/com/healthcalendar/app/ui/util/ResponsiveLayout.kt`):**Updating Index:** Run `scripts/build_medication_index.py` (scrapes NHS A-Z pages).



```kotlin## Document Scanning

val windowSize = getWindowSizeClass()

when (windowSize) {**Library:** ML Kit Document Scanner (`com.google.android.gms:play-services-mlkit-document-scanner:16.0.0-beta1`)

    WindowSizeClass.COMPACT -> /* Phone: single column */

    WindowSizeClass.MEDIUM -> /* Small tablet: 2 columns */**Activity Result Pattern:**

    WindowSizeClass.EXPANDED -> /* Large tablet: 2-3 columns */```kotlin

}val scanLauncher = rememberLauncherForActivityResult(

```    ActivityResultContracts.StartIntentSenderForResult()

) { result -> /* Handle scanned documents */ }

**Breakpoints:** <600dp = COMPACT, 600-840dp = MEDIUM, >840dp = EXPANDED```



**Layout Pattern for Tablets:** Use `Row` with weight distribution (e.g., CalendarScreen uses 45/55 split in landscape).**Scanner Launch:** Use `GmsDocumentScanning.getClient()` → `getStartScanIntent()` → launch with `IntentSenderRequest`.



### Material3 Patterns## Common Patterns

- **Always annotate time/date pickers:** `@OptIn(ExperimentalMaterial3Api::class)`

- **Card clicks:** Use `Card(onClick = {})`, not `clickable` modifier### Date/Time Handling

- **Icons:** `Icons.Filled.*` from `androidx.compose.material.icons-extended`**Library:** `kotlinx-datetime:0.5.0` (NOT `java.time`)

- Always import `kotlinx.datetime.LocalDateTime`, `LocalTime`, `LocalDate`

### Navigation- TimeZone: Use `TimeZone.currentSystemDefault()` (fully qualified: `kotlinx.datetime.TimeZone`)

**Sealed class routes** in `Navigation.kt`. Pattern for parameterized routes:- Converters handle Room storage as strings

```kotlin

object EditAppointment : Screen("edit_appointment/{appointmentId}") {### AlarmManager Integration

    fun createRoute(appointmentId: Long) = "edit_appointment/$appointmentId"**Critical permissions:** Add to `AndroidManifest.xml`:

}```xml

```<uses-permission android:name="android.permission.SCHEDULE_EXACT_ALARM" />

<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />

**Navigate:** `navController.navigate(Screen.EditAppointment.createRoute(id))````



## Key Features**Components:**

- `AlarmScheduler.kt` - Schedule medication reminders

### Medication Management- `AlarmReceiver.kt` - BroadcastReceiver for alarm events

- **Add/Edit Medications:** Name, dosage per tablet, amount, unit, form, instructions- `BootReceiver.kt` - Restore alarms after reboot

- **Daily Reminders:** Creates both `MedicationSchedule` (for notifications) and `MedicationReminder` (for UI display in Alarms tab)

- **NHS Integration:** Search and autofill from NHS medication index### Image Loading

**Library:** Coil 2.5.0

### Alarms & Appointments```kotlin

- **Multiple Medication Selection:** Alarms can link to multiple medications using checkboxesAsyncImage(

- **Display Format:** Shows "3 medications: Aspirin, Ibuprofen, Lisinopril" for multiple meds    model = imageUri,

- **Recurring Patterns:** Daily, weekly, monthly with custom days    contentDescription = "..."

)

### Import/Export```

- **Formats:** JSON, XML, CSV

- **Backward Compatibility:** Importers use `optString()` with default values for new fields## Version Requirements

- **CSV Headers:** Include all current fields (Dosage, Amount, Unit for medications)- **Min SDK:** API 26 (Android 8.0)

- **Target/Compile SDK:** API 34 (Android 14)

## NHS Medication Search- **Kotlin:** 1.9.20

- **Compose BOM:** 2023.10.01

**Architecture:** Lightweight browser-based (NOT local database).- **Java:** 17 (sourceCompatibility/targetCompatibility)

- **Index:** `app/src/main/assets/nhs_medications.json` (31KB, 297 medications)

- **Data Model:** `NHSMedicationInfo(name: String, url: String)` - ONLY name and NHS URL## File Organization

- **Search:** Local index search in `LocalNHSMedicationService.kt````

- **Details:** Opens browser to NHS website (don't store descriptions/side effects)app/src/main/java/com/healthcalendar/app/

├── data/

**Updating Index:** Run `scripts/build_medication_index.py` (scrapes NHS A-Z pages).│   ├── database/

│   │   ├── entities/        # Room entities

## Document Scanning│   │   ├── dao/             # Data access objects

│   │   └── HealthDatabase.kt

**Library:** ML Kit Document Scanner (`com.google.android.gms:play-services-mlkit-document-scanner:16.0.0-beta1`)│   ├── repository/          # Data repositories

│   └── api/                 # Network (NHS scraping)

**Activity Result Pattern:**├── ui/

```kotlin│   ├── screens/

val scanLauncher = rememberLauncherForActivityResult(│   │   ├── calendar/        # Calendar + appointments

    ActivityResultContracts.StartIntentSenderForResult()│   │   ├── medication/      # Med management + NHS search

) { result -> /* Handle scanned documents */ }│   │   └── documents/       # Scanner + viewer

```│   ├── navigation/          # NavController setup

│   ├── theme/               # Material3 theme

**Scanner Launch:** Use `GmsDocumentScanning.getClient()` → `getStartScanIntent()` → launch with `IntentSenderRequest`.│   └── util/                # ResponsiveLayout.kt

├── viewmodel/               # MVVM ViewModels

## Common Patterns├── util/

│   ├── alarm/               # AlarmManager utilities

### Date/Time Handling│   └── scanner/             # ML Kit helpers

**Library:** `kotlinx-datetime:0.5.0` (NOT `java.time`)└── di/                      # Hilt modules

- Always import `kotlinx.datetime.LocalDateTime`, `LocalTime`, `LocalDate````

- TimeZone: Use `TimeZone.currentSystemDefault()` (fully qualified: `kotlinx.datetime.TimeZone`)

- Converters handle Room storage as strings## Known Issues & Workarounds

- Use wildcard import: `import kotlinx.datetime.*` for date arithmetic

### Database Migration

### AlarmManager IntegrationCurrently using `.fallbackToDestructiveMigration()` - data lost on schema changes. For production, implement proper migrations.

**Critical permissions:** Add to `AndroidManifest.xml`:

```xml### MedicationReminder Integration (In Progress)

<uses-permission android:name="android.permission.SCHEDULE_EXACT_ALARM" />Entity and DAO exist but NOT yet added to `HealthDatabase.kt`. If building with reminders:

<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />1. Add `MedicationReminder::class` to `@Database` entities

```2. Add `abstract fun medicationReminderDao(): MedicationReminderDao`

3. Increment version to 3

**Components:**4. Add provider in `DatabaseModule.kt`

- `AlarmScheduler.kt` - Schedule medication reminders

- `AlarmReceiver.kt` - BroadcastReceiver for alarm events### Deprecated Fields

- `BootReceiver.kt` - Restore alarms after rebootNHS medication fields (description, sideEffects, etc.) are deprecated - only use `name` and `url`.



### Image Loading## Testing on Device

**Library:** Coil 2.5.0User's device: **Vivo X Fold 3 Pro** (foldable tablet)

```kotlin- Test responsive layouts in both folded (COMPACT/MEDIUM) and unfolded (EXPANDED) modes

AsyncImage(- Verify landscape calendar split (45/55 column weights)

    model = imageUri,- Check circle sizes on calendar dates (32-48dp range)

    contentDescription = "..."

)## Build Output

```APKs: `app\build\outputs\apk\debug\app-debug.apk` (~16-18 MB)

## Version Requirements
- **Min SDK:** API 26 (Android 8.0)
- **Target/Compile SDK:** API 34 (Android 14)
- **Kotlin:** 1.9.20
- **Compose BOM:** 2023.10.01
- **Java:** 17 (sourceCompatibility/targetCompatibility)
- **Database Version:** 10

## File Organization
```
app/src/main/java/com/healthcalendar/app/
├── data/
│   ├── database/
│   │   ├── entities/        # Room entities
│   │   ├── dao/             # Data access objects
│   │   ├── HealthDatabase.kt
│   │   └── Converters.kt    # Type converters
│   ├── repository/          # Data repositories
│   └── api/                 # Network (NHS scraping)
├── ui/
│   ├── screens/
│   │   ├── calendar/        # Calendar + appointments
│   │   ├── medication/      # Med management + NHS search
│   │   └── documents/       # Scanner + viewer
│   ├── navigation/          # NavController setup
│   ├── theme/               # Material3 theme
│   └── util/                # ResponsiveLayout.kt
├── viewmodel/               # MVVM ViewModels
├── util/
│   ├── alarm/               # AlarmManager utilities
│   ├── scanner/             # ML Kit helpers
│   ├── DataImporter.kt      # CSV/JSON/XML import
│   ├── DataExporter.kt      # CSV/JSON/XML export
│   └── CsvExporter.kt       # CSV-specific export
└── di/                      # Hilt modules (DatabaseModule, AppModule)
```

## Recent Updates (v21-v22)

### v21: Multiple Medication Selection
- Alarms can now link to multiple medications
- Checkbox-based multi-select dialog
- Display shows comma-separated list or count

### v22: Medication Amount Field
- Added `amount` field to track tablets to take
- `dosage` now means dose per tablet (e.g., 500mg per tablet)
- Display format: "2 x 500 mg" (2 tablets of 500mg each)
- Backward compatible: defaults to "1" for old data

## Data Integrity

### Import/Export Compatibility
**Always maintain backward compatibility:**
```kotlin
// JSON Import
amount = json.optString("amount", "1") // Default for old exports

// CSV Import  
amount = map["Amount"] ?: map["amount"] ?: "1" // Case-insensitive + default
```

**Export includes all current fields:**
- CSV: "ID,Name,Dosage,Amount,Unit,Form,..."
- JSON: Automatic via Gson
- XML: Explicit tags for each field

## Testing on Device
User's device: **Vivo X Fold 3 Pro** (foldable tablet)
- Test responsive layouts in both folded (COMPACT/MEDIUM) and unfolded (EXPANDED) modes
- Verify landscape calendar split (45/55 column weights)
- Check circle sizes on calendar dates (32-48dp range)

## Build Output
APKs: `app\build\outputs\apk\debug\app-debug.apk` (~19 MB)
Auto-installs to connected device via ADB after successful build
