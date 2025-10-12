# Health Calendar Android App - Project Summary

## 🎯 Project Overview

A comprehensive Android health management application that helps users:
1. **Track medications** with custom details, dosages, and active/discontinued status
2. **Set medication alarms** with recurring patterns (daily/weekly/biweekly/monthly/yearly)
3. **Manage appointments** using an integrated calendar with recurring patterns
4. **Monitor medication adherence** with history logs
5. **Search medications** using Drugs.com integration
6. **Scan documents** with ML Kit for medication records

## 📱 App Features

### ✅ Fully Implemented

#### 1. Medication Management
- Add/edit/delete medications
- Custom fields: name, dosage (per tablet), amount (quantity), unit, form (tablet, liquid, etc.)
- Instructions and notes
- Search functionality with A-Z sorting
- Active/Discontinued medication sections
- Medication detail view
- Photo support (placeholder)

#### 2. Medication Schedules & Alarms
- Multiple medications per alarm
- Time selection (hour/minute)
- Recurring patterns: Daily, Weekly, Biweekly, Monthly, Yearly
- Enable/disable alarms
- Persistent alarms using AlarmManager
- Auto-reschedule after device reboot
- Medication reminders tab

#### 3. Calendar & Appointments
- Month view calendar with responsive design (foldable tablet support)
- Date selection
- Appointment display with categories
- Color-coded appointments
- Recurring appointments support
- Link multiple medications to appointments
- Appointment categories (Doctor, Dentist, Lab Test, etc.)

#### 4. Alarms Tab
- Combined view of medication reminders and custom alarms
- Sorted by date and time
- Mark medication reminders as taken
- Quick access to edit/delete
- Recurring pattern indicators

#### 5. Document Scanning
- ML Kit Document Scanner integration
- Save scanned documents
- View document history
- Activity result pattern implementation

#### 6. Home Dashboard
- Welcome card with time-based greeting
- Quick actions (Add Medication, View Calendar)
- Recent medications list (active only)
- Upcoming appointments preview

#### 7. UI/UX
- Material Design 3
- Jetpack Compose (modern declarative UI)
- Bottom navigation (Home, Calendar, Medications, Alarms, Documents)
- Responsive design for foldable tablets (Vivo X Fold 3 Pro optimized)
- Clean, intuitive interface
- Dark theme support
- Window size class handling (Compact/Medium/Expanded)

#### 8. Data Management
- CSV/JSON/XML export
- CSV/JSON import with backward compatibility
- Proper database migrations (no data loss on updates)

### 🚧 In Progress / Planned Features

1. **Alarm Improvements**
   - ⚠️ Fix alarm not triggering
   - Link alarms to medication schedules automatically
   - Add "Taken/Not Taken" action buttons to notifications
   - Custom notification sounds
   - Use system alarm sounds

2. **Drugs.com Integration** (replacing NHS)
   - Search medication names
   - Auto-complete
   - Open Drugs.com pages for drug information
   - Local medication index

3. **Medical History & Statistics**
   - Replace "View Calendar" button with "Medical History"
   - Adherence tracking
   - Statistics dashboard
   - Missed dose tracking
   - Visual charts and reports
   - Monthly/weekly summaries

4. **Theme System**
   - Multiple color themes
   - Theme selector in settings
   - Custom accent colors
   - Light/dark variants

5. **Calendar Improvements**
   - Remove alarms from appointment calendar view
   - Keep alarms only in dedicated Alarms tab
   - Better appointment filtering

6. **Enhanced Appointment Management**
   - Full CRUD operations (completed)
   - Appointment reminders
   - Calendar export/sync

7. **Cloud Features**
   - Cloud sync (Firebase)
   - Backup/restore
   - Multi-device support

8. **Additional Features**
   - Medication refill reminders
   - Prescription management
   - Health provider contacts
   - Wear OS companion app

4. **Photo Support**
   - Take photos of medication packaging
   - Gallery integration

5. **Data Management**
   - Export to CSV/JSON
   - Import data
   - Cloud sync (Firebase)
   - Backup/restore

6. **Additional Features**
   - Medication refill reminders
   - Prescription management
   - Health provider contacts
   - Wear OS companion app

## 🏗️ Technical Architecture

