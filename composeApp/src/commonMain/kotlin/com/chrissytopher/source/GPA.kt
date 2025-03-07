package com.chrissytopher.source

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.Row
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.foundation.border
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.TabRow
import androidx.compose.material3.Tab
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.Alignment
import androidx.compose.runtime.key
import net.sergeych.sprintf.*
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.CardDefaults
import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State


@Composable
fun GPAScreen(viewModel: AppViewModel) {
    val sourceDataState = viewModel.sourceData()
    val selectedQuarter by viewModel.selectedQuarter()

    val currClasses = sourceDataState.value?.get(selectedQuarter)?.classes

    //PastClass(date_completed=01/2020, grade_level=6, school=Robert Eagle Staff MS, course_id=HWL1275, course_name=JAPANESE 1A, credit_earned=0.25, credit_attempted=0.25, grade=A <b></b>)
    val pastClasses = sourceDataState.value?.get(selectedQuarter)?.past_classes
    var ignoreClasses: List<String> by remember { mutableStateOf(listOf()) }

    var gpaSelector by remember { mutableStateOf(0) }

    Column ( modifier = Modifier.verticalScroll(rememberScrollState()) ) {
        TabRow( selectedTabIndex = gpaSelector ) {
            Tab(text = {Text("Unweighted GPA")}, selected = gpaSelector == 0, onClick = { gpaSelector = 0 })
            Tab(text = {Text("Weighted GPA")}, selected = gpaSelector == 1, onClick = { gpaSelector = 1 })
        }
        Box( contentAlignment = Alignment.Center, modifier = Modifier.fillMaxWidth() )
        {
            key(ignoreClasses) {
                val (unweightedGpa, weightedGpa) = key(currClasses) {
                    remember { calculateGpas(currClasses, pastClasses, ignoreClasses) }
                }
                Text("%.3f".sprintf((if (gpaSelector == 0) unweightedGpa else weightedGpa)) + if (ignoreClasses.isEmpty()) "" else "*", fontSize = 70.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, modifier = Modifier.padding(20.dp))
            }
        }

        val grades = key(currClasses) { remember { currClasses?.map { ClassMeta(it).grade } } }
        val currentGrade = sourceDataState.value?.get(selectedQuarter)?.grade_level
        val gradeColors by viewModel.gradeColors()

        currClasses?.forEachIndexed { i, it ->
            val disabled = remember { mutableStateOf(false) }
            GradeCard(it.name, grades?.getOrNull(i) ?: "",currentGrade ?: "", disabled, gradeColors) {
                disabled.value = !disabled.value
                if (ignoreClasses.contains(it.frn)) {
                    ignoreClasses = ignoreClasses - it.frn
                } else {
                    ignoreClasses = ignoreClasses + it.frn
                }
            }
        }

        pastClasses?.reversed()?.forEach {
            if (it.credit_attempted > 0 && !it.grade.endsWith("<b>*</b>")) {
                val disabled = remember { mutableStateOf(false) }
                GradeCard(it.course_name, it.grade, it.grade_level, disabled, gradeColors) {
                    disabled.value = !disabled.value
                    if (ignoreClasses.contains(it.course_id)) {
                        ignoreClasses = ignoreClasses - it.course_id
                    } else {
                        ignoreClasses = (ignoreClasses + it.course_id).toMutableList()
                    }
                }
            }
        }
    }
}

@Composable
fun GradeCard(courseName: String, grade: String, gradeLevel: String, disabledState: State<Boolean>, gradeColors: GradeColors, onClick: (() -> Unit)? = null) {
    val inner = @Composable {
        Row {
            Text(courseName, modifier = Modifier.padding(10.dp))
            Spacer( modifier = Modifier.weight(1f) )

            Text(gradeLevel + "th", modifier = Modifier.padding(top = 10.dp, bottom = 10.dp, start = 10.dp, end = 20.dp))

            Text(grade.removeSuffix(" <b></b>"), modifier = Modifier.padding(10.dp))
        }
    }
    val disabled by disabledState
    val colors = gradeColors.gradeColor(grade.removeSuffix(" <b></b>").firstOrNull().toString())?.let {
        if (disabled) {
            null
        } else {
            CardDefaults.cardColors(
                containerColor = it * darkModeColorModifier(),
            )
        }
    } ?: CardDefaults.cardColors()

    val modifier = Modifier.padding(5.dp)
    if (onClick == null) {
        Card(
            colors = colors,
            modifier = modifier,
        ) {
            inner()
        }
    } else {
        Card(
            colors = colors,
            modifier = modifier,
            onClick = onClick
        ) {
            inner()
        }
    }
}

private fun calculateGpas(currClasses: List<Class>?, pastClasses: List<PastClass>?, ignoreClasses: List<String>): Pair<Double, Double> {
    var unweightedGpa = 0.0
    var weightedAdditions = 0.0
    var totalClasses = 0

    pastClasses?.forEach {
        if (ignoreClasses.contains(it.course_id)) return@forEach
        if (it.credit_attempted > 0) {
            unweightedGpa += when (it.grade.removeSuffix(" <b></b>")) {
                "A" -> {  4.0 }
                "A-" -> { 3.7 }
                "B+" -> { 3.3 }
                "B" -> { 3.0 }
                "B-" -> { 2.7 }
                "C+" -> { 2.3 }
                "C" -> { 2.0 }
                "C-" -> { 1.7 }
                "D+" -> { 1.3 }
                "D" -> { 1.0 }
                else -> {
                    return@forEach
                }
            }

            if (it.course_name.startsWith("AP ")) {
                weightedAdditions += 1.0
            } else if (it.course_name.endsWith(" H")) {
                weightedAdditions += 0.5
            }
            totalClasses ++
        }
    }

    val metas = currClasses?.map { ClassMeta(it) }

    currClasses?.forEachIndexed { i, it ->
        if (ignoreClasses.contains(it.frn)) return@forEachIndexed
        if (metas?.getOrNull(i)?.grade != null) {
            unweightedGpa += when (metas[i].grade.toString()) {
                "A" -> { 4.0 }
                "A-" -> { 3.7 }
                "B+" -> { 3.3 }
                "B" -> { 3.0 }
                "B-" -> { 2.7 }
                "C+" -> { 2.3 }
                "C" -> { 2.0 }
                "C-" -> { 1.7 }
                "D+" -> { 1.3 }
                "D" -> { 1.0 }
                else -> {
                    return@forEachIndexed
                }
            }

            if (it.name.startsWith("AP ")) {
                weightedAdditions += 1.0
            } else if (it.name.endsWith(" H")) {
                weightedAdditions += 0.5
            }
            totalClasses ++
        }
    }

    unweightedGpa /= totalClasses
    weightedAdditions /= totalClasses
    return Pair(unweightedGpa, unweightedGpa + weightedAdditions)
}
