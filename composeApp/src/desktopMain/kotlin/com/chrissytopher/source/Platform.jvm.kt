package com.chrissytopher.source

class JVMPlatform: Platform {
    override val name: String = "Java ${System.getProperty("java.version")}"
}

actual fun getPlatform(): Platform = JVMPlatform()

actual fun getSourceData(username: String, password: String): String {
    println(System.mapLibraryName("sourceapp"))
    return SourceApi.getSourceData(username, password)
}