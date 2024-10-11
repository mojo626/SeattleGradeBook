package com.chrissytopher.source

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform

expect fun getSourceData(username: String, password: String): String