# Passcode Security Enhancements Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Add comprehensive security features to the passcode lock system including biometric integration, auto-lock timing, failed attempt tracking, emergency access, and flexible passcode options.

**Architecture:** Extend existing DataStore preferences with new security settings. Add SecurityManager class to handle attempt tracking, lockout logic, and emergency access. Modify PasscodeLockScreen.kt to incorporate new UI components and flows. Follow MVVM pattern with repository for security state.

**Tech Stack:** Kotlin, Jetpack Compose, DataStore, BiometricPrompt, Hilt DI, Room (for attempt logs), Material3

---

## Task 1: Add Security Preferences to DataStore

**Files:**
- Modify: `app/src/main/java/com/healthcalendar/app/data/preferences/UserPreferences.kt:10-33`
- Test: Manual testing via settings UI (Android UI testing framework not set up)

### Step 1: Add new preference keys

**In UserPreferences.kt, add after line 33:**

```kotlin
        // Auto-lock settings
        val AUTO_LOCK_ENABLED = booleanPreferencesKey("auto_lock_enabled")
        val AUTO_LOCK_TIMEOUT_MINUTES = intPreferencesKey("auto_lock_timeout_minutes")
        val LOCK_ON_SCREEN_OFF = booleanPreferencesKey("lock_on_screen_off")
        val REMEMBER_UNLOCK_MINUTES = intPreferencesKey("remember_unlock_minutes")

        // Failed attempt security
        val MAX_FAILED_ATTEMPTS = intPreferencesKey("max_failed_attempts")
        val FAILED_ATTEMPT_COUNT = intPreferencesKey("failed_attempt_count")
        val LOCKOUT_UNTIL_TIMESTAMP = stringPreferencesKey("lockout_until_timestamp")
        val REQUIRE_BIOMETRIC_AFTER_FAILS = booleanPreferencesKey("require_biometric_after_fails")

        // Emergency access
        val SECURITY_QUESTION = stringPreferencesKey("security_question")
        val SECURITY_ANSWER_HASH = stringPreferencesKey("security_answer_hash")
        val RECOVERY_EMAIL = stringPreferencesKey("recovery_email")

        // Passcode options
        val PASSCODE_LENGTH = intPreferencesKey("passcode_length")
        val ALLOW_ALPHANUMERIC = booleanPreferencesKey("allow_alphanumeric")
```

### Step 2: Add Flow properties for reading

**Add after line 129:**

```kotlin
    // Auto-lock settings
    val autoLockEnabled: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.AUTO_LOCK_ENABLED] ?: false
    }

    val autoLockTimeoutMinutes: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.AUTO_LOCK_TIMEOUT_MINUTES] ?: 0 // 0 = immediate
    }

    val lockOnScreenOff: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.LOCK_ON_SCREEN_OFF] ?: false
    }

    val rememberUnlockMinutes: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.REMEMBER_UNLOCK_MINUTES] ?: 0
    }

    // Failed attempt security
    val maxFailedAttempts: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.MAX_FAILED_ATTEMPTS] ?: 5
    }

    val failedAttemptCount: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.FAILED_ATTEMPT_COUNT] ?: 0
    }

    val lockoutUntilTimestamp: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.LOCKOUT_UNTIL_TIMESTAMP]
    }

    val requireBiometricAfterFails: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.REQUIRE_BIOMETRIC_AFTER_FAILS] ?: false
    }

    // Emergency access
    val securityQuestion: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.SECURITY_QUESTION]
    }

    val securityAnswerHash: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.SECURITY_ANSWER_HASH]
    }

    val recoveryEmail: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.RECOVERY_EMAIL]
    }

    // Passcode options
    val passcodeLength: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.PASSCODE_LENGTH] ?: 6
    }

    val allowAlphanumeric: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.ALLOW_ALPHANUMERIC] ?: false
    }
```

### Step 3: Add setter functions

**Find the setters section (around line 150+) and add:**

```kotlin
    // Auto-lock setters
    suspend fun setAutoLockEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.AUTO_LOCK_ENABLED] = enabled
        }
    }

    suspend fun setAutoLockTimeoutMinutes(minutes: Int) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.AUTO_LOCK_TIMEOUT_MINUTES] = minutes
        }
    }

    suspend fun setLockOnScreenOff(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.LOCK_ON_SCREEN_OFF] = enabled
        }
    }

    suspend fun setRememberUnlockMinutes(minutes: Int) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.REMEMBER_UNLOCK_MINUTES] = minutes
        }
    }

    // Failed attempt setters
    suspend fun setMaxFailedAttempts(max: Int) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.MAX_FAILED_ATTEMPTS] = max
        }
    }

    suspend fun incrementFailedAttempts() {
        context.dataStore.edit { preferences ->
            val current = preferences[PreferencesKeys.FAILED_ATTEMPT_COUNT] ?: 0
            preferences[PreferencesKeys.FAILED_ATTEMPT_COUNT] = current + 1
        }
    }

    suspend fun resetFailedAttempts() {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.FAILED_ATTEMPT_COUNT] = 0
            preferences[PreferencesKeys.LOCKOUT_UNTIL_TIMESTAMP] = null
        }
    }

    suspend fun setLockoutUntil(timestamp: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.LOCKOUT_UNTIL_TIMESTAMP] = timestamp
        }
    }

    suspend fun setRequireBiometricAfterFails(required: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.REQUIRE_BIOMETRIC_AFTER_FAILS] = required
        }
    }

    // Emergency access setters
    suspend fun setSecurityQuestion(question: String, answerHash: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.SECURITY_QUESTION] = question
            preferences[PreferencesKeys.SECURITY_ANSWER_HASH] = answerHash
        }
    }

    suspend fun setRecoveryEmail(email: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.RECOVERY_EMAIL] = email
        }
    }

    // Passcode options setters
    suspend fun setPasscodeLength(length: Int) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.PASSCODE_LENGTH] = length
        }
    }

    suspend fun setAllowAlphanumeric(allowed: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.ALLOW_ALPHANUMERIC] = allowed
        }
    }
```

