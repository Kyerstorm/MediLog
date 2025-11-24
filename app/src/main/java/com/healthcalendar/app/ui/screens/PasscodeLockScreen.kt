package com.healthcalendar.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.healthcalendar.app.viewmodel.SettingsViewModel
import android.content.Context
import androidx.fragment.app.FragmentActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.compose.ui.platform.LocalContext
import java.util.concurrent.Executor

@Composable
fun PasscodeLockScreen(
    settingsViewModel: SettingsViewModel,
    onUnlocked: () -> Unit
) {
    val passHash by settingsViewModel.passcodeHash.collectAsState()
    var input by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current
    val biometricEnabled by settingsViewModel.biometricEnabled.collectAsState()

    // Attempt biometric authentication automatically if enabled and available
    val bm = BiometricManager.from(context)
    val canBiometric = try { bm.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG) == BiometricManager.BIOMETRIC_SUCCESS } catch (e: Exception) { false }

    if (biometricEnabled && canBiometric) {
    val activity = (context as? FragmentActivity)
        val executor: Executor = ContextCompat.getMainExecutor(context)
        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Unlock ${context.applicationInfo.loadLabel(context.packageManager)}")
            .setSubtitle("Use biometric to unlock")
            .setNegativeButtonText("Use passcode")
            .build()

        LaunchedEffect(Unit) {
            try {
                activity?.let {
                    val biometricPrompt = BiometricPrompt(it, executor, object : BiometricPrompt.AuthenticationCallback() {
                        override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                            super.onAuthenticationSucceeded(result)
                            onUnlocked()
                        }

                        override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                            super.onAuthenticationError(errorCode, errString)
                            // ignore and fallback to passcode
                        }
                    })
                    biometricPrompt.authenticate(promptInfo)
                }
            } catch (e: Exception) {
                // fallback to passcode UI
            }
        }
    }

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Enter Passcode", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = input,
                onValueChange = { input = it.filter { ch -> ch.isDigit() }; error = null },
                label = { Text("Passcode") },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.NumberPassword)
            )
            error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = {
                val enteredHash = sha256(input)
                if (passHash != null && passHash == enteredHash) {
                    onUnlocked()
                } else {
                    error = "Incorrect passcode"
                }
            }) {
                Text("Unlock")
            }
        }
    }
}

// local helper
private fun sha256(input: String): String {
    val md = java.security.MessageDigest.getInstance("SHA-256")
    val bytes = md.digest(input.toByteArray(Charsets.UTF_8))
    return bytes.joinToString("") { "%02x".format(it) }
}
