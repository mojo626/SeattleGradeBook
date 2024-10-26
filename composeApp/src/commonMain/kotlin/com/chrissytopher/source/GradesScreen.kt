package com.chrissytopher.source

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Percent
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material.icons.outlined.ChevronLeft
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material.icons.outlined.OpenInNew
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import kotlinx.serialization.encodeToString
import net.sergeych.sprintf.*
import kotlin.math.round

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GradesScreen() {
    val currentClass by ClassForGradePage.current
    val assignmentForPage = AssignmentForPage.current
    val platform = LocalPlatform.current
    val kvault = LocalKVault.current
    val json = LocalJson.current

    LaunchedEffect(currentClass) {
        val updateClasses = kvault?.string(CLASS_UPDATES_KEY)?.let { json.decodeFromString<ArrayList<String>>(it) } ?: arrayListOf()
        currentClass?.name?.let { updateClasses.remove(it) }
        kvault?.set(CLASS_UPDATES_KEY, json.encodeToString(updateClasses))
    }

    val navHost = LocalNavHost.current

    var goToAssignment by remember { mutableStateOf<AssignmentSection?>(null) }

    var openedAssignment by remember { mutableStateOf(-1) }

    LaunchedEffect(goToAssignment)
    {
        if (goToAssignment != null)
        {
            assignmentForPage.value = goToAssignment
            navHost?.navigateTo(NavScreen.Assignments)
            goToAssignment = null
        }
    }

    if (currentClass == null) {
        navHost?.popStack()
        return
    }
    val meta = key(currentClass) {
        remember { ClassMeta(currentClass!!) }
    }

    var goBack by remember { mutableStateOf(false) }
    LaunchedEffect (goBack) {
        if (goBack) {
            navHost?.navigateTo(NavScreen.Home)
        }
    }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        Row( verticalAlignment = Alignment.CenterVertically ) {
            IconButton({ goBack = true }) {
                Icon(Icons.Outlined.ChevronLeft, contentDescription = "left arrow", modifier = Modifier.padding(5.dp))
            }
            
            Text(currentClass!!.name, style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f).offset(x= -12.dp).padding(5.dp), textAlign = TextAlign.Center)
        }
        
        Row {
            Box (modifier = Modifier.aspectRatio(1f).weight(1f).padding(10.dp)) {
                ClassCard(currentClass!!, meta, false)
            }
            Column (modifier = Modifier.aspectRatio(1f).weight(1f).padding(10.dp)) {
                Box(Modifier.fillMaxWidth().weight(1f)) {
                    Row {
                        Text(currentClass?.teacher_name ?: "Contact Teacher", style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
                        Icon(Icons.AutoMirrored.Outlined.OpenInNew, "Open teacher contact", modifier = Modifier.size(40.dp).clickable {
                            currentClass?.teacher_contact?.let { platform.openLink(it) }
                        })
                    }
                }
                Box(Modifier.background(MaterialTheme.colorScheme.primaryContainer, CardDefaults.shape).padding(10.dp).fillMaxWidth().clickable {
                    navHost?.navigateTo(NavScreen.Calculator)
                }) {
                    Text("Grade Calculator", style = MaterialTheme.typography.titleLarge)
                }
            }
        }
        var showPercent by remember { mutableStateOf(true) }
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(5.dp)) {
            Text("Assignments", style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
            Switch(
                checked = showPercent,
                onCheckedChange = { showPercent = it },
                thumbContent = {
                        Icon(
                            imageVector = Icons.Filled.Percent,
                            contentDescription = null,
                            modifier = Modifier.size(SwitchDefaults.IconSize),
                        )
                    }
            )

        }
        val assignmentsSorted = key(currentClass) {
            remember { currentClass?.assignments_parsed?.sortedByDescending { it._assignmentsections.maxOf { LocalDate.parse(it.duedate) } } }
        }
        Column(Modifier.fillMaxSize()) {
            assignmentsSorted?.forEachIndexed {index, assignment ->
                val newestSection =
                    assignment._assignmentsections.maxByOrNull { LocalDate.parse(it.duedate) }
                val newestScore = newestSection?._assignmentscores?.maxByOrNull { LocalDateTime.parse(it.scoreentrydate) }
                if (newestSection != null) {
                    val themeModifier = darkModeColorModifier()
                    val colors = if (newestSection.iscountedinfinalgrade) {
                        newestScore?.scorelettergrade?.firstOrNull()?.let {
                            gradeColors[it.toString()]?.let {
                                CardDefaults.cardColors(
                                    containerColor = it*themeModifier
                                )
                            }
                        } ?: CardDefaults.cardColors()
                    } else {
                        CardDefaults.cardColors()
                    }
                    Card(modifier = Modifier.padding(5.dp).clickable { openedAssignment = index }, colors = colors) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(10.dp)) {
                            val localDensity = LocalDensity.current
                            Text(newestScore?.scorelettergrade ?: "", modifier = Modifier.width( with (localDensity) { MaterialTheme.typography.titleLarge.fontSize.toDp()*1.5f } ), style = MaterialTheme.typography.titleLarge)
                            Text(newestSection.name, style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
                            Text(
                                if (showPercent) {
                                    newestScore?.scorepercent?.let {"${"%.2f".sprintf(it)}%"} ?: "-"
                                } else {
                                    newestScore?.scorepoints?.let { "${it * newestSection.weight} / ${newestSection.totalpointvalue}" } ?: "-"
                                },
                                style = MaterialTheme.typography.titleLarge)
                        }

                    }
                }
            }
        }

        val sheetState = rememberModalBottomSheetState( skipPartiallyExpanded = true )

        if (openedAssignment != -1)
        {
            ModalBottomSheet(
                onDismissRequest = { openedAssignment = -1 },
                sheetState = sheetState,
            ) {
                val assignment = assignmentsSorted?.get(openedAssignment)!!
                val newestSection = assignment._assignmentsections.maxByOrNull { LocalDate.parse(it.duedate) }!!
                val newestScore = newestSection._assignmentscores.maxByOrNull { LocalDateTime.parse(it.scoreentrydate) }

                Text(
                    newestSection.name,
                    fontSize = 25.sp,
                    modifier = Modifier.padding(20.dp)
                )

                Text("${newestScore?.scorepoints?.let { "${it * newestSection.weight} / ${newestSection.totalpointvalue}" } ?: "-"} (${newestScore?.scorepercent?.let {"${"%.2f".sprintf(it)}%"} ?: "-"})",
                    modifier = Modifier.padding(20.dp, top = 0.dp, bottom = 20.dp),
                    fontSize = 20.sp
                )

                HorizontalDivider(thickness = 2.dp)

                Text(
                    "Due Date",
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(20.dp, top = 20.dp, bottom = 0.dp),
                    fontSize = 25.sp
                )

                val date = LocalDateTime.parse("${newestSection.duedate}T12:00:00")

                Text("${date.month.toString()} ${date.dayOfMonth}, ${date.year}",
                    modifier = Modifier.padding(20.dp, top = 10.dp),
                    fontSize = 20.sp
                )

                Text(
                    "Effect on Grade",
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(20.dp, top = 20.dp, bottom = 0.dp),
                    fontSize = 25.sp
                )

                val withoutAssignment = ClassMeta(currentClass!!, newestSection._id)
                val withAssignment = ClassMeta(currentClass!!)
                val percentChange = (withAssignment.finalScore ?: 0.0f) - (withoutAssignment.finalScore ?: 0.0f)

                Text(
                    "${if (percentChange >= 0.0) "+" else {""}}${"%.2f".sprintf(percentChange)}%",
                    modifier = Modifier.padding(20.dp, top = 10.dp),
                    fontSize = 20.sp
                )


                var sliderVal by remember { mutableStateOf((newestScore?.scorepoints ?: 0.0f) * (newestSection.weight ?: 0.0f)) }

                Text(
                    "If you got ${sliderVal}/${newestSection.totalpointvalue} (${"%.2f".sprintf(sliderVal/newestSection.totalpointvalue*100.0f)}%)",
                    modifier = Modifier.padding(20.dp, top = 20.dp),
                    fontSize = 20.sp
                )

                Slider(
                    value = sliderVal,
                    onValueChange = {
                        sliderVal = round(it*10) /10.0f
                    },
                    valueRange = 0.0f..(newestSection.totalpointvalue),
                    modifier = Modifier.padding(horizontal = 20.dp)
                )

                val classGrade = ClassMeta(currentClass!!, newestSection._id)
                val newGrade = (classGrade.earnedPoints + sliderVal) / (classGrade.totalPoints + newestSection.totalpointvalue)

                Text(
                    "Your grade would be ${"%.2f".sprintf(newGrade * 100.0f)}%",
                    modifier = Modifier.padding(20.dp, top = 20.dp),
                    fontSize = 20.sp
                )

                Spacer(
                    modifier = Modifier.height(50.dp)
                )
            }
        }

    }
}