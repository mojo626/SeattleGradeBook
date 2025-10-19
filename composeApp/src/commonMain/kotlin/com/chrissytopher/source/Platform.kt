package com.chrissytopher.source

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.IntSize
import io.github.aakira.napier.Napier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.io.Source
import org.jetbrains.compose.resources.DrawableResource

abstract class Platform {
    abstract val name: String

    abstract fun closeApp()

    abstract fun filesDir(): String

    abstract fun livingInFearOfBackGestures(): Boolean

    abstract fun appIcon(): DrawableResource
    abstract fun snowFlake(): DrawableResource
    abstract fun iconRounding(): RoundedCornerShape

    abstract fun openLink(link: String)

    @Composable
    abstract fun BackHandler(enabled: Boolean, onBack: () -> Unit)

    abstract fun shareText(text: String)

    abstract suspend fun pickFile(mimeType: String): Source?

    abstract fun imageTypeDescriptor(): String
    abstract fun jsonTypeDescriptor(): String

    abstract fun successVibration()
    abstract fun failureVibration()

    abstract fun implementPluey(reverse: Boolean)
}

@Composable
expect fun getScreenSize(): IntSize