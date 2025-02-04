package com.chrissytopher.source

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Chip
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChevronLeft
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.KeyboardArrowUp
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedFilterChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SelectableChipElevation
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chrissytopher.materialme.colorpicker.ClassicColorPicker
import com.chrissytopher.materialme.colorpicker.HsvColor
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun ColorsScreen(
    currentTheme: ThemeVariant,
    onThemeChange: (ThemeVariant) -> Unit
) {
    val kvault = LocalKVault.current
    var expanded by remember { mutableStateOf(false) }
    var selectedTheme by remember { mutableStateOf(currentTheme.toString()) }
    val navHost = LocalNavHost.current


    Column(Modifier.verticalScroll(rememberScrollState()).padding(12.dp)) {
        Box(Modifier.fillMaxWidth()) {
            Row(Modifier.align(Alignment.CenterStart)) {
                Spacer(Modifier.width(8.dp))
                val screenSize = getScreenSize()
                Icon(Icons.Outlined.ChevronLeft, contentDescription = "left arrow", modifier = Modifier.padding(0.dp, 5.dp).clip(CircleShape).clickable { navHost?.popStack(screenSize.width.toFloat()) })
            }

            Text("Colors and Themes", style = MaterialTheme.typography.titleLarge, modifier = Modifier.align(Alignment.Center), textAlign = TextAlign.Center)
        }

        Text("App Theme", modifier = Modifier, style = MaterialTheme.typography.titleMedium.copy(color = MaterialTheme.colorScheme.primary))

        Row {
            for (theme in ThemeVariant.entries) {
                ElevatedFilterChip(selected = selectedTheme == theme.toString(), onClick = {
                    selectedTheme = theme.toString()
                    onThemeChange(theme)
                    kvault?.set("THEME", theme.toString())
                }, label = { Text(theme.name) }, elevation = SelectableChipElevation(0.dp, 0.dp, 0.dp, 0.dp, 0.dp, 0.dp))
                Spacer(Modifier.width(5.dp))
            }
        }

//        Box (modifier = Modifier.fillMaxWidth()) {
//            Row( modifier = Modifier.clickable{ expanded = true }.border(2.dp, SolidColor(
//                MaterialTheme.colorScheme.secondary),shape = RoundedCornerShape(15.dp)
//            ) ) {
//                Text(selectedTheme, modifier = Modifier, style = MaterialTheme.typography.titleMedium)
//                Spacer(modifier = Modifier.weight(1f))
//                Icon(if (expanded) Icons.Outlined.KeyboardArrowUp else Icons.Outlined.KeyboardArrowDown, contentDescription = "", modifier = Modifier)
//            }
//
//            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false })
//            {
//                ThemeVariant.entries.forEach { theme ->
//                    DropdownMenuItem (
//                        text = { Text(theme.toString()) },
//                        onClick = { selectedTheme = theme.toString(); onThemeChange(theme); expanded = false; kvault?.set("THEME", theme.toString())}
//                    )
//                }
//            }
//        }
        var gradeColors by LocalGradeColors.current
        var pendingGrade: String? by remember { mutableStateOf(null) }
        Text("Grade Colors", style = MaterialTheme.typography.titleMedium.copy(color = MaterialTheme.colorScheme.primary))
        Row(Modifier.fillMaxWidth().padding(0.dp, 7.dp).clip(RoundedCornerShape(15.dp)).background(MaterialTheme.colorScheme.surfaceContainerHigh).clickable {
           pendingGrade = "Modes"
        }.padding(10.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Modes", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Medium))
            Icon(Icons.Outlined.ChevronRight, "Open")
        }
        ColorCard(gradeColors.AColor, "A", top = true) {
            pendingGrade = "A"
        }
        ColorCard(gradeColors.BColor, "B") {
            pendingGrade = "B"
        }
        ColorCard(gradeColors.CColor, "C") {
            pendingGrade = "C"
        }
        ColorCard(gradeColors.DColor, "D") {
            pendingGrade = "D"
        }
        ColorCard(gradeColors.EColor, "E", bottom = true) {
            pendingGrade = "E"
        }
        val sheetState = rememberModalBottomSheetState( skipPartiallyExpanded = true )
        if (pendingGrade != null && pendingGrade != "Modes") {
            ModalBottomSheet(
                onDismissRequest = { pendingGrade = null },
                sheetState = sheetState,
            ) {
                Box(Modifier.fillMaxWidth().aspectRatio(1f).padding(20.dp)) {
                    val color = when (pendingGrade) {
                        "A" -> gradeColors.AColor
                        "B" -> gradeColors.BColor
                        "C" -> gradeColors.CColor
                        "D" -> gradeColors.DColor
                        "E" -> gradeColors.EColor
                        else -> Color.Black
                    }
                    ClassicColorPicker(color = HsvColor.from(color)) {
                        when (pendingGrade) {
                            "A" -> gradeColors = gradeColors.copy(_AColor = it.toColor().value)
                            "B" -> gradeColors = gradeColors.copy(_BColor = it.toColor().value)
                            "C" -> gradeColors = gradeColors.copy(_CColor = it.toColor().value)
                            "D" -> gradeColors = gradeColors.copy(_DColor = it.toColor().value)
                            "E" -> gradeColors = gradeColors.copy(_EColor = it.toColor().value)
                            else -> {}
                        }
                        kvault?.set(GRADE_COLORS_KEY, Json.encodeToString(gradeColors))
                    }
                }
            }
        }
        if (pendingGrade == "Modes") {
            ModalBottomSheet(
                onDismissRequest = { pendingGrade = null },
                sheetState = sheetState,
            ) {
                Column(Modifier.fillMaxWidth().aspectRatio(1f).padding(12.dp)) {
                    Row(Modifier
                        .fillMaxWidth()
                        .padding(0.dp, 5.dp)
                        .background(MaterialTheme.colorScheme.surfaceContainerHigh, RoundedCornerShape(15.dp, 15.dp, 15.dp, 15.dp))
                        .clip(RoundedCornerShape(15.dp, 15.dp, 15.dp, 15.dp))
                        .clickable {
                            gradeColors = GradeColors.default()
                            kvault?.set(GRADE_COLORS_KEY, Json.encodeToString(gradeColors))
                            pendingGrade = null
                        }.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Default Mode", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Medium))
                    }
                    Row(Modifier
                        .fillMaxWidth()
                        .padding(0.dp, 5.dp)
                        .background(MaterialTheme.colorScheme.surfaceContainerHigh, RoundedCornerShape(15.dp, 15.dp, 15.dp, 15.dp))
                        .clip(RoundedCornerShape(15.dp, 15.dp, 15.dp, 15.dp))
                        .clickable {
                            gradeColors = GradeColors.georgeMode()
                            kvault?.set(GRADE_COLORS_KEY, Json.encodeToString(gradeColors))
                            pendingGrade = null
                        }.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("George Mode", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Medium))
                    }
                    Row(Modifier
                        .fillMaxWidth()
                        .padding(0.dp, 5.dp)
                        .background(MaterialTheme.colorScheme.surfaceContainerHigh, RoundedCornerShape(15.dp, 15.dp, 15.dp, 15.dp))
                        .clip(RoundedCornerShape(15.dp, 15.dp, 15.dp, 15.dp))
                        .clickable {
                            gradeColors = GradeColors.fionaMode()
                            kvault?.set(GRADE_COLORS_KEY, Json.encodeToString(gradeColors))
                            pendingGrade = null
                        }.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Fiona Mode", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Medium))
                    }
                }
            }
        }
    }
}

@Composable
fun ColorCard(color: Color, letter: String, top: Boolean = false, bottom: Boolean = false, clicked: () -> Unit) {
    val shape = RoundedCornerShape(if (top) 15.dp else 5.dp, if (top) 15.dp else 5.dp, if (bottom) 15.dp else 5.dp, if (bottom) 15.dp else 5.dp)
    Row(Modifier
        .fillMaxWidth()
        .padding(0.dp, 2.dp)
        .background(MaterialTheme.colorScheme.surfaceContainerHigh, shape)
        .clip(shape)
        .clickable { clicked() }
        .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween) {
        Text("$letter Color", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Medium))
        Box(Modifier.size(with(LocalDensity.current) { MaterialTheme.typography.titleLarge.fontSize.toDp() }).background(color))
    }
}