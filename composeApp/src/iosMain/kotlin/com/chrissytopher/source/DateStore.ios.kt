package com.chrissytopher.source

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences

private var globalDataStore: DataStore<Preferences>? = null

fun createDataStore(filesDir: String): DataStore<Preferences> {
    if (globalDataStore == null) {
        globalDataStore = createDataStore(
            producePath = {
                "$filesDir/$dataStoreFileName"
            }
        )
    }
    return globalDataStore!!
}