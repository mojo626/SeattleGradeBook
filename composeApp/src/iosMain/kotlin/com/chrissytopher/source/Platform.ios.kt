package com.chrissytopher.source

import kotlinx.serialization.json.Json
import platform.UIKit.UIDevice

class IOSPlatform: Platform {
    override val name: String = UIDevice.currentDevice.systemName() + " " + UIDevice.currentDevice.systemVersion
}

actual fun getPlatform(): Platform = IOSPlatform()

lateinit var getSourceDataSwift: (String, String) -> String

private val json = Json { ignoreUnknownKeys = true }

actual fun getSourceData(username: String, password: String): Result<List<Class>> = runCatching {
    json.decodeFromString<List<Class>>(getSourceDataSwift(username, password))
}