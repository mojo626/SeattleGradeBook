package com.chrissytopher.source

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import okio.Path.Companion.toPath

fun createDataStore(producePath: () -> String): DataStore<Preferences> =
    PreferenceDataStoreFactory.createWithPath(
        produceFile = { producePath().toPath(normalize = true) }
    )

internal val dataStoreFileName = "seattle_gradebook.preferences_pb"

val SOURCE_DATA_PREFERENCE = stringPreferencesKey(SOURCE_DATA_KEY)
val USERNAME_PREFERENCE = stringPreferencesKey(USERNAME_KEY)
val PASSWORD_PREFERENCE = stringPreferencesKey(PASSWORD_KEY)
val QUARTER_PREFERENCE = stringPreferencesKey(QUARTER_KEY)
val HIDE_MENTORSHIP_PREFERENCE = booleanPreferencesKey(HIDE_MENTORSHIP_KEY)
val SHOW_MIDDLE_NAME_PREFERENCE = booleanPreferencesKey(SHOW_MIDDLE_NAME_KEY)
val SCROLL_HOME_SCREEN_PREFERENCE = booleanPreferencesKey("SCROLL_HOME_SCREEN")
val IMPLEMENT_PLUEY_PREFERENCE = booleanPreferencesKey("IMPLEMENT_PLUEY")

val GRADE_COLORS_PREFERENCE = stringPreferencesKey(GRADE_COLORS_KEY)
//val CLASS_UPDATES_PREFERENCE = stringPreferencesKey(CLASS_UPDATES_KEY)
val ASSIGNMENT_UPDATES_PREFERENCE = stringPreferencesKey("ASSIGNMENT_UPDATES")
val PREFER_REPORTED_PREFERENCE = booleanPreferencesKey(PREFER_REPORTED_KEY)
val NEW_ASSIGNMENTS_NOTIFICATIONS_PREFERENCE = booleanPreferencesKey(NEW_ASSIGNMENTS_NOTIFICATIONS_KEY)
val LETTER_GRADE_CHANGES_NOTIFICATIONS_PREFERENCE = booleanPreferencesKey(LETTER_GRADE_CHANGES_NOTIFICATIONS_KEY)
val THRESHOLD_NOTIFICATIONS_PREFERENCE = booleanPreferencesKey(THRESHOLD_NOTIFICATIONS_KEY)
val THRESHOLD_VALUE_NOTIFICATIONS_PREFERENCE = floatPreferencesKey(THRESHOLD_VALUE_NOTIFICATIONS_KEY)
val WIDGET_CLASS_PREFERENCE = stringPreferencesKey(WIDGET_CLASS_KEY)
val THEME_PREFERENCE = stringPreferencesKey("THEME")
val AUTO_SYNC_PREFERENCE = booleanPreferencesKey("AUTO_SYNC")