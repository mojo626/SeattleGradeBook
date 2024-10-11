package com.chrissytopher.source

import kotlinx.serialization.json.Json
import platform.UIKit.UIDevice

class IOSPlatform: Platform {
    override val name: String = UIDevice.currentDevice.systemName() + " " + UIDevice.currentDevice.systemVersion
}

actual fun getPlatform(): Platform = IOSPlatform()

lateinit var getSourceDataSwift: (String, String) -> String

actual fun getSourceData(username: String, password: String): List<Class>? = runCatching {
    Json.decodeFromString<List<Class>>(getSourceDataSwift(username, password))
}.getOrNull()