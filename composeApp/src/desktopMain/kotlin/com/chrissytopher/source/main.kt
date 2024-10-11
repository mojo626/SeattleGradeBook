package com.chrissytopher.source

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

fun main() = application {
    LaunchedEffect(true) {
        getSourceData("1cjhuntwork", "joemama")
    }
    Window(
        onCloseRequest = ::exitApplication,
        title = "Source2",
    ) {
        App()
    }
}