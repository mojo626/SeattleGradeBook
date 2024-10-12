package com.chrissytopher.source

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform

expect fun getSourceData(username: String, password: String): Result<SourceData>

expect fun closeApp()

expect fun filesDir(): String