### Technology Stack

| Component | Technology |
|-----------|-----------|
| Language | Kotlin |
| UI Framework | Jetpack Compose + Material3 |
| Architecture | MVVM (Model-View-ViewModel) |
| Database | Room (SQLite) |
| Dependency Injection | Hilt (Dagger) |
| Notifications | AlarmManager + NotificationManager |
| Background Work | WorkManager |
| Date/Time | Kotlinx DateTime |
| Navigation | Jetpack Navigation Compose |
| Async | Kotlin Coroutines + Flow |
| Build System | Gradle (Kotlin DSL) |

### Project Structure

```
app/
├── data/
│   ├── database/
│   │   ├── entities/          # Data models (Medication, Appointment, etc.)
│   │   ├── dao/               # Database access objects
│   │   ├── HealthDatabase.kt  # Room database
│   │   └── Converters.kt      # Type converters for Room
│   ├── repository/            # Repository pattern implementation
│   └── api/                   # Future: NHS API service
│
├── ui/
│   ├── screens/               # All UI screens
│   │   ├── HomeScreen.kt
│   │   ├── medication/        # Medication-related screens
│   │   └── calendar/          # Calendar screen
│   ├── navigation/            # Navigation setup
│   └── theme/                 # Material3 theme customization
│
├── viewmodel/                 # ViewModels for MVVM
│   ├── MedicationViewModel.kt
│   └── AppointmentViewModel.kt
│
├── util/
│   └── alarm/                 # Alarm scheduling utilities
│       ├── AlarmScheduler.kt
│       ├── AlarmReceiver.kt
│       └── BootReceiver.kt
│
├── di/                        # Dependency injection modules
│   └── DatabaseModule.kt
│
├── HealthCalendarApplication.kt  # Application class
└── MainActivity.kt               # Main entry point
```

### Database Schema

#### Tables

1. **medications**
   - id, name, dosage, unit, form
   - instructions, notes, photoUri
   - createdAt, isActive

2. **medication_schedules**
   - id, medicationId (FK)
   - time, frequency, daysOfWeek
   - isEnabled, lastAlarmSet

3. **medication_logs**
   - id, medicationId (FK)
   - scheduledTime, takenTime
   - status, notes

4. **appointments**
   - id, title, description, location
   - startTime, endTime
   - category, reminderMinutesBefore
   - isRecurring, recurringPattern, color

### Key Design Patterns

1. **MVVM Architecture**
   - ViewModels handle business logic
   - UI observes StateFlow/Flow
   - Repository pattern for data access

2. **Dependency Injection**
   - Hilt provides dependencies
   - Singleton repositories
   - Scoped ViewModels

3. **Reactive Programming**
   - Kotlin Flow for data streams
   - StateFlow for UI state
   - Coroutines for async operations

## 📋 Requirements

### Minimum Requirements
- Android 8.0 (API 26) or higher
- 50 MB storage space
- Internet (future NHS integration)

### Recommended
- Android 10.0 (API 29) or higher
- 100 MB storage
- Physical device for alarm testing

### Permissions
- `POST_NOTIFICATIONS` - Show medication reminders
- `SCHEDULE_EXACT_ALARM` - Precise alarm timing
- `RECEIVE_BOOT_COMPLETED` - Restore alarms after reboot
- `WAKE_LOCK` - Wake device for alarms
- `VIBRATE` - Vibration on notifications
- `INTERNET` - Future NHS integration

## 🚀 Getting Started

### Development Setup

1. **Install Android Studio** (latest version)
2. **Clone/Open project** in Android Studio
3. **Sync Gradle** dependencies
4. **Run on emulator** or physical device

### Build Commands

```powershell
# Debug build
.\gradlew assembleDebug

# Release build
.\gradlew assembleRelease

# Run tests
.\gradlew test
```

### First Run

1. Grant notification permission when prompted
2. Add your first medication
3. Set up a schedule
4. Test the alarm functionality

## 🔧 Configuration

### Changing App Name
Edit `app/src/main/res/values/strings.xml`:
```xml
<string name="app_name">Your App Name</string>
```

### Changing Package Name
1. Refactor package name in Android Studio
2. Update `build.gradle.kts` applicationId
3. Update AndroidManifest.xml

