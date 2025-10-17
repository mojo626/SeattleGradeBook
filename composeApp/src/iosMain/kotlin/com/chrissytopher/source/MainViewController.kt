package com.chrissytopher.source

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.interop.LocalUIViewController
import androidx.compose.ui.window.ComposeUIViewController
import androidx.lifecycle.viewmodel.compose.viewModel
import com.chrissytopher.source.themes.theme.AppTheme
import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier
import io.ktor.http.decodeURLPart
import kotlinx.coroutines.runBlocking
import kotlinx.io.files.Path
import kotlinx.serialization.json.Json
import platform.UIKit.UIViewController

fun MainViewController(filesDir: String, sendNotification: (String, String) -> Unit, openLink: (String) -> Unit): UIViewController {
    val notificationSender = object : NotificationSender() {
        override fun sendNotification(title: String, body: String) {
            sendNotification(title, body)
        }
    }
    return ComposeUIViewController {
        val viewModel = viewModel { IosAppViewModel(notificationSender, createDataStore(filesDir), Path(filesDir.decodeURLPart())) }
        CompositionLocalProvider(LocalPlatform provides IOSPlatform(LocalUIViewController.current, filesDir, openLink)) {
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

fun debugBuild() {
    Napier.base(DebugAntilog())
    Napier.d("started napier!")
}

fun runBackgroundSync(sendNotification: (String, String) -> Unit, filesDir: String) {
    Napier.base(DebugAntilog())
    val notificationSender = object : NotificationSender() {
        override fun sendNotification(title: String, body: String) {
            sendNotification(title, body)
        }
    }
    val dataStore = createDataStore(filesDir)
    val json = json()
    val sourceApi = sourceApi(Path(filesDir.decodeURLPart()), json)
    val runBackgroundSync: GetSourceDataLambda = { username: String, password: String, quarter: String, pfp: Boolean ->
        sourceApi.getSourceData(username, password, quarter, pfp, true, null)
    }
    return runBlocking {
        backgroundSyncDatastore(dataStore, json, notificationSender, runBackgroundSync)
    }
}