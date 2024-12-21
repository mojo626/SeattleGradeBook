package com.chrissytopher.source

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
    import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.layout.onSizeChanged
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.imageResource
import kotlin.math.sin
import kotlin.random.Random

@Composable
fun Snow() {
    val startUpLocal = RefreshedAlready.current.value
    var startUp = remember { startUpLocal }
    var width by remember { mutableStateOf(0f) }
    var height by remember { mutableStateOf(0f) }
    var time by remember { mutableStateOf(0f) }
    val flakes = remember { mutableStateOf((1..100).map {
        Pair(Pair(Random.nextFloat(), Random.nextFloat() * if (startUp) 1 else -1), Random.nextFloat()*30f+20f)
    }.toMutableList()) }
    val timeAnimator = remember { Animatable(time) }
    val timeAnimatorScope = rememberCoroutineScope()
    timeAnimatorScope.launch {
        while (true) {
            timeAnimator.animateTo(
                targetValue = time + 10f,
                animationSpec = tween(10000, easing = LinearEasing)
            ) {
                time = value
            }
        }
    }

    for (i in 0 until flakes.value.size) {
        if ((flakes.value[i].first.second)*height + time * 50f > height) {
            flakes.value[i] = flakes.value[i].copy(first = flakes.value[i].first.copy(second = (-time * 50f - flakes.value[i].second*4f)/height))
        }
    }

    val platform = LocalPlatform.current
    val snowFlake: ImageBitmap = imageResource(platform.snowFlake())
    Box(Modifier.fillMaxSize().onSizeChanged { width = it.width.toFloat(); height = it.height.toFloat() }.drawBehind {
        for ((pos, size) in flakes.value) {
            val (x, y) = pos
            drawImageBitmap(snowFlake, Offset(x*width + sin(time+size)*60f, y*height + time * 50f), Size(size, size)*2f)
        }
    }) {}
}

fun DrawScope.drawImageBitmap(imageBitmap: ImageBitmap, offset: Offset, size: Size) {
    val imgWidth = imageBitmap.width
    val imgHeight = imageBitmap.height
    val scaleX = size.width / imgWidth
    val scaleY = size.height / imgHeight

    this.drawIntoCanvas { canvas: Canvas ->
        canvas.save()
        canvas.translate(offset.x, offset.y)
        canvas.scale(scaleX, scaleY)
        canvas.drawImage(
            image = imageBitmap,
            Offset(0f, 0f),
            Paint()
        )
        canvas.restore()
    }
}