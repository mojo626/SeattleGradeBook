package com.chrissytopher.source

import android.os.Build
import kotlinx.serialization.json.Json

class AndroidPlatform : Platform {
    override val name: String = "Android ${Build.VERSION.SDK_INT}"
}

actual fun getPlatform(): Platform = AndroidPlatform()

actual fun getSourceData(username: String, password: String): List<Class>? = runCatching {
    Json.decodeFromString<List<Class>>(SourceApi.getSourceData(username, password))
}.getOrNull()