### Step 4: Verify compilation

**Run:**
```bash
./gradlew compileDebugKotlin
```

**Expected:** Build succeeds with no errors

### Step 5: Commit

```bash
git add app/src/main/java/com/healthcalendar/app/data/preferences/UserPreferences.kt
git commit -m "feat: add security preferences to DataStore"
```

---

## Task 2: Create SecurityManager for Business Logic

**Files:**
- Create: `app/src/main/java/com/healthcalendar/app/util/SecurityManager.kt`
- Test: Unit tests (to be created)

### Step 1: Create SecurityManager class

**Create new file `app/src/main/java/com/healthcalendar/app/util/SecurityManager.kt`:**

```kotlin
package com.healthcalendar.app.util

import com.healthcalendar.app.data.preferences.UserPreferencesRepository
import kotlinx.coroutines.flow.first
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages passcode security logic including failed attempts, lockouts, and emergency access
 */
@Singleton
class SecurityManager @Inject constructor(
    private val preferencesRepository: UserPreferencesRepository
) {

    /**
     * Check if user is currently locked out
     * @return Pair<Boolean, Long?> - (isLockedOut, remainingSeconds)
     */
    suspend fun checkLockoutStatus(): Pair<Boolean, Long?> {
        val lockoutTimestamp = preferencesRepository.lockoutUntilTimestamp.first()

        if (lockoutTimestamp.isNullOrEmpty()) {
            return Pair(false, null)
        }

        val lockoutUntil = Instant.parse(lockoutTimestamp)
        val now = Clock.System.now()

        return if (now < lockoutUntil) {
            val remainingSeconds = (lockoutUntil - now).inWholeSeconds
            Pair(true, remainingSeconds)
        } else {
            // Lockout expired, reset
            preferencesRepository.resetFailedAttempts()
            Pair(false, null)
        }
    }

    /**
     * Handle failed passcode attempt
     * @return Pair<Boolean, Long?> - (shouldLockout, lockoutDurationSeconds)
     */
    suspend fun handleFailedAttempt(): Pair<Boolean, Long?> {
        preferencesRepository.incrementFailedAttempts()

        val failedCount = preferencesRepository.failedAttemptCount.first()
        val maxAttempts = preferencesRepository.maxFailedAttempts.first()

        if (failedCount >= maxAttempts) {
            // Calculate lockout duration based on attempts
            val lockoutSeconds = calculateLockoutDuration(failedCount, maxAttempts)
            val lockoutUntil = Clock.System.now().plus(kotlin.time.Duration.parse("${lockoutSeconds}s"))

            preferencesRepository.setLockoutUntil(lockoutUntil.toString())
            return Pair(true, lockoutSeconds)
        }

        return Pair(false, null)
    }

    /**
     * Calculate lockout duration based on failed attempts
     * Progressive delays: 30s, 1min, 5min, 15min, 30min
     */
    private fun calculateLockoutDuration(failedCount: Int, maxAttempts: Int): Long {
        return when {
            failedCount <= maxAttempts -> 30L // 30 seconds
            failedCount <= maxAttempts + 1 -> 60L // 1 minute
            failedCount <= maxAttempts + 2 -> 300L // 5 minutes
            failedCount <= maxAttempts + 3 -> 900L // 15 minutes
            else -> 1800L // 30 minutes
        }
    }

    /**
     * Reset failed attempts on successful login
     */
    suspend fun handleSuccessfulLogin() {
        preferencesRepository.resetFailedAttempts()
    }

    /**
     * Get remaining attempts before lockout
     */
    suspend fun getRemainingAttempts(): Int {
        val failedCount = preferencesRepository.failedAttemptCount.first()
        val maxAttempts = preferencesRepository.maxFailedAttempts.first()
        return (maxAttempts - failedCount).coerceAtLeast(0)
    }

    /**
     * Verify security question answer
     */
    suspend fun verifySecurityAnswer(answer: String): Boolean {
        val storedHash = preferencesRepository.securityAnswerHash.first() ?: return false
        val answerHash = sha256(answer.trim().lowercase())
        return answerHash == storedHash
    }

    /**
     * SHA-256 hash helper
     */
    private fun sha256(input: String): String {
        val md = java.security.MessageDigest.getInstance("SHA-256")
        val bytes = md.digest(input.toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }
}
```

### Step 2: Verify compilation

**Run:**
```bash
./gradlew compileDebugKotlin
```

**Expected:** Build succeeds

### Step 3: Commit

```bash
git add app/src/main/java/com/healthcalendar/app/util/SecurityManager.kt
git commit -m "feat: add SecurityManager for lockout and security logic"
```

---

## Task 3: Update SettingsViewModel with Security State

