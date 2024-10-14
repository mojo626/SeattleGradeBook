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
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.key
import io.github.aakira.napier.Napier
import kotlin.math.round


@Composable
fun GPAScreen() {
    val sourceDataState = LocalSourceData.current

    val currClasses = sourceDataState.value?.classes

    //PastClass(date_completed=01/2020, grade_level=6, school=Robert Eagle Staff MS, course_id=HWL1275, course_name=JAPANESE 1A, credit_earned=0.25, credit_attempted=0.25, grade=A <b></b>)
    val pastClasses = sourceDataState.value?.past_classes

    val (unweightedGpa, weightedGpa) = key(currClasses) {
        remember { calculateGpas(currClasses, pastClasses) }
    }

    var gpaSelector by remember { mutableStateOf(0) }

    Column ( modifier = Modifier.verticalScroll(rememberScrollState()) ) {
        TabRow( selectedTabIndex = gpaSelector ) {
            Tab(text = {Text("Unweighted GPA")}, selected = gpaSelector == 0, onClick = { gpaSelector = 0 })
            Tab(text = {Text("Weighted GPA")}, selected = gpaSelector == 1, onClick = { gpaSelector = 1 })
        }
        Box( contentAlignment = Alignment.Center, modifier = Modifier.fillMaxWidth() )
        {
            Text((if (gpaSelector == 0) unweightedGpa else weightedGpa).round(2).toString(), fontSize = 70.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, modifier = Modifier.padding(20.dp))
        }

        pastClasses?.reversed()?.forEach {
            if (it.credit_attempted > 0 && it.grade.removeRange(it.grade.indexOf('<'), it.grade.length) != "P*") {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp)
                        .border(2.dp, SolidColor(Color.Black),shape = RoundedCornerShape(15.dp))
                ) {
                    Row() {
                        Text(it.course_name, modifier = Modifier.padding(10.dp))
                        Spacer( modifier = Modifier.weight(1f) )

                        Text(it.grade_level + "th", modifier = Modifier.padding(top = 10.dp, bottom = 10.dp, start = 10.dp, end = 20.dp))

                        Text(it.grade.removeRange(it.grade.indexOf('<'), it.grade.length), modifier = Modifier.padding(10.dp))
                    }
                }
            }
        }
    }
}

private fun calculateGpas(currClasses: List<Class>?, pastClasses: List<PastClass>?): Pair<Double, Double> {
    var unweightedGpa = 0.0
    var weightedAdditions = 0.0
    var totalClasses = 0

    pastClasses?.forEach {
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
        if (metas?.get(i)?.grade != null)
        {
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

fun Double.round(decimals: Int): Double {
    var multiplier = 1.0f
    repeat(decimals) { multiplier *= 10 }
    return round(this * multiplier) / multiplier
}