# Health Calendar App - Setup Guide

## Overview

This Android application helps users manage their health by:
- Tracking medications with customizable reminders
- Managing appointments and health events
- Setting up precise medication alarms
- Maintaining a medication history log

## Prerequisites

- **Android Studio**: Latest stable version (Hedgehog or newer recommended)
- **JDK**: Java 17 or higher
- **Android SDK**: API level 26 (Android 8.0) minimum, API level 34 (Android 14) target
- **Physical Device or Emulator**: Running Android 8.0 or higher

## Project Setup

### 1. Open the Project

1. Launch Android Studio
2. Click "Open an Existing Project"
3. Navigate to this project folder
4. Click "OK"

### 2. Sync Gradle

1. Wait for Android Studio to index the project
2. Click "Sync Project with Gradle Files" (or wait for auto-sync)
3. Wait for all dependencies to download

### 3. Configure Android SDK

1. Go to **File > Project Structure > SDK Location**
2. Ensure Android SDK location is set correctly
3. Verify SDK platforms and tools are installed

## Building the App

### Debug Build

```bash
# From Android Studio Terminal (PowerShell):
.\gradlew assembleDebug
```

Or use Android Studio's **Build > Build Bundle(s) / APK(s) > Build APK(s)**

### Release Build

1. Create a keystore (if you don't have one):
   ```bash
   keytool -genkey -v -keystore my-release-key.jks -keyalg RSA -keysize 2048 -validity 10000 -alias my-key-alias
   ```

2. Add signing config to `app/build.gradle.kts`
3. Run:
   ```bash
   .\gradlew assembleRelease
   ```

## Running the App

### On Emulator

1. Create an AVD (Android Virtual Device) via **Tools > Device Manager**
2. Select API 26+ system image
3. Click the green "Run" button or press **Shift + F10**
4. Select your emulator

### On Physical Device

1. Enable Developer Options on your Android device:
   - Go to **Settings > About Phone**
   - Tap "Build Number" 7 times
2. Enable USB Debugging in Developer Options
3. Connect device via USB
4. Click "Run" and select your device

## Important Permissions

The app requires the following permissions:

- **POST_NOTIFICATIONS**: To show medication reminders
- **SCHEDULE_EXACT_ALARM**: For precise medication timing
- **RECEIVE_BOOT_COMPLETED**: To restore alarms after reboot

### Requesting Permissions

On Android 13+ (API 33+), you'll need to manually grant notification permission:
1. Open the app
2. When prompted, tap "Allow" for notifications
3. For exact alarms, go to **Settings > Apps > Health Calendar > Alarms & Reminders** and enable

## Project Structure

```
app/
├── data/
│   ├── database/
│   │   ├── entities/          # Room database entities
│   │   ├── dao/               # Data Access Objects
│   │   ├── HealthDatabase.kt  # Main database class
│   │   └── Converters.kt      # Type converters
│   └── repository/            # Repository layer
├── ui/
│   ├── screens/               # Composable screens
│   ├── navigation/            # Navigation setup
│   └── theme/                 # Material3 theme
├── viewmodel/                 # ViewModels (MVVM)
├── util/
│   └── alarm/                 # Alarm scheduling utilities
└── di/                        # Dependency injection (Hilt)
```

## Key Technologies

- **Kotlin**: Primary programming language
- **Jetpack Compose**: Modern declarative UI framework
- **Room Database**: Local SQLite database
- **Hilt**: Dependency injection
- **AlarmManager**: Precise medication alarms
- **WorkManager**: Background work scheduling
- **Kotlinx DateTime**: Date and time handling
- **Material3**: Modern Material Design

## Features Implementation Status

### ✅ Implemented
- Custom medication entry with details
- Medication list and search
- Medication schedules with daily/weekly frequency
- Alarm scheduling with exact timing
- Calendar view for appointments
- Home dashboard with quick actions
- Material3 UI design

### 🚧 To Be Implemented
- NHS medication database integration
- Medication web scraping
- Appointment creation and editing
- Medication adherence statistics
- Photo support for medications
- Export/import data
- Cloud sync

## NHS Database Integration (Future)

To add NHS medication database scraping:

1. Add web scraping library:
   ```kotlin
   implementation("org.jsoup:jsoup:1.16.1")
   ```

2. Create an API service to fetch medication data
3. Parse HTML or use NHS API if available
4. Store in local database for offline access

Example NHS sources:
- https://www.nhs.uk/medicines/
- NHS OpenData API (if available)

## Troubleshooting

### Gradle Build Fails

```bash
# Clean and rebuild
.\gradlew clean
.\gradlew build
```

### Alarm Not Firing

1. Check battery optimization settings
2. Verify SCHEDULE_EXACT_ALARM permission on Android 12+
3. Check device's "Do Not Disturb" settings

### Database Issues

```bash
# Uninstall app to clear database
adb uninstall com.healthcalendar.app

# Or clear app data in device settings
```

### Compose Preview Not Showing

1. Invalidate caches: **File > Invalidate Caches > Invalidate and Restart**
2. Ensure you're using the latest Android Studio

## Development Tips

### Running Tests

```bash
# Unit tests
.\gradlew test

# Instrumented tests (requires emulator/device)
.\gradlew connectedAndroidTest
```

### Debugging

1. Set breakpoints in Android Studio
2. Run app in debug mode (**Shift + F9**)
3. Use Logcat to view logs

### Code Style

- Follow Kotlin coding conventions
- Use meaningful variable names
- Document complex functions
- Keep Composables small and focused

## Contributing

When adding new features:

1. Create feature branch
2. Follow existing architecture patterns
3. Add appropriate documentation
4. Test on multiple Android versions
5. Submit pull request

## License

This project is for personal use. Modify and distribute as needed.

## Support

For issues or questions:
- Check Android Studio Logcat for errors
- Review Room database migrations if schema changes
- Verify all permissions are granted
- Test on physical device if emulator issues occur

## Next Steps

1. **Test the app thoroughly** on different Android versions
2. **Add medication photos** using camera/gallery
3. **Implement NHS scraping** for medication database
4. **Add statistics** for medication adherence
5. **Implement appointment management** fully
6. **Add data export/import** functionality
7. **Consider cloud sync** with Firebase or similar

Happy coding! 🏥💊📅
