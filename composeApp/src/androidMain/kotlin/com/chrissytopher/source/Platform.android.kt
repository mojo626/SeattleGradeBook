package com.chrissytopher.source

import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import source2.composeapp.generated.resources.Res
import source2.composeapp.generated.resources.icon_android
import source2.composeapp.generated.resources.snowflake_android
import kotlin.system.exitProcess
import androidx.core.net.toUri
import kotlinx.coroutines.channels.Channel
import kotlinx.io.Source

class AndroidPlatform(private val context: Context) : Platform() {
    override val name: String = "Android ${Build.VERSION.SDK_INT}"

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
    override fun snowFlake() = Res.drawable.snowflake_android
    override fun iconRounding() = CircleShape

    override fun openLink(link: String) {
        val urlIntent = Intent(
            Intent.ACTION_VIEW,
            link.toUri()
        )
        context.startActivity(urlIntent)
    }

    @Composable
    override fun BackHandler(enabled: Boolean, onBack: () -> Unit) {
        androidx.activity.compose.BackHandler(enabled, onBack)
    }

    override fun shareText(text: String) {
        val sendIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, text)
            type = "text/plain"
        }

        val shareIntent = Intent.createChooser(sendIntent, null)
        context.startActivity(shareIntent)
    }

    override suspend fun pickFile(mimeType: String): Source? {
        val channel = Channel<Source?>()
        getContentCallback = {
            channel.send(it)
        }
        (context as? MainActivity)?.getContent?.launch(mimeType)
        return channel.receive()
    }

    override fun imageTypeDescriptor() = "image/*"
    override fun jsonTypeDescriptor() = "application/json"
}

@Composable
actual fun getScreenSize(): IntSize {
    return with(LocalDensity.current) {
        IntSize(LocalConfiguration.current.screenWidthDp.dp.roundToPx(), LocalConfiguration.current.screenHeightDp.dp.roundToPx())
    }
}
