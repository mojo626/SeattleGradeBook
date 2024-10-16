package com.chrissytopher.source

import androidx.compose.foundation.shape.RoundedCornerShape
import kotlinx.serialization.json.Json
import platform.UIKit.UIDevice
import platform.posix.exit
import source2.composeapp.generated.resources.Res
import source2.composeapp.generated.resources.icon_apple

class IOSPlatform: Platform {
    override val name: String = UIDevice.currentDevice.systemName() + " " + UIDevice.currentDevice.systemVersion
}

actual fun getPlatform(): Platform = IOSPlatform()

lateinit var getSourceDataSwift: (String, String) -> String
lateinit var globalFilesDir: String

private val json = Json { ignoreUnknownKeys = true }

actual fun getSourceData(username: String, password: String): Result<SourceData> = runCatching {
    json.decodeFromString(getSourceDataSwift(username, password))
}

actual fun closeApp() {
    exit(0)
}

actual fun filesDir(): String {
    return globalFilesDir
}

actual fun livingInFearOfBackGestures(): Boolean = false

actual fun appIcon() = Res.drawable.icon_apple
actual fun iconRounding() = RoundedCornerShape(20)