**Files:**
- Modify: `app/src/main/java/com/healthcalendar/app/viewmodel/SettingsViewModel.kt:23-38`
- Modify: `app/src/main/java/com/healthcalendar/app/viewmodel/SettingsViewModel.kt:49-85`

### Step 1: Add security fields to SettingsUiState

**Modify SettingsUiState data class (around line 23):**

```kotlin
data class SettingsUiState(
    val themeMode: UserPreferencesRepository.ThemeMode = UserPreferencesRepository.ThemeMode.SYSTEM,
    val dynamicColor: Boolean = true,
    val fontScale: Float = 1.0f,
    val highContrast: Boolean = false,
    val amoledMode: Boolean = false,
    val language: UserPreferencesRepository.Language = UserPreferencesRepository.Language.ENGLISH,
    val notificationSound: UserPreferencesRepository.NotificationSound = UserPreferencesRepository.NotificationSound.DEFAULT,
    val vibrate: Boolean = true,
    val autoBackup: Boolean = false,
    val backupFrequency: UserPreferencesRepository.BackupFrequency = UserPreferencesRepository.BackupFrequency.WEEKLY,
    val exportFormat: UserPreferencesRepository.ExportFormat = UserPreferencesRepository.ExportFormat.CSV,
    val passcodeEnabled: Boolean = false,
    val passcodeHash: String? = null,
    val biometricEnabled: Boolean = false,
    // New security fields
    val autoLockEnabled: Boolean = false,
    val autoLockTimeoutMinutes: Int = 0,
    val lockOnScreenOff: Boolean = false,
    val rememberUnlockMinutes: Int = 0,
    val maxFailedAttempts: Int = 5,
    val failedAttemptCount: Int = 0,
    val requireBiometricAfterFails: Boolean = false,
    val passcodeLength: Int = 6,
    val allowAlphanumeric: Boolean = false,
    val securityQuestion: String? = null,
    val recoveryEmail: String? = null
)
```

### Step 2: Update combine flow

**Modify settingsState combine (around line 49):**

```kotlin
    val settingsState: StateFlow<SettingsUiState> = combine(
        preferencesRepository.themeMode,
        preferencesRepository.dynamicColor,
        preferencesRepository.fontScale,
        preferencesRepository.highContrast,
        preferencesRepository.amoledMode,
        preferencesRepository.language,
        preferencesRepository.notificationSound,
        preferencesRepository.vibrate,
        preferencesRepository.autoBackup,
        preferencesRepository.backupFrequency,
        preferencesRepository.exportFormat,
        preferencesRepository.passcodeEnabled,
        preferencesRepository.passcodeHash,
        preferencesRepository.biometricEnabled,
        preferencesRepository.autoLockEnabled,
        preferencesRepository.autoLockTimeoutMinutes,
        preferencesRepository.lockOnScreenOff,
        preferencesRepository.rememberUnlockMinutes,
        preferencesRepository.maxFailedAttempts,
        preferencesRepository.failedAttemptCount,
        preferencesRepository.requireBiometricAfterFails,
        preferencesRepository.passcodeLength,
        preferencesRepository.allowAlphanumeric,
        preferencesRepository.securityQuestion,
        preferencesRepository.recoveryEmail
    ) { values ->
        SettingsUiState(
            themeMode = values[0] as UserPreferencesRepository.ThemeMode,
            dynamicColor = values[1] as Boolean,
            fontScale = values[2] as Float,
            highContrast = values[3] as Boolean,
            amoledMode = values[4] as Boolean,
            language = values[5] as UserPreferencesRepository.Language,
            notificationSound = values[6] as UserPreferencesRepository.NotificationSound,
            vibrate = values[7] as Boolean,
            autoBackup = values[8] as Boolean,
            backupFrequency = values[9] as UserPreferencesRepository.BackupFrequency,
            exportFormat = values[10] as UserPreferencesRepository.ExportFormat,
            passcodeEnabled = values[11] as Boolean,
            passcodeHash = values[12] as String?,
            biometricEnabled = values[13] as Boolean,
            autoLockEnabled = values[14] as Boolean,
            autoLockTimeoutMinutes = values[15] as Int,
            lockOnScreenOff = values[16] as Boolean,
            rememberUnlockMinutes = values[17] as Int,
            maxFailedAttempts = values[18] as Int,
            failedAttemptCount = values[19] as Int,
            requireBiometricAfterFails = values[20] as Boolean,
            passcodeLength = values[21] as Int,
            allowAlphanumeric = values[22] as Boolean,
            securityQuestion = values[23] as String?,
            recoveryEmail = values[24] as String?
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SettingsUiState()
    )
```

### Step 3: Add individual StateFlow properties

**Add after existing StateFlow properties:**

```kotlin
    val autoLockEnabled = preferencesRepository.autoLockEnabled.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(), false
    )
    val autoLockTimeoutMinutes = preferencesRepository.autoLockTimeoutMinutes.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(), 0
    )
    val lockOnScreenOff = preferencesRepository.lockOnScreenOff.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(), false
    )
    val maxFailedAttempts = preferencesRepository.maxFailedAttempts.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(), 5
    )
    val failedAttemptCount = preferencesRepository.failedAttemptCount.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(), 0
    )
    val passcodeLength = preferencesRepository.passcodeLength.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(), 6
    )
```

### Step 4: Add setter methods

**Add at end of SettingsViewModel class:**

