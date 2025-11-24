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
