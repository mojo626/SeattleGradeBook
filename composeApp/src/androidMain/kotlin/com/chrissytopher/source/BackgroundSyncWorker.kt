package com.chrissytopher.source

import android.content.Context
import androidx.activity.viewModels
import androidx.datastore.preferences.core.edit
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.liftric.kvault.KVault
import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

class BackgroundSyncWorker(private val appContext: Context, workerParams: WorkerParameters):
    Worker(appContext, workerParams) {
    override fun doWork(): Result {
        Napier.base(DebugAntilog())
        val json = Json { ignoreUnknownKeys = true }
        val notificationSender = AndroidNotificationSender(appContext)
        val runBackgroundSync = { username: String, password: String, quarter: String, pfp: Boolean ->
            runCatching {
                json.decodeFromString<SourceData>(SourceApi.getSourceData(username, password, applicationContext.filesDir.absolutePath, quarter, pfp))
            }
        }
        val dataStore = createDataStore(appContext)
        return runBlocking {
            if (backgroundSyncDatastore(dataStore, json, notificationSender, runBackgroundSync)) {
                return@runBlocking Result.success()
            } else {
                return@runBlocking Result.failure()
            }
        }
    }
}