```kotlin
    // Security settings setters
    fun setAutoLockEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.setAutoLockEnabled(enabled)
        }
    }

    fun setAutoLockTimeout(minutes: Int) {
        viewModelScope.launch {
            preferencesRepository.setAutoLockTimeoutMinutes(minutes)
        }
    }

    fun setLockOnScreenOff(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.setLockOnScreenOff(enabled)
        }
    }

    fun setMaxFailedAttempts(max: Int) {
        viewModelScope.launch {
            preferencesRepository.setMaxFailedAttempts(max)
        }
    }

    fun setPasscodeLength(length: Int) {
        viewModelScope.launch {
            preferencesRepository.setPasscodeLength(length)
        }
    }

    fun setAllowAlphanumeric(allowed: Boolean) {
        viewModelScope.launch {
            preferencesRepository.setAllowAlphanumeric(allowed)
        }
    }

    fun setSecurityQuestion(question: String, answer: String) {
        viewModelScope.launch {
            val answerHash = sha256(answer.trim().lowercase())
            preferencesRepository.setSecurityQuestion(question, answerHash)
        }
    }

    fun setRecoveryEmail(email: String) {
        viewModelScope.launch {
            preferencesRepository.setRecoveryEmail(email)
        }
    }

    private fun sha256(input: String): String {
        val md = java.security.MessageDigest.getInstance("SHA-256")
        val bytes = md.digest(input.toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }
```

### Step 5: Verify compilation

**Run:**
```bash
./gradlew compileDebugKotlin
```

**Expected:** Build succeeds

### Step 6: Commit

```bash
git add app/src/main/java/com/healthcalendar/app/viewmodel/SettingsViewModel.kt
git commit -m "feat: add security state management to SettingsViewModel"
```

---

## Task 4: Enhanced Biometric Integration UI

**Files:**
- Modify: `app/src/main/java/com/healthcalendar/app/ui/screens/PasscodeLockScreen.kt:200-223`
- Add new composable after line 475

### Step 1: Add biometric retry button

**Replace the biometric icon section (around line 203-206) with:**

```kotlin
                            // Pulsing biometric icon with tap-to-retry (if enabled)
                            if (biometricEnabled && canBiometric) {
                                BiometricPromptButton(
                                    onBiometricClick = {
                                        // Trigger biometric prompt
                                        activity?.let {
                                            val biometricPrompt = BiometricPrompt(
                                                it,
                                                executor,
                                                object : BiometricPrompt.AuthenticationCallback() {
                                                    override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                                                        super.onAuthenticationSucceeded(result)
                                                        showSuccess = true
                                                        coroutineScope.launch {
                                                            delay(800)
                                                            onUnlocked()
                                                        }
                                                    }

                                                    override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                                                        super.onAuthenticationError(errorCode, errString)
                                                        error = when (errorCode) {
                                                            BiometricPrompt.ERROR_LOCKOUT ->
                                                                "Too many attempts. Try passcode."
                                                            BiometricPrompt.ERROR_LOCKOUT_PERMANENT ->
                                                                "Biometric locked. Use passcode."
                                                            BiometricPrompt.ERROR_NO_BIOMETRICS ->
                                                                "No biometric enrolled. Use passcode."
                                                            BiometricPrompt.ERROR_CANCELED -> null
                                                            BiometricPrompt.ERROR_USER_CANCELED -> null
                                                            BiometricPrompt.ERROR_NEGATIVE_BUTTON -> null
                                                            else -> "Biometric failed. Try again."
                                                        }
                                                    }

                                                    override fun onAuthenticationFailed() {
                                                        super.onAuthenticationFailed()
                                                        error = "Not recognized. Try again."
                                                    }
                                                }
                                            )
                                            biometricPrompt.authenticate(promptInfo)
                                        }
                                    }
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                            }
```

### Step 2: Create BiometricPromptButton composable

**Add after SuccessCheckmark composable (around line 475):**

```kotlin
/**
 * Tappable biometric prompt button with pulsing animation
 */
@Composable
private fun BiometricPromptButton(
    onBiometricClick: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        IconButton(
            onClick = onBiometricClick,
            modifier = Modifier.size(64.dp)
        ) {
            Icon(
                Icons.Filled.Fingerprint,
                contentDescription = "Use Biometric",
                modifier = Modifier
                    .size(48.dp)
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                        this.alpha = alpha
                    },
                tint = MaterialTheme.colorScheme.primary
            )
        }
        Text(
            "Tap to use biometric",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
```

### Step 3: Verify compilation

**Run:**
```bash
./gradlew compileDebugKotlin
```

**Expected:** Build succeeds

### Step 4: Test on device

**Run:**
```bash
rm -rf app/build && ./gradlew assembleDebug && adb install -r app/build/outputs/apk/debug/app-debug.apk
```

**Expected:** APK installs successfully. Test biometric button taps and error messages.

### Step 5: Commit

```bash
git add app/src/main/java/com/healthcalendar/app/ui/screens/PasscodeLockScreen.kt
git commit -m "feat: add tappable biometric button with error handling"
```

---

## Task 5: Failed Attempt Security with Lockout

**Files:**
- Modify: `app/src/main/java/com/healthcalendar/app/ui/screens/PasscodeLockScreen.kt:33-43`
- Add lockout UI components

### Step 1: Inject SecurityManager into PasscodeLockScreen

**Modify PasscodeLockScreen function signature (around line 30):**

