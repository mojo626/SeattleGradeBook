package com.chrissytopher.source

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

const val SOURCE_DATA_KEY = "SOURCE_DATA"
const val USERNAME_KEY = "USERNAME"
const val PASSWORD_KEY = "PASSWORD"
const val HIDE_MENTORSHIP_KEY = "HIDE_MENTORSHIP"
const val CLASS_UPDATES_KEY = "CLASS_UPDATES"
const val NEW_ASSIGNMENTS_NOTIFICATIONS_KEY = "NEW_ASSIGNMENTS_NOTIFICATIONS"
const val LETTER_GRADE_CHANGES_NOTIFICATIONS_KEY = "LETTER_GRADE_CHANGES_NOTIFICATIONS"
const val THRESHOLD_NOTIFICATIONS_KEY = "THRESHOLD_NOTIFICATIONS"
const val THRESHOLD_VALUE_NOTIFICATIONS_KEY = "THRESHOLD_VALUE_NOTIFICATIONS"

const val MENTORSHIP_NAME = "MENTORSHIP"

const val ASSIGNMENTS_NOTIFICATION_CHANNEL = "ASSIGNMENTS"

const val WORK_MANAGER_BACKGROUND_SYNC_ID = "BACKGROUND_SYNC"

val gradeColors = mapOf(
    "A" to Color(0xFF5ab52c),
    "B" to Color(0xff20abdc),
    "C" to Color(0xffdcc927),
    "D" to Color(0xffe76918),
    "E" to Color(0xffee2323),
)

@Composable
fun darkModeColorModifier() = if (isSystemInDarkTheme()) 1f else 1f