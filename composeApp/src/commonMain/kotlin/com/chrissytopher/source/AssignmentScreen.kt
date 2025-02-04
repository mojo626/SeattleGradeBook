package com.chrissytopher.source

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Block
import androidx.compose.material.icons.outlined.ChairAlt
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.ChevronLeft
import androidx.compose.material.icons.outlined.Error
import androidx.compose.material.icons.outlined.IncompleteCircle
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.datetime.LocalDateTime
import net.sergeych.sprintf.sprintf
import kotlin.math.round

@Composable
fun AssignmentScreen() {
    val currentAssignment by AssignmentForPage.current
    val currentClass by ClassForGradePage.current
    val navHost = LocalNavHost.current
    val screenSize = getScreenSize()

    val newestScore = currentAssignment?._assignmentscores?.maxByOrNull { LocalDateTime.parse(it.scoreentrydate) }

    Column (Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {

        Row( verticalAlignment = Alignment.CenterVertically ) {
            IconButton({ navHost?.popStack(animateWidth = screenSize.width.toFloat()) }) {
                Icon(Icons.Outlined.ChevronLeft, contentDescription = "left arrow", modifier = Modifier.padding(5.dp))
            }

            Text("Assignment Details", style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f).offset(x= -12.dp).padding(5.dp), textAlign = TextAlign.Center)
        }

        Text(currentAssignment!!.name, fontSize = 30.sp, modifier = Modifier.padding(20.dp))

        Row() {
            Text("${newestScore?.scorepoints?.let { "${it * currentAssignment!!.weight} / ${currentAssignment!!.totalpointvalue}" } ?: "-"} (${newestScore?.scorepercent?.let {"${"%.2f".sprintf(it)}%"} ?: "-"})",
                modifier = Modifier.padding(20.dp, top = 0.dp, bottom = 20.dp),
                fontSize = 20.sp
            )

            Spacer( modifier = Modifier.weight(1f) )

            if (newestScore != null)
            {
                if (newestScore.iscollected) {
                    Icon(Icons.Outlined.Check, contentDescription = "Work is collected")
                }
                if (newestScore.islate) {
                    Icon(Icons.Outlined.Schedule, contentDescription = "Work is Late")
                }
                if (newestScore.isabsent) {
                    Icon(Icons.Outlined.ChairAlt, contentDescription = "Student was Absent")
                }
                if (newestScore.isexempt) {
                    Icon(Icons.Outlined.Block, contentDescription = "Grade is Exempt")
                }
                if (newestScore.ismissing) {
                    Icon(Icons.Outlined.Error, contentDescription = "Assignment is Missing")
                }
                if (newestScore.isincomplete) {
                    Icon(Icons.Outlined.IncompleteCircle, contentDescription = "Assignment is Missing")
                }
            }

        }


        HorizontalDivider(thickness = 2.dp)

        Text(
            "Due Date",
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(20.dp, top = 20.dp, bottom = 0.dp),
            fontSize = 25.sp
        )

        val date = LocalDateTime.parse("${currentAssignment!!.duedate}T12:00:00")

        Text(
            "${date.month.toString()} ${date.dayOfMonth}, ${date.year}",
            modifier = Modifier.padding(20.dp, top = 10.dp),
            fontSize = 20.sp
        )

        Text(
            "Course",
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(20.dp, top = 20.dp, bottom = 0.dp),
            fontSize = 25.sp
        )

        Text(
            currentClass!!.name,
            modifier = Modifier.padding(20.dp, top = 10.dp),
            fontSize = 20.sp
        )

        Text(
            "Effect on Grade",
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(20.dp, top = 20.dp, bottom = 0.dp),
            fontSize = 25.sp
        )

//        val withoutAssignment = ClassMeta(currentClass!!, currentAssignment!!._id)
        val withAssignment = ClassMeta(currentClass!!)
//        val percentChange = (withAssignment.finalScore ?: 0.0f) - (withoutAssignment.finalScore ?: 0.0f)

//        Text(
//            "${if (percentChange >= 0.0) "+" else {""}}${"%.2f".sprintf(percentChange)}%",
//            modifier = Modifier.padding(20.dp, top = 10.dp),
//            fontSize = 20.sp
//        )

        var sliderVal by remember { mutableStateOf((newestScore?.scorepoints ?: 0.0f) * (currentAssignment?.weight ?: 0.0f)) }

        Text(
            "If you got ${sliderVal}/${currentAssignment!!.totalpointvalue} (${"%.2f".sprintf(sliderVal/currentAssignment!!.totalpointvalue*100.0f)}%)",
            modifier = Modifier.padding(20.dp, top = 20.dp),
            fontSize = 20.sp
        )

        Slider(
            value = sliderVal,
            onValueChange = {
                sliderVal = round(it*10)/10.0f
            },
            valueRange = 0.0f..(currentAssignment!!.totalpointvalue),
            modifier = Modifier.padding(horizontal = 20.dp)
        )

//        val classGrade = ClassMeta(currentClass!!, currentAssignment!!._id)
//        val newGrade = (classGrade.earnedPoints + sliderVal) / (classGrade.totalPoints + currentAssignment!!.totalpointvalue)

//        Text(
//            "Your grade would be ${"%.2f".sprintf(newGrade * 100.0f)}%",
//            modifier = Modifier.padding(20.dp, top = 20.dp),
//            fontSize = 20.sp
//        )
    }


}

