package com.chrissytopher.source

import android.os.Build
import kotlinx.serialization.json.Json

class AndroidPlatform : Platform {
    override val name: String = "Android ${Build.VERSION.SDK_INT}"
}

actual fun getPlatform(): Platform = AndroidPlatform()

private val json = Json { ignoreUnknownKeys = true }

actual fun getSourceData(username: String, password: String): Result<List<Class>> = runCatching {
    json.decodeFromString<List<Class>>(SourceApi.getSourceData(username, password))
}