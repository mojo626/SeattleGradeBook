package com.chrissytopher.source

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChevronLeft
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.datetime.LocalDateTime
import net.sergeych.sprintf.sprintf

@Composable
fun AssignmentScreen( ) {
    val currentAssignment by AssignmentForPage.current
    val navHost = LocalNavHost.current

    var goBack by remember { mutableStateOf(false) }
    LaunchedEffect (goBack) {
        if (goBack) {
            navHost?.navigateUp()
        }
    }

    val newestScore = currentAssignment?._assignmentscores?.maxByOrNull { LocalDateTime.parse(it.scoreentrydate) }

    Column (Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {

        Row( verticalAlignment = Alignment.CenterVertically ) {
            IconButton({ goBack = true }) {
                Icon(Icons.Outlined.ChevronLeft, contentDescription = "left arrow", modifier = Modifier.padding(5.dp))
            }

            Text("Assignment Details", style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f).offset(x= -12.dp).padding(5.dp), textAlign = TextAlign.Center)
        }

        Text(currentAssignment!!.name, style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(20.dp))
        Text("${newestScore?.scorepoints?.let { "${it * currentAssignment!!.weight} / ${currentAssignment!!.totalpointvalue}" } ?: "-"} (${newestScore?.scorepercent?.let {"${"%.2f".sprintf(it)}%"} ?: "-"})")

    }


}