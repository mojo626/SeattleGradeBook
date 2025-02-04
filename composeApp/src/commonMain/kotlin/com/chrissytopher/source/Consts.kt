package com.chrissytopher.source

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

const val SOURCE_DATA_KEY = "SOURCE_DATA"
const val USERNAME_KEY = "USERNAME"
const val PASSWORD_KEY = "PASSWORD"
const val QUARTER_KEY = "QUARTER"
const val HIDE_MENTORSHIP_KEY = "HIDE_MENTORSHIP"
const val SHOW_MIDDLE_NAME_KEY = "SHOW_MIDDLE_NAME"
const val GRADE_COLORS_KEY = "GRADE_COLORS"
const val CLASS_UPDATES_KEY = "CLASS_UPDATES"
const val PREFER_REPORTED_KEY = "PREFER_REPORTED"
const val NEW_ASSIGNMENTS_NOTIFICATIONS_KEY = "NEW_ASSIGNMENTS_NOTIFICATIONS"
const val LETTER_GRADE_CHANGES_NOTIFICATIONS_KEY = "LETTER_GRADE_CHANGES_NOTIFICATIONS"
const val THRESHOLD_NOTIFICATIONS_KEY = "THRESHOLD_NOTIFICATIONS"
const val THRESHOLD_VALUE_NOTIFICATIONS_KEY = "THRESHOLD_VALUE_NOTIFICATIONS"
const val WIDGET_CLASS_KEY = "WIDGET_"

const val MENTORSHIP_NAME = "MENTORSHIP"

const val ASSIGNMENTS_NOTIFICATION_CHANNEL = "ASSIGNMENTS"

const val WORK_MANAGER_BACKGROUND_SYNC_ID = "BACKGROUND_SYNC"

//val greenColor = Color(0xFF5ab52c)
//val blueColor = Color(0xff20abdc)
//val yellowColor = Color(0xffdcc927)
//val orangeColor = Color(0xffe76918)
//val redColor = Color(0xffee2323)
//
//val georgeGreenColor = Color(0xff428820)
//val georgeBlueColor = Color(0xff1d92ba)
//
//val gradeColors = mapOf(
//    "A" to greenColor,
//    "B" to blueColor,
//    "C" to yellowColor,
//    "D" to orangeColor,
//    "E" to redColor,
//)
//
//val georgeGradeColors = mapOf(
//    "A" to georgeGreenColor,
//    "B" to georgeBlueColor,
//    "C" to yellowColor,
//    "D" to orangeColor,
//    "E" to redColor,
//)

@Composable
fun darkModeColorModifier() = if (isSystemInDarkTheme()) 1f else 1f