```kotlin
@Composable
fun PasscodeLockScreen(
    settingsViewModel: SettingsViewModel,
    securityManager: SecurityManager = hiltViewModel(), // Add SecurityManager
    onUnlocked: () -> Unit,
    pinLength: Int = 6 // Configurable PIN length (4-6 digits)
) {
```

**Note:** This requires Hilt injection. You'll need to convert PasscodeLockScreen to receive SecurityManager via parameter from parent composable that can inject it.

**Alternative approach - get from context:**

```kotlin
@Composable
fun PasscodeLockScreen(
    settingsViewModel: SettingsViewModel,
    onUnlocked: () -> Unit,
    pinLength: Int = 6 // Configurable PIN length (4-6 digits)
) {
    val context = LocalContext.current
    // Note: SecurityManager needs to be provided via Hilt in parent or created here
    // For now, we'll implement lockout logic directly using preferences
```

### Step 2: Add lockout state checking

**Add after line 43 (after biometricEnabled):**

```kotlin
    val maxAttempts by settingsViewModel.maxFailedAttempts.collectAsState()
    val failedCount by settingsViewModel.failedAttemptCount.collectAsState()
    val remainingAttempts = (maxAttempts - failedCount).coerceAtLeast(0)

    // Check lockout status
    var isLockedOut by remember { mutableStateOf(false) }
    var lockoutSecondsRemaining by remember { mutableStateOf(0L) }

    LaunchedEffect(Unit) {
        while (true) {
            // Check lockout every second
            delay(1000)
            // TODO: Call SecurityManager.checkLockoutStatus()
            // For now, simple check based on failed attempts
            if (failedCount >= maxAttempts) {
                isLockedOut = true
                lockoutSecondsRemaining = (lockoutSecondsRemaining - 1).coerceAtLeast(0)
                if (lockoutSecondsRemaining <= 0) {
                    isLockedOut = false
                }
            }
        }
    }
```

### Step 3: Add lockout UI

**Add before the floating card (around line 175):**

```kotlin
                // Lockout overlay
                if (isLockedOut) {
                    LockoutOverlay(
                        remainingSeconds = lockoutSecondsRemaining,
                        onDismiss = { /* Cannot dismiss */ }
                    )
                } else {
                    // Existing floating card code...
                }
```

### Step 4: Create LockoutOverlay composable

**Add new composable after BiometricPromptButton:**

```kotlin
/**
 * Lockout overlay shown when too many failed attempts
 */
@Composable
private fun LockoutOverlay(
    remainingSeconds: Long,
    onDismiss: () -> Unit
) {
    val minutes = remainingSeconds / 60
    val seconds = remainingSeconds % 60

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.95f)),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .padding(24.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.errorContainer
            )
        ) {
            Column(
                modifier = Modifier
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Icon(
                    Icons.Filled.Lock,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.error
                )

                Text(
                    "Too Many Attempts",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )

                Text(
                    "Please wait before trying again",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )

                // Countdown timer
                Surface(
                    modifier = Modifier.size(120.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.error
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                String.format("%02d:%02d", minutes, seconds),
                                style = MaterialTheme.typography.headlineLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onError
                            )
                            Text(
                                "remaining",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onError.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }
        }
    }
}
```

### Step 5: Handle failed attempts in passcode validation

**Modify the passcode validation logic (around line 268):**

```kotlin
                                        if (input.length == validPinLength) {
                                            val enteredHash = sha256(input)
                                            if (passHash != null && passHash == enteredHash) {
                                                // Success
                                                coroutineScope.launch {
                                                    settingsViewModel.preferencesRepository.resetFailedAttempts()
                                                }
                                                showSuccess = true
                                                coroutineScope.launch {
                                                    delay(800)
                                                    onUnlocked()
                                                }
                                            } else {
                                                // Failed
                                                coroutineScope.launch {
                                                    settingsViewModel.preferencesRepository.incrementFailedAttempts()
                                                }
                                                error = "Incorrect passcode ($remainingAttempts attempts left)"
                                                input = ""

                                                // Check if should lockout
                                                if (remainingAttempts <= 0) {
                                                    isLockedOut = true
                                                    lockoutSecondsRemaining = 30L // Start with 30 seconds
                                                }
                                            }
                                        }
```

### Step 6: Build and test

**Run:**
```bash
./gradlew assembleDebug && adb install -r app/build/outputs/apk/debug/app-debug.apk
```

**Expected:**
- Enter wrong passcode 5 times
- Lockout overlay appears with countdown
- Cannot enter passcode until countdown finishes

### Step 7: Commit

```bash
git add app/src/main/java/com/healthcalendar/app/ui/screens/PasscodeLockScreen.kt
git commit -m "feat: add failed attempt tracking with lockout timer"
```

---

## Task 6: Emergency Access - Forgot Passcode

**Files:**
- Modify: `app/src/main/java/com/healthcalendar/app/ui/screens/PasscodeLockScreen.kt`
- Add emergency access UI

### Step 1: Add "Forgot Passcode?" button

**Add below the numeric keypad (around line 290):**

```kotlin
                        // Forgot passcode button
                        TextButton(
                            onClick = { showEmergencyAccess = true },
                            modifier = Modifier.padding(top = 16.dp)
                        ) {
                            Text(
                                "Forgot Passcode?",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
```

### Step 2: Add emergency access state

**Add after showSuccess state (around line 92):**

