package com.chrissytopher.source

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.chrisbanes.haze.hazeSource
import net.sergeych.sprintf.*


@Composable
fun GPAScreen(viewModel: AppViewModel, innerPadding: PaddingValues) {
    val sourceDataState = viewModel.sourceData()
    val selectedQuarter by viewModel.selectedQuarter()

    val currClasses = sourceDataState.value?.get(selectedQuarter)?.classes

    val pastClasses = sourceDataState.value?.get(selectedQuarter)?.past_classes
    var ignoreClasses: List<String> by remember { mutableStateOf(listOf()) }

    val gpaSelector by viewModel.gpaTypeSelectionState.collectAsState()

    val preferReported by viewModel.preferReported()

    Column(modifier = Modifier.hazeSource(viewModel.hazeState).verticalScroll(rememberScrollState()).padding(innerPadding)) {
        if (WithinApp.current) {
            TabRow( selectedTabIndex = gpaSelector, containerColor = MaterialTheme.colorScheme.surfaceContainerLow ) {
                Tab(text = {Text("Unweighted GPA")}, selected = gpaSelector == 0, onClick = { viewModel.gpaTypeSelectionState.value = 0 })
                Tab(text = {Text("Weighted GPA")}, selected = gpaSelector == 1, onClick = { viewModel.gpaTypeSelectionState.value = 1 })
            }
        }
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxWidth()) {
            key(ignoreClasses) {
                val (unweightedGpa, weightedGpa) = key(currClasses) {
                    remember { calculateGpas(currClasses, pastClasses, ignoreClasses, preferReported) }
                }
                Text("%.3f".sprintf((if (gpaSelector == 0) unweightedGpa else weightedGpa)) + if (ignoreClasses.isEmpty()) "" else "*", fontSize = 70.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, modifier = Modifier.padding(20.dp))
            }
        }

        val grades = key(currClasses) { remember { currClasses?.map { ClassMeta(it).grade } } }
        val currentGrade = sourceDataState.value?.get(selectedQuarter)?.grade_level
        val gradeColors by viewModel.gradeColors()

        currClasses?.forEachIndexed { i, it ->
            val disabled = remember { mutableStateOf(false) }
            val grade = (if (preferReported) currClasses.getOrNull(i)?.reported_grade else null) ?: grades?.getOrNull(i) ?: currClasses.getOrNull(i)?.reported_grade
            GradeCard(it.name, grade ?: "",currentGrade ?: "", disabled, gradeColors, onClick = grades?.getOrNull(i)?.let { _ -> {
                disabled.value = !disabled.value
                if (ignoreClasses.contains(it.frn)) {
                    ignoreClasses = ignoreClasses - it.frn
                } else {
                    ignoreClasses = ignoreClasses + it.frn
                }
            }})
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
        ElevatedCard(
            colors = colors,
            modifier = modifier,
            onClick = onClick,
            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 3.dp)
        ) {
            inner()
        }
    }
}

private fun calculateGpas(currClasses: List<Class>?, pastClasses: List<PastClass>?, ignoreClasses: List<String>, preferReported: Boolean): Pair<Double, Double> {
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
        val grade = (if (preferReported) currClasses.getOrNull(i)?.reported_grade else null) ?: metas?.getOrNull(i)?.grade ?: currClasses.getOrNull(i)?.reported_grade
        if (grade != null) {
            unweightedGpa += when (grade) {
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
                //TODO: E?
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
