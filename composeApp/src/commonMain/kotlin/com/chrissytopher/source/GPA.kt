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


@Composable
fun GPAScreen() {
    val kvault = LocalKVault.current
    val json = LocalJson.current
    val sourceDataState = LocalSourceData.current
    val navHost = LocalNavHost.current

    var currClasses = sourceDataState?.value?.classes

    //PastClass(date_completed=01/2020, grade_level=6, school=Robert Eagle Staff MS, course_id=HWL1275, course_name=JAPANESE 1A, credit_earned=0.25, credit_attempted=0.25, grade=A <b></b>)
    var pastClasses = sourceDataState?.value?.past_classes
    Napier.d("Helloladjflasdjflasjdf;lkasjdf;lajsd;flaljk")
    Napier.d(pastClasses?.get(0).toString())


    var unweighted_gpa = 0.0
    var total_classes = 0

    pastClasses?.forEachIndexed { i, it ->
        when (it.grade.removeRange(it.grade.indexOf('<'), it.grade.length)) {
            "A" -> unweighted_gpa += 4.0
            "A-" -> unweighted_gpa += 3.7
            "B+" -> unweighted_gpa += 3.3
            "B" -> unweighted_gpa += 3.0
            "B-" -> unweighted_gpa += 2.7
            "C+" -> unweighted_gpa += 2.3
            "C" -> unweighted_gpa += 2.0
            "C-" -> unweighted_gpa += 1.7
            "D+" -> unweighted_gpa += 1.3
            "D" -> unweighted_gpa += 1.0
            else -> {
                unweighted_gpa += 0.0
            }
        }
        total_classes ++
    }

    currClasses?.forEachIndexed { i, it ->
        when (ClassMeta(it).grade) {
            "A" -> unweighted_gpa += 4.0
            "A-" -> unweighted_gpa += 3.7
            "B+" -> unweighted_gpa += 3.3
            "B" -> unweighted_gpa += 3.0
            "B-" -> unweighted_gpa += 2.7
            "C+" -> unweighted_gpa += 2.3
            "C" -> unweighted_gpa += 2.0
            "C-" -> unweighted_gpa += 1.7
            "D+" -> unweighted_gpa += 1.3
            "D" -> unweighted_gpa += 1.0
            else -> {
                unweighted_gpa += 0.0
            }
        }
        total_classes ++
    }

    unweighted_gpa /= total_classes

    

    Column ( modifier = Modifier.verticalScroll(rememberScrollState()) ) {
        Text(unweighted_gpa.toString())


        pastClasses?.forEachIndexed { i, it ->
            if (it.credit_attempted > 0) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp)
                        .border(2.dp, SolidColor(Color.Black),shape = RoundedCornerShape(15.dp))
                ) {
                    Row() {
                        Text(it.course_name, modifier = Modifier.padding(10.dp))
                        Spacer( modifier = Modifier.weight(1f) )
                        Text(it.grade.removeRange(it.grade.indexOf('<'), it.grade.length), modifier = Modifier.padding(10.dp))
                    }
                    
                }
            }
            
        }
    }
}