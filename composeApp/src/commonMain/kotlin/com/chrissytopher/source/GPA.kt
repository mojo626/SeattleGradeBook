package com.chrissytopher.source

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.unit.dp
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.foundation.border 
import io.github.aakira.napier.Napier
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.TabRow
import androidx.compose.material.Tab
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue



@Composable
fun GPAScreen() {
    val kvault = LocalKVault.current
    val json = LocalJson.current
    val sourceDataState = LocalSourceData.current
    val navHost = LocalNavHost.current

    var currClasses = sourceDataState?.value?.classes

    //PastClass(date_completed=01/2020, grade_level=6, school=Robert Eagle Staff MS, course_id=HWL1275, course_name=JAPANESE 1A, credit_earned=0.25, credit_attempted=0.25, grade=A <b></b>)
    var pastClasses = sourceDataState?.value?.past_classes



    var unweighted_gpa = 0.0
    var weighted_gpa = 0.0
    var total_classes = 0

    pastClasses?.forEachIndexed { i, it ->
        if (it.credit_attempted > 0 && it.grade.removeRange(it.grade.indexOf('<'), it.grade.length) != "P*") {
            when (it.grade.removeRange(it.grade.indexOf('<'), it.grade.length)) {
                "A" -> { unweighted_gpa += 4.0; weighted_gpa += 4.0 }
                "A-" -> { unweighted_gpa += 3.7; weighted_gpa += 4.0 }
                "B+" -> { unweighted_gpa += 3.3; weighted_gpa += 4.0 }
                "B" -> { unweighted_gpa += 3.0; weighted_gpa += 4.0 }
                "B-" -> { unweighted_gpa += 2.7; weighted_gpa += 4.0 }
                "C+" -> { unweighted_gpa += 2.3; weighted_gpa += 4.0 }
                "C" -> { unweighted_gpa += 2.0; weighted_gpa += 4.0 }
                "C-" -> { unweighted_gpa += 1.7; weighted_gpa += 4.0 }
                "D+" -> { unweighted_gpa += 1.3; weighted_gpa += 4.0 }
                "D" -> { unweighted_gpa += 1.0; weighted_gpa += 4.0 }
                else -> {
                    unweighted_gpa += 0.0
                }
            }

            if (it.course_name.get(0) == 'A' && it.course_name.get(1) == 'P')
            {
                weighted_gpa += 1.0
            } else if (it.course_name.get(it.course_name.length - 1) == 'H')
            {
                weighted_gpa += 0.5
            }
            total_classes ++
        }
    }

    currClasses?.forEachIndexed { i, it ->
        when (ClassMeta(it).grade) {
            "A" -> { unweighted_gpa += 4.0; weighted_gpa += 4.0 }
            "A-" -> { unweighted_gpa += 3.7; weighted_gpa += 4.0 }
            "B+" -> { unweighted_gpa += 3.3; weighted_gpa += 4.0 }
            "B" -> { unweighted_gpa += 3.0; weighted_gpa += 4.0 }
            "B-" -> { unweighted_gpa += 2.7; weighted_gpa += 4.0 }
            "C+" -> { unweighted_gpa += 2.3; weighted_gpa += 4.0 }
            "C" -> { unweighted_gpa += 2.0; weighted_gpa += 4.0 }
            "C-" -> { unweighted_gpa += 1.7; weighted_gpa += 4.0 }
            "D+" -> { unweighted_gpa += 1.3; weighted_gpa += 4.0 }
            "D" -> { unweighted_gpa += 1.0; weighted_gpa += 4.0 }
            else -> {
                unweighted_gpa += 0.0
            }
        }

        if (it.name.get(0) == 'A' && it.name.get(1) == 'P')
        {
            weighted_gpa += 1.0
        } else if (it.name.get(it.name.length - 1) == 'H')
        {
            weighted_gpa += 0.5
        }
        total_classes ++
    }

    unweighted_gpa /= total_classes
    weighted_gpa /= total_classes

    var gpa_selector by remember { mutableStateOf(0) }

    

    Column ( modifier = Modifier.verticalScroll(rememberScrollState()) ) {
        TabRow( selectedTabIndex = gpa_selector ) {
            Tab(text = {Text("Unweighted GPA")}, selected = gpa_selector == 0, onClick = { gpa_selector = 0 })
            Tab(text = {Text("Weighted GPA")}, selected = gpa_selector == 1, onClick = { gpa_selector = 1 })
        }
        Text(if (gpa_selector == 0) unweighted_gpa.toString() else weighted_gpa.toString())


        pastClasses?.forEachIndexed { i, it ->
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