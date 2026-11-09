package com.example.util

import androidx.compose.ui.graphics.Color
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

enum class DeadlineBadgeSeverity {
    CRITICAL,
    WARNING,
    UPCOMING,
    OVERDUE,
    NEUTRAL
}

data class DeadlineBadgeInfo(
    val label: String,
    val severity: DeadlineBadgeSeverity,
    val daysRemaining: Int?
)

object DateUtils {

    private val supportedFormats = listOf(
        "MMM dd, yyyy",
        "MMM d, yyyy",
        "MMMM dd, yyyy",
        "MMMM d, yyyy",
        "yyyy-MM-dd",
        "dd MMM yyyy",
        "d MMM yyyy",
        "dd/MM/yyyy",
        "MM/dd/yyyy"
    )

    fun parseDate(dateStr: String): Date? {
        if (dateStr.isBlank()) return null
        val trimmed = dateStr.trim()
        for (pattern in supportedFormats) {
            try {
                val sdf = SimpleDateFormat(pattern, Locale.ENGLISH)
                sdf.isLenient = false
                val date = sdf.parse(trimmed)
                if (date != null) return date
            } catch (_: Exception) {
                // Try next pattern
            }
        }
        return null
    }

    /**
     * Calculates days difference: (targetDate - today).
     * If date cannot be parsed, returns null.
     */
    fun getDaysRemaining(dateStr: String): Int? {
        val target = parseDate(dateStr) ?: return null
        val calendarNow = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val calendarTarget = Calendar.getInstance().apply {
            time = target
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val diffMillis = calendarTarget.timeInMillis - calendarNow.timeInMillis
        return TimeUnit.MILLISECONDS.toDays(diffMillis).toInt()
    }

    fun getBadgeInfo(dateStr: String): DeadlineBadgeInfo {
        val days = getDaysRemaining(dateStr)
        return when {
            days == null -> {
                DeadlineBadgeInfo(
                    label = if (dateStr.isNotBlank()) dateStr else "No date",
                    severity = DeadlineBadgeSeverity.NEUTRAL,
                    daysRemaining = null
                )
            }
            days < 0 -> {
                val overdueDays = -days
                DeadlineBadgeInfo(
                    label = if (overdueDays == 1) "1 day overdue" else "$overdueDays days overdue",
                    severity = DeadlineBadgeSeverity.OVERDUE,
                    daysRemaining = days
                )
            }
            days == 0 -> {
                DeadlineBadgeInfo(
                    label = "Due Today ⚠️",
                    severity = DeadlineBadgeSeverity.CRITICAL,
                    daysRemaining = 0
                )
            }
            days == 1 -> {
                DeadlineBadgeInfo(
                    label = "Due Tomorrow",
                    severity = DeadlineBadgeSeverity.CRITICAL,
                    daysRemaining = 1
                )
            }
            days in 2..4 -> {
                DeadlineBadgeInfo(
                    label = "In $days days",
                    severity = DeadlineBadgeSeverity.WARNING,
                    daysRemaining = days
                )
            }
            days in 5..14 -> {
                DeadlineBadgeInfo(
                    label = "In $days days",
                    severity = DeadlineBadgeSeverity.UPCOMING,
                    daysRemaining = days
                )
            }
            else -> {
                DeadlineBadgeInfo(
                    label = "In $days days",
                    severity = DeadlineBadgeSeverity.NEUTRAL,
                    daysRemaining = days
                )
            }
        }
    }

    fun formatTodayDate(): String {
        val sdf = SimpleDateFormat("EEEE, MMM d, yyyy", Locale.ENGLISH)
        return sdf.format(Date())
    }

    fun getGreeting(): String {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        return when (hour) {
            in 5..11 -> "Good morning"
            in 12..16 -> "Good afternoon"
            in 17..21 -> "Good evening"
            else -> "Night owl mode"
        }
    }
}