```kotlin
    var showEmergencyAccess by remember { mutableStateOf(false) }
```

### Step 3: Create EmergencyAccessDialog composable

**Add new composable after LockoutOverlay:**

```kotlin
/**
 * Emergency access dialog for password recovery
 */
@Composable
private fun EmergencyAccessDialog(
    securityQuestion: String?,
    recoveryEmail: String?,
    onVerifyAnswer: (String) -> Boolean,
    onClearData: () -> Unit,
    onDismiss: () -> Unit
) {
    var selectedOption by remember { mutableStateOf<EmergencyOption?>(null) }
    var answerInput by remember { mutableStateOf("") }
    var verificationError by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Emergency Access",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                when (selectedOption) {
                    EmergencyOption.SECURITY_QUESTION -> {
                        if (securityQuestion != null) {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(
                                    securityQuestion,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Medium
                                )

                                OutlinedTextField(
                                    value = answerInput,
                                    onValueChange = {
                                        answerInput = it
                                        verificationError = null
                                    },
                                    label = { Text("Your Answer") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth(),
                                    isError = verificationError != null
                                )

                                verificationError?.let {
                                    Text(
                                        it,
                                        color = MaterialTheme.colorScheme.error,
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }
                        } else {
                            Text(
                                "No security question set up.",
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }

                    EmergencyOption.EMAIL_RECOVERY -> {
                        if (recoveryEmail != null) {
                            Text("Recovery instructions will be sent to:\n$recoveryEmail")
                            // TODO: Implement email sending
                        } else {
                            Text(
                                "No recovery email set up.",
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }

                    EmergencyOption.CLEAR_DATA -> {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                "⚠️ WARNING",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.error
                            )
                            Text(
                                "This will permanently delete all your health data, appointments, medications, and documents. This action cannot be undone.",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }

                    null -> {
                        // Show options
                        Text(
                            "Choose a recovery method:",
                            style = MaterialTheme.typography.bodyMedium
                        )

                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            if (securityQuestion != null) {
                                OutlinedButton(
                                    onClick = { selectedOption = EmergencyOption.SECURITY_QUESTION },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(Icons.Filled.Lock, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Answer Security Question")
                                }
                            }

                            if (recoveryEmail != null) {
                                OutlinedButton(
                                    onClick = { selectedOption = EmergencyOption.EMAIL_RECOVERY },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(Icons.Filled.Lock, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Email Recovery")
                                }
                            }

                            OutlinedButton(
                                onClick = { selectedOption = EmergencyOption.CLEAR_DATA },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = MaterialTheme.colorScheme.error
                                )
                            ) {
                                Icon(Icons.Filled.Lock, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Clear All Data")
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            when (selectedOption) {
                EmergencyOption.SECURITY_QUESTION -> {
                    TextButton(
                        onClick = {
                            if (onVerifyAnswer(answerInput)) {
                                // Success - reset passcode
                                onDismiss()
                            } else {
                                verificationError = "Incorrect answer"
                            }
                        }
                    ) {
                        Text("Verify")
                    }
                }
                EmergencyOption.EMAIL_RECOVERY -> {
                    TextButton(onClick = { /* TODO: Send email */ }) {
                        Text("Send Email")
                    }
                }
                EmergencyOption.CLEAR_DATA -> {
                    TextButton(
                        onClick = {
                            onClearData()
                            onDismiss()
                        },
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text("Clear Data")
                    }
                }
                null -> {}
            }
        },
        dismissButton = {
            TextButton(onClick = {
                if (selectedOption != null) {
                    selectedOption = null
                } else {
                    onDismiss()
                }
            }) {
                Text(if (selectedOption != null) "Back" else "Cancel")
            }
        }
    )
}

private enum class EmergencyOption {
    SECURITY_QUESTION,
    EMAIL_RECOVERY,
    CLEAR_DATA
}
```

### Step 4: Add dialog to PasscodeLockScreen

**Add before the closing Box (around end of PasscodeLockScreen function):**

```kotlin
    // Emergency access dialog
    if (showEmergencyAccess) {
        val securityQuestion by settingsViewModel.settingsState.collectAsState()

        EmergencyAccessDialog(
            securityQuestion = securityQuestion.securityQuestion,
            recoveryEmail = securityQuestion.recoveryEmail,
            onVerifyAnswer = { answer ->
                // TODO: Verify with SecurityManager
                false
            },
            onClearData = {
                // TODO: Clear all app data
            },
            onDismiss = { showEmergencyAccess = false }
        )
    }
```

### Step 5: Add import for Icons

**Add to imports:**

```kotlin
import androidx.compose.material.icons.filled.Email
```

### Step 6: Build and test

**Run:**
```bash
./gradlew assembleDebug && adb install -r app/build/outputs/apk/debug/app-debug.apk
```

**Expected:**
- Tap "Forgot Passcode?"
- See emergency access options
- Navigate through security question flow

### Step 7: Commit

```bash
git add app/src/main/java/com/healthcalendar/app/ui/screens/PasscodeLockScreen.kt
git commit -m "feat: add emergency access dialog with recovery options"
```

---

## Task 7: Passcode Length Selection UI

**Files:**
- Create: `app/src/main/java/com/healthcalendar/app/ui/screens/settings/SecuritySettingsScreen.kt`

### Step 1: Create SecuritySettingsScreen composable

