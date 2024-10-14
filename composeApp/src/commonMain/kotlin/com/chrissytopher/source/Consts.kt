package com.chrissytopher.source

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

const val SOURCE_DATA_KEY = "SOURCE_DATA"
const val USERNAME_KEY = "USERNAME"
const val PASSWORD_KEY = "PASSWORD"
const val HIDE_MENTORSHIP_KEY = "HIDE_MENTORSHIP"

const val MENTORSHIP_NAME = "MENTORSHIP"

val gradeColors = mapOf(
    "A" to Color(0xFF64ed72),
    "B" to Color(0xFF69d0f5),
    "C" to Color(0xFFf0e269),
    "D" to Color(0xFFf09151),
    "E" to Color(0xFFf24646),
)

@Composable
fun darkModeColorModifier() = if (isSystemInDarkTheme()) 0.8f else 1f