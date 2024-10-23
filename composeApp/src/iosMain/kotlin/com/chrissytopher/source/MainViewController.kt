package com.chrissytopher.source

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.window.ComposeUIViewController
import com.chrissytopher.source.Platform
import com.liftric.kvault.KVault
import dev.icerock.moko.permissions.ios.PermissionsController
import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier
import kotlinx.serialization.json.Json
import platform.UIKit.UIViewController

fun MainViewController(getSourceData: (String, String) -> String, filesDir: String, sendNotification: (String, String) -> Unit, openLink: (String) -> Unit): UIViewController {
    val platform = IOSPlatform(filesDir, getSourceData, openLink)
    val kvault = KVault()
    val permissionsController = PermissionsController()
    val notificationSender = object : NotificationSender() {
        override fun sendNotification(title: String, body: String) {
            sendNotification(title, body)
        }
    }
    return ComposeUIViewController {
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

fun debugBuild() {
    Napier.base(DebugAntilog())
    Napier.d("started napier!")
}

fun runBackgroundSync(sendNotification: (String, String) -> Unit, getSourceData: (String, String) -> String, filesDir: String) {
    Napier.base(DebugAntilog())
    val kvault = KVault()
    val json = Json { ignoreUnknownKeys = true }
    val notificationSender = object : NotificationSender() {
        override fun sendNotification(title: String, body: String) {
            sendNotification(title, body)
        }
    }
    val platform = IOSPlatform(filesDir, getSourceData) {
        Napier.e("tried to open a link but openLink is null!")
    }
    doBackgroundSync(kvault, json, notificationSender, platform)
}