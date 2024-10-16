package com.chrissytopher.source

import android.os.Build
import androidx.compose.foundation.shape.CircleShape
import kotlinx.serialization.json.Json
import org.jetbrains.compose.resources.DrawableResource
import source2.composeapp.generated.resources.Res
import source2.composeapp.generated.resources.icon_android
import kotlin.system.exitProcess

class AndroidPlatform : Platform {
    override val name: String = "Android ${Build.VERSION.SDK_INT}"
}

actual fun getPlatform(): Platform = AndroidPlatform()

private val json = Json { ignoreUnknownKeys = true }

actual fun getSourceData(username: String, password: String): Result<SourceData> = runCatching {
    json.decodeFromString(SourceApi.getSourceData(username, password, filesDirectory))
}

actual fun closeApp() {
    exitProcess(0)
}
actual fun filesDir(): String {
    return filesDirectory
}

actual fun livingInFearOfBackGestures(): Boolean = true

actual fun appIcon() = Res.drawable.icon_android
actual fun iconRounding() = CircleShape