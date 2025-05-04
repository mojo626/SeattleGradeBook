package com.chrissytopher.source

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences

private var globalDataStore: DataStore<Preferences>? = null

fun createDataStore(context: Context): DataStore<Preferences> {
    return globalDataStore
        ?: createDataStore(
            producePath = { context.filesDir.resolve(dataStoreFileName).absolutePath }
        ).apply { globalDataStore = this }
}