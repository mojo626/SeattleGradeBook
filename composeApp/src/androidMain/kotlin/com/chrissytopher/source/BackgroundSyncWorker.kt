package com.chrissytopher.source

import android.content.Context
import androidx.activity.viewModels
import androidx.datastore.preferences.core.edit
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.liftric.kvault.KVault
import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier
import io.ktor.http.decodeURLPart
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.io.files.Path
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

class BackgroundSyncWorker(private val appContext: Context, workerParams: WorkerParameters):
    Worker(appContext, workerParams) {
    override fun doWork(): Result {
        Napier.base(DebugAntilog())
        val json = json()
        val sourceApi = sourceApi(Path(appContext.filesDir.absolutePath), json)
        val notificationSender = AndroidNotificationSender(appContext)
        val runBackgroundSync: GetSourceDataLambda = { username: String, password: String, quarter: String, pfp: Boolean ->
            sourceApi.getSourceData(username, password, quarter, pfp, true, null)
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