**Create new file `app/src/main/java/com/healthcalendar/app/ui/screens/settings/SecuritySettingsScreen.kt`:**

```kotlin
package com.healthcalendar.app.ui.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.healthcalendar.app.viewmodel.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SecuritySettingsScreen(
    settingsViewModel: SettingsViewModel,
    onNavigateBack: () -> Unit
) {
    val settingsState by settingsViewModel.settingsState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Security Settings") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Filled.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Passcode Length Section
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        "Passcode Settings",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        "Passcode Length",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(4, 6, 8).forEach { length ->
                            FilterChip(
                                selected = settingsState.passcodeLength == length,
                                onClick = { settingsViewModel.setPasscodeLength(length) },
                                label = { Text("$length digits") }
                            )
                        }
                    }

                    Divider()

                    // Alphanumeric option
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Alphanumeric Password",
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Text(
                                "Use letters and numbers",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = settingsState.allowAlphanumeric,
                            onCheckedChange = { settingsViewModel.setAllowAlphanumeric(it) }
                        )
                    }
                }
            }

            // Auto-Lock Settings
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        "Auto-Lock",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Enable Auto-Lock")
                        Switch(
                            checked = settingsState.autoLockEnabled,
                            onCheckedChange = { settingsViewModel.setAutoLockEnabled(it) }
                        )
                    }

                    if (settingsState.autoLockEnabled) {
                        Text(
                            "Auto-Lock Timeout",
                            style = MaterialTheme.typography.bodyMedium
                        )

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            listOf(
                                0 to "Immediate",
                                1 to "1 min",
                                5 to "5 min",
                                15 to "15 min",
                                30 to "30 min"
                            ).forEach { (minutes, label) ->
                                FilterChip(
                                    selected = settingsState.autoLockTimeoutMinutes == minutes,
                                    onClick = { settingsViewModel.setAutoLockTimeout(minutes) },
                                    label = { Text(label) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }

                        Divider()

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Lock on Screen Off")
                            Switch(
                                checked = settingsState.lockOnScreenOff,
                                onCheckedChange = { settingsViewModel.setLockOnScreenOff(it) }
                            )
                        }
                    }
                }
            }

            // Failed Attempts Settings
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        "Failed Attempts",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        "Maximum Failed Attempts",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(3, 5, 10).forEach { max ->
                            FilterChip(
                                selected = settingsState.maxFailedAttempts == max,
                                onClick = { settingsViewModel.setMaxFailedAttempts(max) },
                                label = { Text("$max attempts") }
                            )
                        }
                    }

                    Text(
                        "After exceeding limit, temporary lockout increases progressively: 30s → 1min → 5min → 15min",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Emergency Access Settings
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        "Emergency Access",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    OutlinedButton(
                        onClick = { /* TODO: Open security question dialog */ },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Filled.Lock, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            if (settingsState.securityQuestion != null)
                                "Change Security Question"
                            else
                                "Set Security Question"
                        )
                    }

                    OutlinedButton(
                        onClick = { /* TODO: Open recovery email dialog */ },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Filled.Email, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            if (settingsState.recoveryEmail != null)
                                "Change Recovery Email"
                            else
                                "Set Recovery Email"
                        )
                    }

                    if (settingsState.securityQuestion != null || settingsState.recoveryEmail != null) {
                        Text(
                            "Emergency access methods let you recover if you forget your passcode",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}
```

### Step 2: Verify compilation

**Run:**
```bash
./gradlew compileDebugKotlin
```

**Expected:** Build succeeds

### Step 3: Commit

```bash
git add app/src/main/java/com/healthcalendar/app/ui/screens/settings/SecuritySettingsScreen.kt
git commit -m "feat: add security settings screen with all options"
```

---

## Task 8: Update PasscodeLockScreen to Use Dynamic Length

**Files:**
- Modify: `app/src/main/java/com/healthcalendar/app/ui/screens/PasscodeLockScreen.kt:33-43`
- Modify: `app/src/main/java/com/healthcalendar/app/ui/screens/PasscodeLockScreen.kt:97-107`

### Step 1: Remove hardcoded PIN length parameter

**Modify PasscodeLockScreen signature (line 33):**

```kotlin
@Composable
fun PasscodeLockScreen(
    settingsViewModel: SettingsViewModel,
    onUnlocked: () -> Unit
) {
    val passHash by settingsViewModel.passcodeHash.collectAsState()
    val passcodeLength by settingsViewModel.passcodeLength.collectAsState()
    var input by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current
    val biometricEnabled by settingsViewModel.biometricEnabled.collectAsState()

    // Validate PIN length (4-8 digits)
    val validPinLength = passcodeLength.coerceIn(4, 8)
```

### Step 2: Update PinDotsDisplay to support 4-8 dots

**Modify PinDotsDisplay usage (around line 103):**

```kotlin
                                PinDotsDisplay(
                                    pinLength = input.length,
                                    maxLength = validPinLength,
                                    hasError = error != null
                                )
```

### Step 3: Build and test

**Run:**
```bash
./gradlew assembleDebug && adb install -r app/build/outputs/apk/debug/app-debug.apk
```

**Expected:**
- Change passcode length in settings
- PIN dots adjust accordingly
- Passcode validation uses new length

### Step 4: Commit

```bash
git add app/src/main/java/com/healthcalendar/app/ui/screens/PasscodeLockScreen.kt
git commit -m "feat: use dynamic passcode length from settings"
```

