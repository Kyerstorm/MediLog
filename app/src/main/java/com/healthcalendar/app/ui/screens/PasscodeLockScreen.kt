package com.healthcalendar.app.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.healthcalendar.app.viewmodel.SettingsViewModel
import androidx.fragment.app.FragmentActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import java.util.concurrent.Executor

@Composable
fun PasscodeLockScreen(
    settingsViewModel: SettingsViewModel,
    onUnlocked: () -> Unit,
    pinLength: Int = 6 // Configurable PIN length (4-6 digits)
) {
    val passHash by settingsViewModel.passcodeHash.collectAsState()
    var input by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current
    val biometricEnabled by settingsViewModel.biometricEnabled.collectAsState()

    // Validate PIN length
    val validPinLength = pinLength.coerceIn(4, 6)

    // Check if biometric authentication is available
    val bm = BiometricManager.from(context)
    val canBiometric = try { bm.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG) == BiometricManager.BIOMETRIC_SUCCESS } catch (e: Exception) { false }

    // Setup biometric authentication components
    val activity = (context as? FragmentActivity)
    val executor: Executor = ContextCompat.getMainExecutor(context)
    val promptInfo = BiometricPrompt.PromptInfo.Builder()
        .setTitle("Unlock ${context.applicationInfo.loadLabel(context.packageManager)}")
        .setSubtitle("Use biometric to unlock")
        .setNegativeButtonText("Use passcode")
        .build()

    // Success state for animation
    var showSuccess by remember { mutableStateOf(false) }

    // Coroutine scope for async operations
    val coroutineScope = rememberCoroutineScope()

    // Fade-in animation state
    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        isVisible = true
    }

    // Dark mode detection
    val isDark = isSystemInDarkTheme()

    // Gradient background colors
    val gradientColors = if (isDark) {
        listOf(
            Color(0xFF000000), // Pure black for OLED
            Color(0xFF1A1A1A),
            Color(0xFF0D1B2A)
        )
    } else {
        listOf(
            MaterialTheme.colorScheme.primary.copy(alpha = 0.05f),
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f),
            MaterialTheme.colorScheme.background
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = gradientColors
                )
            )
    ) {
        AnimatedVisibility(
            visible = isVisible,
            enter = fadeIn(
                animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing)
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(48.dp))

                // App Logo and Title Section
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(0.3f),
                    verticalArrangement = Arrangement.Center
                ) {
                    // App Icon/Logo
                    Surface(
                        modifier = Modifier.size(80.dp),
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shadowElevation = 8.dp
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Filled.Lock,
                                contentDescription = "App Icon",
                                modifier = Modifier.size(40.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        "Health Calendar",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }

                // Floating Card with PIN Entry
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    shape = RoundedCornerShape(32.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isDark) {
                            Color(0xFF1C1C1E) // Dark card for OLED
                        } else {
                            MaterialTheme.colorScheme.surface
                        }
                    ),
                    elevation = CardDefaults.cardElevation(
                        defaultElevation = 16.dp
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        verticalArrangement = Arrangement.SpaceEvenly,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Title and biometric icon
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
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

                            Text(
                                "Enter Passcode",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                "Unlock to continue",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Success checkmark or PIN dots
                        Box(
                            modifier = Modifier.height(80.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            if (showSuccess) {
                                SuccessCheckmark()
                            } else {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    PinDotsDisplay(
                                        pinLength = input.length,
                                        maxLength = validPinLength,
                                        hasError = error != null
                                    )

                                    Spacer(modifier = Modifier.height(16.dp))

                                    error?.let {
                                        Text(
                                            it,
                                            color = MaterialTheme.colorScheme.error,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }
                        }

                        // Custom numeric keypad
                        if (!showSuccess) {
                            NumericKeypad(
                                onNumberClick = { digit ->
                                    if (input.length < validPinLength) {
                                        input += digit
                                        error = null

                                        // Auto-submit when all digits entered
                                        if (input.length == validPinLength) {
                                            val enteredHash = sha256(input)
                                            if (passHash != null && passHash == enteredHash) {
                                                showSuccess = true
                                                // Delay before unlocking to show animation
                                                coroutineScope.launch {
                                                    delay(800)
                                                    onUnlocked()
                                                }
                                            } else {
                                                error = "Incorrect passcode"
                                                input = ""
                                            }
                                        }
                                    }
                                },
                                onBackspaceClick = {
                                    if (input.isNotEmpty()) {
                                        input = input.dropLast(1)
                                        error = null
                                    }
                                },
                                isDarkMode = isDark
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

/**
 * PIN dots display component showing filled/empty circles for entered digits
 * with animations for filling and error states
 */
@Composable
private fun PinDotsDisplay(
    pinLength: Int,
    maxLength: Int,
    hasError: Boolean
) {
    // Shake animation on error
    var shouldShake by remember { mutableStateOf(false) }
    val shakeOffset by animateFloatAsState(
        targetValue = if (shouldShake) 0f else 1f,
        animationSpec = if (shouldShake) {
            repeatable(
                iterations = 4,
                animation = tween(durationMillis = 50, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            )
        } else {
            tween(durationMillis = 0)
        },
        finishedListener = { shouldShake = false },
        label = "shake"
    )

    // Trigger shake on error
    LaunchedEffect(hasError) {
        if (hasError) {
            shouldShake = true
        }
    }

    Row(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.graphicsLayer {
            translationX = if (shouldShake) (shakeOffset - 0.5f) * 20f else 0f
        }
    ) {
        repeat(maxLength) { index ->
            AnimatedPinDot(
                isFilled = index < pinLength,
                hasError = hasError,
                index = index
            )
        }
    }
}

/**
 * Individual animated PIN dot with scale and fade animations
 */
@Composable
private fun AnimatedPinDot(
    isFilled: Boolean,
    hasError: Boolean,
    index: Int
) {
    // Scale animation when filling
    val scale by animateFloatAsState(
        targetValue = if (isFilled) 1f else 0.8f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "scale"
    )

    // Color animation
    val color by animateColorAsState(
        targetValue = when {
            hasError -> MaterialTheme.colorScheme.error
            isFilled -> MaterialTheme.colorScheme.primary
            else -> MaterialTheme.colorScheme.surfaceVariant
        },
        animationSpec = tween(durationMillis = 200),
        label = "color"
    )

    Surface(
        modifier = Modifier
            .size(16.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            },
        shape = CircleShape,
        color = color,
        border = if (!isFilled && !hasError) {
            androidx.compose.foundation.BorderStroke(
                2.dp,
                MaterialTheme.colorScheme.outline
            )
        } else null
    ) {}
}

/**
 * Pulsing biometric icon with infinite scale animation
 */
@Composable
private fun PulsingBiometricIcon() {
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

    Icon(
        Icons.Filled.Fingerprint,
        contentDescription = "Biometric Authentication",
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

/**
 * Success checkmark animation with scale and fade-in
 */
@Composable
private fun SuccessCheckmark() {
    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        isVisible = true
    }

    AnimatedVisibility(
        visible = isVisible,
        enter = scaleIn(
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMedium
            )
        ) + fadeIn(
            animationSpec = tween(durationMillis = 300)
        )
    ) {
        Surface(
            modifier = Modifier.size(64.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primary
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    Icons.Filled.CheckCircle,
                    contentDescription = "Success",
                    modifier = Modifier.size(40.dp),
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    }
}

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

/**
 * Custom numeric keypad with circular buttons in 3x4 grid
 * Layout: 1-9, with 0 and backspace on bottom row
 */
@Composable
private fun NumericKeypad(
    onNumberClick: (String) -> Unit,
    onBackspaceClick: () -> Unit,
    isDarkMode: Boolean = false,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Rows 1-3: digits 1-9
        listOf(
            listOf("1", "2", "3"),
            listOf("4", "5", "6"),
            listOf("7", "8", "9")
        ).forEach { row ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                row.forEach { digit ->
                    NumericButton(
                        text = digit,
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onNumberClick(digit)
                        },
                        isDarkMode = isDarkMode
                    )
                }
            }
        }

        // Bottom row: 0, empty, backspace
        Row(
            horizontalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Zero button (left position)
            NumericButton(
                text = "0",
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onNumberClick("0")
                },
                isDarkMode = isDarkMode
            )

            // Empty spacer for alignment (middle position)
            Spacer(modifier = Modifier.size(72.dp))

            // Backspace button (right position)
            IconButton(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onBackspaceClick()
                },
                modifier = Modifier.size(72.dp)
            ) {
                Icon(
                    Icons.Filled.Backspace,
                    contentDescription = "Backspace",
                    modifier = Modifier.size(32.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * Individual circular button for numeric keypad with enhanced styling
 */
@Composable
private fun NumericButton(
    text: String,
    onClick: () -> Unit,
    isDarkMode: Boolean = false
) {
    FilledTonalButton(
        onClick = onClick,
        modifier = Modifier
            .size(72.dp)
            .clip(CircleShape),
        shape = CircleShape,
        contentPadding = PaddingValues(0.dp),
        colors = ButtonDefaults.filledTonalButtonColors(
            containerColor = if (isDarkMode) {
                Color(0xFF2C2C2E) // Darker button for OLED
            } else {
                MaterialTheme.colorScheme.secondaryContainer
            },
            contentColor = if (isDarkMode) {
                Color(0xFFFFFFFF) // High contrast white text
            } else {
                MaterialTheme.colorScheme.onSecondaryContainer
            }
        ),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 2.dp,
            pressedElevation = 8.dp
        )
    ) {
        Text(
            text = text,
            fontSize = 28.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

// local helper
private fun sha256(input: String): String {
    val md = java.security.MessageDigest.getInstance("SHA-256")
    val bytes = md.digest(input.toByteArray(Charsets.UTF_8))
    return bytes.joinToString("") { "%02x".format(it) }
}
