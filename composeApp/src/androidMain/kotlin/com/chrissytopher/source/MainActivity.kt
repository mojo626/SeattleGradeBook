package com.chrissytopher.source

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.tooling.preview.Preview
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequest
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkRequest
import com.liftric.kvault.KVault
import dev.icerock.moko.permissions.PermissionsController
import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier
import java.time.Duration
import java.util.concurrent.TimeUnit

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        Napier.base(DebugAntilog())
        val kvault = KVault(this)
        super.onCreate(savedInstanceState)
        val platform = AndroidPlatform(this)
        val permissionsController = PermissionsController(this)
        permissionsController.bind(this)
        createNotificationChannel(ASSIGNMENTS_NOTIFICATION_CHANNEL, "Grade Updates", "Updates to grades and assignments")
        val notificationSender = AndroidNotificationSender(this)
        val backgroundSyncRequest: PeriodicWorkRequest =
            PeriodicWorkRequestBuilder<BackgroundSyncWorker>(2, TimeUnit.HOURS)
                .build()
        WorkManager
            .getInstance(this)
            .enqueueUniquePeriodicWork(WORK_MANAGER_BACKGROUND_SYNC_ID, ExistingPeriodicWorkPolicy.KEEP, backgroundSyncRequest)

        setContent {
            CompositionLocalProvider(LocalPermissionsController provides permissionsController) {
                CompositionLocalProvider(LocalKVault provides kvault) {
                    CompositionLocalProvider(LocalNotificationSender provides notificationSender) {
                        CompositionLocalProvider(LocalPlatform provides platform) {
                            App()
                        }
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}