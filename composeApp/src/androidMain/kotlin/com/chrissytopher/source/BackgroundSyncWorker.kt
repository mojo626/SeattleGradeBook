package com.chrissytopher.source

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.liftric.kvault.KVault
import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier
import kotlinx.serialization.json.Json

class BackgroundSyncWorker(private val appContext: Context, workerParams: WorkerParameters):
    Worker(appContext, workerParams) {
    override fun doWork(): Result {
        Napier.base(DebugAntilog())
        val kvault = KVault(appContext)
        val json = Json { ignoreUnknownKeys = true }
        val notificationSender = AndroidNotificationSender(appContext)
        val platform = AndroidPlatform(appContext)
        doBackgroundSync(kvault, json, notificationSender, platform)
        return Result.success()
    }
}