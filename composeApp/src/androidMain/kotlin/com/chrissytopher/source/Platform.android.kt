package com.chrissytopher.source

import android.os.Build

class AndroidPlatform : Platform {
    override val name: String = "Android ${Build.VERSION.SDK_INT}"
}

actual fun getPlatform(): Platform = AndroidPlatform()

actual fun getSourceData(username: String, password: String): String {
    return SourceApi.getSourceData(username, password)
}