# Health Calendar App

An Android application for managing health appointments and medication reminders.

## Features

### 1. Calendar System
- View and manage appointments and reminders
- Monthly/Weekly/Daily views
- Color-coded events
- Recurring appointments support

### 2. Medication Reminders
- Set multiple daily alarms for medications
- Customizable medication schedules
- Snooze and dismiss functionality
- Medication history tracking

### 3. Custom Medication Management
- Add custom medications with:
  - Name and dosage
  - Form (tablet, liquid, injection, etc.)
  - Frequency and timing
  - Notes and instructions
  - Photo support

### 4. Medication Database (Future Enhancement)
- Integration with NHS medication database
- Search and auto-complete functionality
- Medication information and side effects

## Tech Stack

- **Language**: Kotlin
- **UI Framework**: Jetpack Compose
- **Database**: Room (SQLite)
- **Notifications**: WorkManager + AlarmManager
- **Dependency Injection**: Hilt
- **Architecture**: MVVM + Repository Pattern

## Project Structure

```
app/
├── data/
│   ├── database/
│   │   ├── entities/
│   │   ├── dao/
│   │   └── HealthDatabase.kt
│   ├── repository/
│   └── models/
├── ui/
│   ├── calendar/
│   ├── medication/
│   ├── alarm/
│   └── components/
├── viewmodel/
├── util/
│   ├── notification/
│   └── alarm/
└── di/
```

## Getting Started
1. Open the project in Android Studio.
2. (Optional) Create and activate the project's virtual environment `.venv` and install dependencies if required.
3. Sync Gradle dependencies in Android Studio.
4. Build & install the APK using the repository Python wrapper (recommended). From the repository root run:

```powershell
& ".venv\Scripts\python.exe" scripts\build_apk.py debug
```

Notes:
- The build script will invoke Gradle and automatically install the APK via ADB when a device is connected.
- After any code change or after updating documentation/instructions you MUST rebuild and reinstall the APK using the command above to validate your change on a device.

Windows OneDrive users:
- If your project folder is under OneDrive sync, pause OneDrive and delete `app\build\` before building. Resume sync after APK generation to avoid locked files during Gradle tasks.

## Permissions Required

- `SET_ALARM` - For medication reminders
- `POST_NOTIFICATIONS` - For notification alerts
- `SCHEDULE_EXACT_ALARM` - For precise medication timing
- `RECEIVE_BOOT_COMPLETED` - To restore alarms after device restart

## Future Enhancements

- [ ] NHS medication database integration
- [ ] Medication interaction warnings
- [ ] Export/Import data
- [ ] Cloud sync
- [ ] Wear OS companion app
- [ ] Medication adherence reports