### Customizing Theme
Edit `app/src/main/java/.../ui/theme/Color.kt` and `Theme.kt`

## 📊 Future NHS Integration

### Implementation Plan

1. **Add Dependencies**
   ```kotlin
   implementation("org.jsoup:jsoup:1.16.1")  // Web scraping
   // OR
   implementation("com.squareup.retrofit2:retrofit:2.9.0")  // REST API
   ```

2. **Create Service**
   - Implement `NHSMedicationService`
   - Parse NHS website or use API
   - Cache results in Room database

3. **Update UI**
   - Add search autocomplete
   - Display drug information
   - Show interaction warnings

### NHS Data Sources

- **NHS Medicines A-Z**: https://www.nhs.uk/medicines/
- **NHS OpenPrescribing**: https://openprescribing.net/
- **OpenFDA API**: https://open.fda.gov/ (alternative)

## 🧪 Testing

### Unit Tests
- Repository tests
- ViewModel tests
- Utility function tests

### UI Tests
- Compose UI tests
- Navigation tests
- Integration tests

### Manual Testing Checklist
- [ ] Add medication
- [ ] Create schedule
- [ ] Receive alarm notification
- [ ] View calendar
- [ ] Search medications
- [ ] Edit medication
- [ ] Delete medication
- [ ] Test after device reboot

## 🐛 Known Issues & Limitations

1. **Alarm Limitations**
   - Some devices have aggressive battery optimization
   - May need to whitelist app in battery settings

2. **Calendar Implementation**
   - Basic month view only
   - No gesture support for month switching

3. **Photo Support**
   - Not yet implemented
   - Placeholder in database

4. **NHS Integration**
   - Not implemented
   - Requires web scraping or API access

## 📝 Code Examples

### Adding a Medication (ViewModel)
```kotlin
viewModel.saveMedication(
    name = "Aspirin",
    dosage = "100",
    unit = "mg",
    form = MedicationForm.TABLET,
    instructions = "Take with food",
    notes = "For headaches"
)
```

### Creating a Schedule
```kotlin
val schedule = MedicationSchedule(
    medicationId = medicationId,
    time = LocalTime(9, 0),  // 9:00 AM
    frequency = ScheduleFrequency.DAILY,
    isEnabled = true
)
viewModel.addSchedule(schedule)
```

### Observing Data (Compose)
```kotlin
val medications by viewModel.medications.collectAsState()

LazyColumn {
    items(medications) { medication ->
        MedicationCard(medication)
    }
}
```

## 🎨 UI Screenshots (Conceptual)

### Home Screen
- Welcome card
- Quick actions
- Medication list preview
- Upcoming appointments

### Medication List
- Searchable list
- Medication cards
- FAB for adding

### Medication Detail
- Full medication info
- Schedule list
- Edit/delete options

### Calendar
- Month grid
- Day selection
- Appointments list

## 📚 Resources

### Documentation
- [Jetpack Compose](https://developer.android.com/jetpack/compose)
- [Room Database](https://developer.android.com/training/data-storage/room)
- [Hilt](https://developer.android.com/training/dependency-injection/hilt-android)
- [AlarmManager](https://developer.android.com/training/scheduling/alarms)

### Libraries Used
- AndroidX Core: 1.12.0
- Compose BOM: 2023.10.01
- Room: 2.6.1
- Hilt: 2.48
- Navigation Compose: 2.7.5
- Kotlinx DateTime: 0.5.0

## 🤝 Contributing

When extending the app:
1. Follow existing architecture
2. Use Compose for new UI
3. Add ViewModel for business logic
4. Create repository methods
5. Update database schema if needed
6. Test thoroughly

## 📄 License

Personal project - use and modify as needed.

## 🎯 Next Steps Priority

1. **Test all features** on real device
2. **Implement NHS integration** for medication data
3. **Add medication photos** functionality
4. **Complete appointment CRUD** operations
5. **Add adherence statistics** and reports
6. **Implement data export/import**
7. **Add cloud sync** capability
8. **Create Wear OS companion** app

---

**Created**: October 2025  
**Version**: 1.0  
**Status**: Core features implemented, ready for testing and enhancement

For questions or issues, refer to SETUP.md for troubleshooting.
