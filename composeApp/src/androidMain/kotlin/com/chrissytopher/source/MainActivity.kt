package com.chrissytopher.source

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequest
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.chrissytopher.source.themes.theme.AppTheme
import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.io.Source
import kotlinx.io.asSource
import kotlinx.io.buffered
import java.util.concurrent.TimeUnit

var getContentCallback: (suspend (Source?) -> Unit)? = null

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        Napier.base(DebugAntilog())
        super.onCreate(savedInstanceState)
        val platform = AndroidPlatform(this)
        val viewModel: AndroidAppViewModel by viewModels { AndroidAppViewModel.factory(this.applicationContext) }
        viewModel.permissionsController.bind(this)
        createNotificationChannel(ASSIGNMENTS_NOTIFICATION_CHANNEL, "Grade Updates", "Updates to grades and assignments")
        val backgroundSyncRequest: PeriodicWorkRequest =
            PeriodicWorkRequestBuilder<BackgroundSyncWorker>(2, TimeUnit.HOURS)
                .build()
        WorkManager
            .getInstance(this)
            .enqueueUniquePeriodicWork(WORK_MANAGER_BACKGROUND_SYNC_ID, ExistingPeriodicWorkPolicy.KEEP, backgroundSyncRequest)

        //test background sync, caveman way no unit tests 🤢🤢🤢
//        CoroutineScope(Dispatchers.Main).launch {
//            delay(5000)
//            doBackgroundSync(createDataStore(applicationContext), viewModel.json, null, { username: String, password: String, quarter: String, pfp: Boolean ->
//                runCatching {
//                    viewModel.json.decodeFromString<SourceData>(SourceApi.getSourceData(username, password, applicationContext.filesDir.absolutePath, quarter, pfp))
//                }
//            })
//        }
        setContent {
            CompositionLocalProvider(LocalPlatform provides platform) {
                AppTheme {
                    val viewModelInitialized by viewModel.initializedFlows.collectAsState()
                    if (!viewModelInitialized) {
                        Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surface))
                        return@AppTheme
                    }
                    App(viewModel)
                }
            }
        }
    }

    val getContent = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        CoroutineScope(Dispatchers.Main).launch {
            if (uri != null) {
                Log.d("ContentPicker", "Selected URI: $uri")
                contentResolver.openInputStream(uri)?.asSource()?.let { getContentCallback?.invoke(it.buffered()) }
            } else {
                Log.d("ContentPicker", "No media selected")
                getContentCallback?.invoke(null)
            }
        }
    }
}