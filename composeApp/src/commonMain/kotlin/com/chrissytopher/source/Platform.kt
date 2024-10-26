package com.chrissytopher.source

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.IntSize
import org.jetbrains.compose.resources.DrawableResource

interface Platform {
    val name: String
    fun getSourceData(username: String, password: String): Result<SourceData>

    fun closeApp()

    fun filesDir(): String

    fun livingInFearOfBackGestures(): Boolean

    fun appIcon(): DrawableResource
    fun iconRounding(): RoundedCornerShape

    fun openLink(link: String)

    @Composable
    fun BackHandler(enabled: Boolean, onBack: () -> Unit)
}

@Composable
expect fun getScreenSize(): IntSize