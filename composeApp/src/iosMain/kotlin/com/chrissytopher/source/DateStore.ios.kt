package com.chrissytopher.source

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences

fun createDataStore(filesDir: String): DataStore<Preferences> = createDataStore(
    producePath = {
        "$filesDir/$dataStoreFileName"
    }
)