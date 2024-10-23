package com.chrissytopher.source

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
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

    override fun livingInFearOfBackGestures(): Boolean = true

    override fun appIcon() = Res.drawable.icon_android
    override fun iconRounding() = CircleShape

    override fun openLink(link: String) {
        val urlIntent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse(link)
        )
        context.startActivity(urlIntent)
    }
}

