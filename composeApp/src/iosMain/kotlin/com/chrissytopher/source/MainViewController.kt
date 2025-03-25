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
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import platform.UIKit.UIViewController

fun MainViewController(getSourceData: (String, String, String, Boolean) -> String, filesDir: String, sendNotification: (String, String) -> Unit, openLink: (String) -> Unit): UIViewController {
    val notificationSender = object : NotificationSender() {
        override fun sendNotification(title: String, body: String) {
            sendNotification(title, body)
        }
    }
    return ComposeUIViewController {
        val viewModel = viewModel { IosAppViewModel(notificationSender, getSourceData, createDataStore(filesDir)) }
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

fun runBackgroundSync(sendNotification: (String, String) -> Unit, getSourceData: (String, String, String, Boolean) -> String, filesDir: String) {
    Napier.base(DebugAntilog())
    val json = Json { ignoreUnknownKeys = true }
    val notificationSender = object : NotificationSender() {
        override fun sendNotification(title: String, body: String) {
            sendNotification(title, body)
        }
    }
    val dataStore = createDataStore(filesDir)
    val runBackgroundSync = { username: String, password: String, quarter: String, pfp: Boolean ->
        runCatching { Json.decodeFromString<SourceData>(getSourceData(username, password, quarter, pfp)) }
    }
    return runBlocking {
        backgroundSyncDatastore(dataStore, json, notificationSender, runBackgroundSync)
    }
}