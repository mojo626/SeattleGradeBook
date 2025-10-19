package com.chrissytopher.source

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.interop.LocalUIViewController
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.ComposeUIViewController
import androidx.lifecycle.viewmodel.compose.viewModel
import com.chrissytopher.source.themes.theme.AppTheme
import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier
import io.ktor.http.decodeURLPart
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking
import kotlinx.io.files.Path
import kotlinx.serialization.json.Json
import platform.UIKit.UIViewController

fun MainViewController(filesDir: String, sendNotification: (String, String) -> Unit, openLink: (String) -> Unit, implementPluey: (Boolean) -> Unit): UIViewController {
    return ComposeUIViewController {
        val viewModel = viewModel { IosAppViewModel(sendNotification, filesDir) }
        CompositionLocalProvider(LocalPlatform provides IOSPlatform(LocalUIViewController.current, filesDir, openLink, implementPluey)) {
            CompositionLocalProvider(WithinApp provides true) {
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
}

fun NavigationViewController(viewModel: IosAppViewModel, navigationPage: NavScreen, filesDir: String, openLink: (String) -> Unit, implementPluey: (Boolean) -> Unit, navigateTo: (NavScreen) -> Unit, navigateBack: () -> Unit, navigationStackCleared: (NavScreen) -> Unit, paddingState: MutableStateFlow<PaddingValues>): UIViewController {
    return ComposeUIViewController {
        CompositionLocalProvider(LocalPlatform provides IOSPlatform(LocalUIViewController.current, filesDir, openLink, implementPluey)) {
            AppTheme {
                val viewModelInitialized by viewModel.initializedFlows.collectAsState()
                if (!viewModelInitialized) {
                    Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surface))
                } else {
                    val paddingValues by paddingState.collectAsState()
                    Surface {
                        NavSwitcher(viewModel, navigationPage, paddingValues, navigateTo, navigateBack, navigationStackCleared)
                    }
                }
            }
        }
    }
}

fun newPaddingState(): MutableStateFlow<PaddingValues> = MutableStateFlow(PaddingValues())
fun paddingValues(top: Float, bottom: Float, start: Float, end: Float) = PaddingValues(top = top.dp, bottom = bottom.dp, start = start.dp, end = end.dp)

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