---

## Task 9: Integration Testing and Polish

**Files:**
- All modified files

### Step 1: Test all features

**Manual test checklist:**

1. **Biometric Integration**
   - [ ] Tap biometric button triggers prompt
   - [ ] Error messages display correctly
   - [ ] Success unlocks app

2. **Auto-Lock**
   - [ ] Change timeout settings
   - [ ] Background app and verify lock timing
   - [ ] Screen off triggers lock

3. **Failed Attempts**
   - [ ] Enter wrong passcode 5 times
   - [ ] Lockout timer displays
   - [ ] Cannot enter passcode during lockout
   - [ ] Timer counts down correctly
   - [ ] Lockout clears after time expires

4. **Emergency Access**
   - [ ] Tap "Forgot Passcode?"
   - [ ] Security question flow works
   - [ ] Clear data warning displays

5. **Passcode Length**
   - [ ] Change from 6 to 4 digits
   - [ ] PIN dots update
   - [ ] Change from 4 to 8 digits
   - [ ] Validation works for all lengths

### Step 2: Fix any issues found

Document issues and create follow-up commits.

### Step 3: Final build and test

**Run:**
```bash
rm -rf app/build && ./gradlew assembleDebug && adb install -r app/build/outputs/apk/debug/app-debug.apk
```

### Step 4: Update documentation

**Create `docs/SECURITY_FEATURES.md`:**

```markdown
# Security Features

## Overview
Health Calendar includes comprehensive security features to protect your health data.

## Features

### Passcode Lock
- 4, 6, or 8 digit PIN codes
- Alphanumeric passwords (optional)
- Biometric authentication (fingerprint/face)

### Auto-Lock
- Immediate lock when app backgrounded
- Configurable timeouts: 1, 5, 15, 30 minutes
- Lock on screen off option
- Remember unlock for X minutes

### Failed Attempt Protection
- Configurable max attempts: 3, 5, or 10
- Progressive lockout: 30s → 1min → 5min → 15min → 30min
- Option to require biometric after failures

### Emergency Access
- Security question recovery
- Email verification (future)
- Clear all data option (with warning)

## Usage

### Setting Up Security
1. Go to Settings → Security
2. Enable Passcode
3. Choose passcode length
4. Set up emergency recovery methods

### Recovering Access
If you forget your passcode:
1. Tap "Forgot Passcode?" on lock screen
2. Choose recovery method:
   - Answer security question
   - Request email reset (if configured)
   - Clear all data (last resort)

## Technical Details

### Data Storage
- Passcodes: SHA-256 hashed, stored in DataStore
- Security answers: SHA-256 hashed
- Biometric: Uses Android BiometricPrompt API

### Lockout Algorithm
```
Attempts 1-5: No lockout
Attempt 6: 30 second lockout
Attempt 7: 1 minute lockout
Attempt 8: 5 minute lockout
Attempt 9: 15 minute lockout
Attempt 10+: 30 minute lockout
```
```

### Step 5: Final commit

```bash
git add docs/SECURITY_FEATURES.md
git commit -m "docs: add security features documentation"
git push
```

---

## Verification Checklist

Before marking complete, verify:

- [ ] All new preferences save and load correctly
- [ ] SecurityManager business logic handles edge cases
- [ ] Biometric button works on devices with/without biometric
- [ ] Lockout timer counts down accurately
- [ ] Emergency access dialog navigates correctly
- [ ] Passcode length changes take effect immediately
- [ ] Settings UI displays current values correctly
- [ ] Dark mode styling looks good for all new components
- [ ] Haptic feedback works on all buttons
- [ ] No crashes or ANRs during testing
- [ ] Memory leaks checked with LeakCanary (if available)
- [ ] Code follows Kotlin/Compose best practices

---

## Known Limitations / Future Work

1. **Email Recovery**: Currently placeholder - needs backend integration
2. **SMS Verification**: Not implemented - requires phone permissions
3. **Pattern Lock**: Not included - could be added as alternative
4. **Biometric Fallback**: Currently only supports passcode fallback
5. **Auto-Lock Timers**: Needs lifecycle-aware implementation
6. **Unit Tests**: Manual testing only - should add automated tests
7. **Accessibility**: Should add TalkBack descriptions
8. **Localization**: All strings hardcoded - should use string resources

---

## Additional Notes for Engineer

### YAGNI Principle Applied
We've implemented only requested features. Avoided:
- Complex biometric APIs (only using BiometricPrompt)
- Custom pattern lock UI (not requested)
- Cloud backup of security settings
- Multi-user support

### DRY Violations to Fix (Future)
- SHA-256 helper duplicated in 3 places (SecurityManager, SettingsViewModel, PasscodeLockScreen)
- Should extract to `SecurityUtils.kt`

### Testing Approach
Since Android UI testing framework isn't set up:
- Manual testing on real device required
- Focus on edge cases (network issues, low memory, interruptions)
- Test on different Android versions (API 26+)

### Performance Considerations
- DataStore reads are Flow-based (efficient)
- Lockout timer uses 1-second polling (acceptable for this use case)
- Biometric prompt has slight delay (Android API limitation)
- Consider adding splash screen if unlock check takes >300ms

---

**Estimated Implementation Time:** 4-6 hours
**Complexity:** Medium-High
**Risk Level:** Medium (touching security-critical code)
**Testing Priority:** High (comprehensive manual testing required)
