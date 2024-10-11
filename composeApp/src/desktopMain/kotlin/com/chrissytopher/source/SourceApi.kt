package com.chrissytopher.source

object SourceApi {
    init {
        System.loadLibrary("sourceapp")
    }

    @JvmStatic
    external fun getSourceData(username: String, password: String): String
}
