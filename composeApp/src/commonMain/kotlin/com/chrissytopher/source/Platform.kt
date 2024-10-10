package com.chrissytopher.source

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform