package com.chrissytopher.source

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.activity.OnBackPressedDispatcher
import androidx.compose.animation.core.SeekableTransitionState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import kotlinx.serialization.json.Json
import org.jetbrains.compose.resources.DrawableResource
import source2.composeapp.generated.resources.Res
import source2.composeapp.generated.resources.icon_android
import kotlin.system.exitProcess

class AndroidPlatform(private val context: Context) : Platform {
    override val name: String = "Android ${Build.VERSION.SDK_INT}"

    private val json = Json { ignoreUnknownKeys = true }

    override fun getSourceData(username: String, password: String): Result<SourceData> = runCatching {
        json.decodeFromString(SourceApi.getSourceData(username, password, context.filesDir.absolutePath))
    }

    override fun closeApp() {
        exitProcess(0)
    }
    override fun filesDir(): String {
        return context.filesDir.absolutePath
    }

    override fun livingInFearOfBackGestures(): Boolean {
        //https://www.b4x.com/android/forum/threads/solved-how-to-determine-users-system-navigation-mode-back-button-or-side-swipe.159347/
        val navigationMode = Settings.Secure.getInt(context.contentResolver, "navigation_mode")
        return (navigationMode == 2)
    }



    override fun appIcon() = Res.drawable.icon_android
    override fun iconRounding() = CircleShape

    override fun openLink(link: String) {
        val urlIntent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse(link)
        )
        context.startActivity(urlIntent)
    }

    @Composable
    override fun BackHandler(enabled: Boolean, onBack: () -> Unit) {
        androidx.activity.compose.BackHandler(enabled, onBack)
    }


}

@Composable
actual fun getScreenSize(): IntSize {
    return with(LocalDensity.current) {
        IntSize(LocalConfiguration.current.screenWidthDp.dp.roundToPx(), LocalConfiguration.current.screenHeightDp.dp.roundToPx())
    }
}
