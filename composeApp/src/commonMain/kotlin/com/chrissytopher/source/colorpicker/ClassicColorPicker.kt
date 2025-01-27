package com.chrissytopher.materialme.colorpicker

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Slider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

/**
 * Classic Color Picker Component that shows a HSV representation of a color, with a Hue Bar on the right,
 * Alpha Bar on the bottom and the rest of the area covered with an area with saturation value touch area.
 *
 * @param modifier modifiers to set to this color picker.
 * @param color the initial color to set on the picker.
 * @param showAlphaBar whether or not to show the bottom alpha bar on the color picker.
 * @param onColorChanged callback that is triggered when the color changes
 *
 */
@Deprecated(
    message = "This version doesn't have matching emit and intake values, move to using the version that takes hsvColor",
    replaceWith =
    ReplaceWith("ClassicColorPicker(modifier = modifier, color = HsvColor.from(color), showAlphaBar = showAlphaBar, onColorChanged = onColorChanged)")
)
@Composable
fun ClassicColorPicker(
    modifier: Modifier = Modifier,
    color: Color = Color.Red,
//    showAlphaBar: Boolean = true,
    onColorChanged: (HsvColor) -> Unit
) {
    ClassicColorPicker(
        modifier = modifier,
        color = HsvColor.from(color),
//        showAlphaBar = showAlphaBar,
        onColorChanged = onColorChanged
    )
}

/**
 * Classic Color Picker Component that shows a HSV representation of a color, with a Hue Bar on the right,
 * Alpha Bar on the bottom and the rest of the area covered with an area with saturation value touch area.
 *
 * @param modifier modifiers to set to this color picker.
 * @param color the initial color to set on the picker.
 * @param showAlphaBar whether or not to show the bottom alpha bar on the color picker.
 * @param onColorChanged callback that is triggered when the color changes
 *
 */
@Composable
fun ClassicColorPicker(
    modifier: Modifier = Modifier,
    color: HsvColor = HsvColor.from(Color.Red),
//    showAlphaBar: Boolean = true,
    onColorChanged: (HsvColor) -> Unit
) {
    val colorPickerValueState = rememberSaveable(stateSaver = HsvColor.Saver) {
        mutableStateOf(color)
    }
    Row(modifier = modifier) {
        val barThickness = 32.dp
        val paddingBetweenBars = 8.dp
        val updatedOnColorChanged by rememberUpdatedState(onColorChanged)
        Column(modifier = Modifier.weight(0.8f)) {
            SaturationValueArea(
                modifier = Modifier.weight(0.8f),
                currentColor = colorPickerValueState.value,
                onSaturationValueChanged = { saturation, value ->
                    colorPickerValueState.value =
                        colorPickerValueState.value.copy(saturation = saturation, value = value)
                    updatedOnColorChanged(colorPickerValueState.value)
                }
            )
//            if (showAlphaBar) {
                Spacer(modifier = Modifier.height(paddingBetweenBars))
                HueSlider(
                    modifier = Modifier.height(barThickness),
                    currentColor = colorPickerValueState.value,
                    onHueChanged = { newHue ->
                        colorPickerValueState.value = colorPickerValueState.value.copy(hue = newHue)
                        updatedOnColorChanged(colorPickerValueState.value)
                    }
                )
//            }
        }
//        Spacer(modifier = Modifier.width(paddingBetweenBars))
//        HueBar(
//            modifier = Modifier.width(barThickness),
//            currentColor = colorPickerValueState.value,
//            onHueChanged = { newHue ->
//                colorPickerValueState.value = colorPickerValueState.value.copy(hue = newHue)
//                updatedOnColorChanged(colorPickerValueState.value)
//            }
//        )
    }
}

@Composable
fun HueSlider(
    modifier: Modifier = Modifier,
    currentColor: HsvColor,
    onHueChanged: (Float) -> Unit,
) {
    val rainbowBrush = remember { Brush.horizontalGradient(getRainbowColors()) }

    Box(modifier, contentAlignment = Alignment.CenterStart) {
        val size = remember { mutableStateOf(IntSize.Zero) }
        Box(Modifier.fillMaxWidth().fillMaxHeight(0.5f).background(rainbowBrush, RoundedCornerShape(1000.dp)).onSizeChanged { size.value = it}) {

        }
        val dragPos = remember { mutableStateOf(currentColor.hue) }
        val dragState = rememberDraggableState {
            dragPos.value += it/size.value.width.toFloat()*360f
            onHueChanged(min(max(dragPos.value, 0f), 360f))
        }
        Box(Modifier.fillMaxHeight().aspectRatio(1f).offset { IntOffset(((currentColor.hue/360f)*size.value.width.toFloat()).roundToInt() - size.value.height, 0) }.background(currentColor.copy(saturation = 1f, value = 1f).toColor(), CircleShape).border(3.dp, Color.White, CircleShape).draggable(dragState, Orientation.Horizontal))
    }
}

fun getRainbowColors(): List<Color> {
    return (0..360).map {
        Color.hsl(it.toFloat(), 1f, 0.5f)
    }
}