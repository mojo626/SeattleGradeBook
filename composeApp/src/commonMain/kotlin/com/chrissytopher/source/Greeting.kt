package com.chrissytopher.source

import android.os.Bundle
import android.util.Log

class Greeting {
    private val platform = getPlatform()

    fun greet(): String {
        Log.d("com.chrissytopher.source", "Hello World!")
        return "Hello, ${platform.name}!"
    }
}