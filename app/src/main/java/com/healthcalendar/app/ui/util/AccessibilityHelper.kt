package com.healthcalendar.app.ui.util

import android.content.Context
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityManager
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Accessibility utilities for the Health Calendar app
 */
object AccessibilityHelper {
    
    /**
     * Minimum touch target size for accessibility (48dp x 48dp)
     */
    val MINIMUM_TOUCH_TARGET = 48.dp
    
    /**
     * Recommended touch target size for important actions (56dp x 56dp)
     */
    val RECOMMENDED_TOUCH_TARGET = 56.dp
    
    /**
     * Check if TalkBack or other accessibility services are enabled
     */
    fun isAccessibilityEnabled(context: Context): Boolean {
        val accessibilityManager = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as? AccessibilityManager
        return accessibilityManager?.isEnabled == true
    }
    
    /**
     * Announce a message to screen readers
     */
    fun announce(context: Context, message: String) {
        val accessibilityManager = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as? AccessibilityManager
        if (accessibilityManager?.isEnabled == true) {
            val event = AccessibilityEvent.obtain().apply {
                eventType = AccessibilityEvent.TYPE_ANNOUNCEMENT
                text.add(message)
            }
            accessibilityManager.sendAccessibilityEvent(event)
        }
    }
}

/**
 * Modifier to ensure minimum touch target size for accessibility
 */
fun Modifier.minimumTouchTarget(
    size: Dp = AccessibilityHelper.MINIMUM_TOUCH_TARGET
): Modifier = this.then(
    Modifier.sizeIn(minWidth = size, minHeight = size)
)

/**
 * Modifier for recommended touch target size for important actions
 */
fun Modifier.recommendedTouchTarget(): Modifier = this.then(
    Modifier.size(AccessibilityHelper.RECOMMENDED_TOUCH_TARGET)
)

/**
 * Add semantic content description and role for accessibility
 */
fun Modifier.accessibilityLabel(
    label: String,
    role: Role = Role.Button
): Modifier = this.semantics {
    contentDescription = label
    this.role = role
}

/**
 * Composable to announce messages to screen readers
 */
@Composable
fun rememberAccessibilityAnnouncer(): (String) -> Unit {
    val context = LocalContext.current
    return remember {
        { message: String ->
            AccessibilityHelper.announce(context, message)
        }
    }
}

/**
 * Format medication description for screen readers
 */
fun formatMedicationForAccessibility(
    name: String,
    amount: String,
    dosage: String,
    unit: String,
    form: String
): String {
    return buildString {
        append(name)
        append(", take ")
        append(amount)
        if (amount != "1") append(" tablets")
        else append(" tablet")
        append(" of ")
        append(dosage)
        append(" ")
        append(unit)
        append(" ")
        append(form)
    }
}

/**
 * Format appointment description for screen readers
 */
fun formatAppointmentForAccessibility(
    title: String,
    date: String,
    time: String,
    location: String? = null
): String {
    return buildString {
        append(title)
        append(" on ")
        append(date)
        append(" at ")
        append(time)
        if (!location.isNullOrEmpty()) {
            append(" at ")
            append(location)
        }
    }
}

/**
 * Format medication log status for screen readers
 */
fun formatMedicationStatusForAccessibility(
    medicationName: String,
    status: String,
    time: String
): String {
    return when (status.lowercase()) {
        "taken" -> "$medicationName marked as taken at $time"
        "skipped" -> "$medicationName marked as skipped at $time"
        "missed" -> "$medicationName was missed at $time"
        "pending" -> "$medicationName is pending at $time"
        else -> "$medicationName status is $status at $time"
    }
}

/**
 * Format time for screen readers (more natural language)
 */
fun formatTimeForAccessibility(hour: Int, minute: Int): String {
    val amPm = if (hour < 12) "AM" else "PM"
    val hour12 = when (hour) {
        0 -> 12
        in 1..12 -> hour
        else -> hour - 12
    }
    val minuteStr = if (minute == 0) "" else " $minute"
    return "$hour12$minuteStr $amPm"
}

/**
 * Format date for screen readers (more natural language)
 */
fun formatDateForAccessibility(day: Int, month: Int, year: Int): String {
    val monthNames = listOf(
        "January", "February", "March", "April", "May", "June",
        "July", "August", "September", "October", "November", "December"
    )
    return "${monthNames[month - 1]} $day, $year"
}

/**
 * Get adherence percentage description for screen readers
 */
fun formatAdherenceForAccessibility(percentage: Double): String {
    return when {
        percentage >= 90 -> "Excellent adherence at ${percentage.toInt()} percent"
        percentage >= 75 -> "Good adherence at ${percentage.toInt()} percent"
        percentage >= 50 -> "Fair adherence at ${percentage.toInt()} percent"
        else -> "Low adherence at ${percentage.toInt()} percent, consider reviewing your schedule"
    }
}
