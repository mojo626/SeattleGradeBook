package com.chrissytopher.source

import androidx.compose.foundation.background
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
import androidx.compose.material3.IconButton
import androidx.compose.material.icons.outlined.ChevronLeft
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material.icons.filled.Chair
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.IncompleteCircle
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.outlined.Chair
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Error
import androidx.compose.material.icons.outlined.HideSource
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import io.github.aakira.napier.Napier
import kotlinx.serialization.encodeToString
import net.sergeych.sprintf.*
import kotlin.math.round
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GradesScreen() {
    val currentClass by ClassForGradePage.current
    val platform = LocalPlatform.current
    val kvault = LocalKVault.current
    val json = LocalJson.current

    LaunchedEffect(currentClass) {
        var updateClasses = kvault?.string(CLASS_UPDATES_KEY)?.let { json.decodeFromString<List<String>>(it) } ?: listOf()
        currentClass?.name?.let { currentName -> updateClasses = updateClasses.filter { it != currentName } }
        kvault?.set(CLASS_UPDATES_KEY, json.encodeToString(updateClasses))
    }

    val navHost = LocalNavHost.current

    var openedAssignment: Pair<AssignmentSection, AssignmentScore?>? by remember { mutableStateOf(null) }

    if (currentClass == null) {
        navHost?.popStack()
        return
    }
    val meta = key(currentClass) {
        remember { ClassMeta(currentClass!!) }
    }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        Box(Modifier.fillMaxWidth()) {
            Row(Modifier.align(Alignment.CenterStart)) {
                Spacer(Modifier.width(20.dp))
                IconButton({ navHost?.popStack() }) {
                    Icon(Icons.Outlined.ChevronLeft, contentDescription = "left arrow", modifier = Modifier.padding(5.dp))
                }
            }

            Text(currentClass!!.name, style = MaterialTheme.typography.titleLarge, modifier = Modifier.align(Alignment.Center).padding(5.dp), textAlign = TextAlign.Center)
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
            assignmentsSorted?.forEach { assignment ->
                AssignmentCard(assignment, if (showPercent) ScoreDisplay.Percent else ScoreDisplay.Points) {
                    val newestSection = assignment._assignmentsections.maxByOrNull { LocalDate.parse(it.duedate) } ?: return@AssignmentCard
                    val newestScore = newestSection._assignmentscores.maxByOrNull { LocalDateTime.parse(it.scoreentrydate) }
                    openedAssignment = Pair(newestSection, newestScore)
                }
            }
        }

        val sheetState = rememberModalBottomSheetState( skipPartiallyExpanded = true )

        openedAssignment?.let { (newestSection, newestScore) ->
            ModalBottomSheet(
                onDismissRequest = { openedAssignment = null },
                sheetState = sheetState,
            ) {
                AssignmentCard(newestSection, newestScore, ScoreDisplay.Both, null)

                newestScore?.scorepoints?.let {
                    Slider(
                        value = it,
                        onValueChange = {
                            val rounded = it.roundToInt().toFloat()
                            val changedScore = newestScore.copy(scorepoints = rounded, scorelettergrade = gradeForScore((rounded/newestSection.totalpointvalue)*100f))
                            openedAssignment = Pair(newestSection, changedScore)
                        },
                        valueRange = 0.0f..(newestSection.totalpointvalue),
                        modifier = Modifier.padding(horizontal = 20.dp)
                    )
                }

                Text(
                    "Effect on Grade",
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(20.dp, top = 20.dp, bottom = 0.dp),
                    fontSize = 25.sp
                )

                val withoutAssignment = ClassMeta(currentClass!!, newestSection._id)
                val withAssignment = newestScore?.scorepoints?.let {
                    ClassMeta(currentClass!!, newestSection, newestScore)
                } ?: withoutAssignment
                val percentChange = (withAssignment.finalScore ?: 0.0f) - (withoutAssignment.finalScore ?: 0.0f)

                Text(
                    "${if (percentChange >= 0.0) "+" else {""}}${"%.2f".sprintf(percentChange)}% ${withAssignment.finalScore?.let { "(${it}%)" } ?: ""}",
                    modifier = Modifier.padding(20.dp, top = 10.dp),
                    fontSize = 20.sp
                )

                newestScore?.let {
                    Text(
                        "Flags",
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(20.dp, top = 20.dp, bottom = 0.dp),
                        fontSize = 25.sp
                    )

                    Row(Modifier.padding(20.dp, bottom = 0.dp)) {
                        IconButton(onClick = {
                            val changedScore = newestScore.copy(iscollected = !it.iscollected)
                            openedAssignment = Pair(newestSection, changedScore)
                        }, colors = IconButtonDefaults.iconButtonColors(containerColor = if (it.iscollected) greenColor else CardDefaults.cardColors().containerColor, contentColor = MaterialTheme.colorScheme.inverseOnSurface)) {
                            Icon(Icons.Filled.Check, "Collected")
                        }
                        IconButton(onClick = {
                            val changedScore = newestScore.copy(islate = !it.islate)
                            openedAssignment = Pair(newestSection, changedScore)
                        }, colors = IconButtonDefaults.iconButtonColors(containerColor = if (it.islate) redColor else CardDefaults.cardColors().containerColor, contentColor = MaterialTheme.colorScheme.inverseOnSurface)) {
                            Icon(Icons.Filled.Schedule, "Late")
                        }
                        IconButton(onClick = {
                            val changedScore = newestScore.copy(ismissing = !it.ismissing)
                            openedAssignment = Pair(newestSection, changedScore)
                        }, colors = IconButtonDefaults.iconButtonColors(containerColor = if (it.ismissing) orangeColor else CardDefaults.cardColors().containerColor, contentColor = MaterialTheme.colorScheme.inverseOnSurface)) {
                            Icon(Icons.Outlined.Error, "Missing")
                        }
                        IconButton(onClick = {
                            val changedScore = newestScore.copy(isexempt = !it.isexempt)
                            openedAssignment = Pair(newestSection, changedScore)
                        }, colors = IconButtonDefaults.iconButtonColors(containerColor = if (it.isexempt) Color(0xffa218e7) else CardDefaults.cardColors().containerColor, contentColor = MaterialTheme.colorScheme.inverseOnSurface)) {
                            Icon(Icons.Outlined.HideSource, "Exempt")
                        }
                        IconButton(onClick = {
                            val changedScore = newestScore.copy(isabsent = !it.isabsent)
                            openedAssignment = Pair(newestSection, changedScore)
                        }, colors = IconButtonDefaults.iconButtonColors(containerColor = if (it.isabsent) greenColor else CardDefaults.cardColors().containerColor, contentColor = MaterialTheme.colorScheme.inverseOnSurface)) {
                            Icon(Icons.Filled.Chair, "Absent")
                        }
                        IconButton(onClick = {
                            val changedScore = newestScore.copy(isincomplete = !it.isincomplete)
                            openedAssignment = Pair(newestSection, changedScore)
                        }, colors = IconButtonDefaults.iconButtonColors(containerColor = if (it.isincomplete) blueColor else CardDefaults.cardColors().containerColor, contentColor = MaterialTheme.colorScheme.inverseOnSurface)) {
                            Icon(Icons.Filled.IncompleteCircle, "Incomplete")
                        }
                        IconButton(onClick = {
                            val changedSection = newestSection.copy(iscountedinfinalgrade = !newestSection.iscountedinfinalgrade)
                            openedAssignment = Pair(changedSection, it)
                        }, colors = IconButtonDefaults.iconButtonColors(containerColor = if (!newestSection.iscountedinfinalgrade) orangeColor else CardDefaults.cardColors().containerColor, contentColor = MaterialTheme.colorScheme.inverseOnSurface)) {
                            Icon(Icons.Rounded.Star, "Incomplete")
                        }
                    }
                }

                Spacer(Modifier.height(20.dp))

                HorizontalDivider(thickness = 2.dp)

                Text(
                    "Due Date",
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(20.dp, top = 20.dp, bottom = 0.dp),
                    fontSize = 25.sp
                )

                val date = LocalDateTime.parse("${newestSection.duedate}T12:00:00")

                Text("${date.month} ${date.dayOfMonth}, ${date.year}",
                    modifier = Modifier.padding(20.dp, top = 10.dp),
                    fontSize = 20.sp
                )

                Spacer(Modifier.height(20.dp))
            }
        }
    }
}

enum class ScoreDisplay {
    Percent,
    Points,
    Both
}

@Composable
fun AssignmentCard(assignment: Assignment, showPercent: ScoreDisplay, onClick: (() -> Unit)?) {
    val newestSection =
        assignment._assignmentsections.maxByOrNull { LocalDate.parse(it.duedate) }
    val newestScore = newestSection?._assignmentscores?.maxByOrNull { LocalDateTime.parse(it.scoreentrydate) }
    if (newestSection != null) {
        AssignmentCard(newestSection, newestScore, showPercent, onClick)
    }
}

@Composable
fun AssignmentCard(section: AssignmentSection, score: AssignmentScore?, showPercent: ScoreDisplay, onClick: (() -> Unit)?) {
    val themeModifier = darkModeColorModifier()
    val kvault = LocalKVault.current
    val isGeorge = remember { kvault?.string(USERNAME_KEY) == "1gdschneider" }
    val colorsList = if (isGeorge) {
        georgeGradeColors
    } else {
        gradeColors
    }
    val colors = if (section.iscountedinfinalgrade) {
        score?.scorelettergrade?.firstOrNull()?.let {
            colorsList[it.toString()]?.let {
                CardDefaults.cardColors(
                    containerColor = it*themeModifier
                )
            }
        } ?: CardDefaults.cardColors()
    } else {
        CardDefaults.cardColors()
    }
    Card(modifier = Modifier.padding(5.dp).then(onClick?.let { Modifier.clickable(onClick = onClick) } ?: Modifier), colors = colors) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(10.dp)) {
            val localDensity = LocalDensity.current
            Text(score?.scorelettergrade ?: "", modifier = Modifier.width( with (localDensity) { MaterialTheme.typography.titleLarge.fontSize.toDp()*1.5f } ), style = MaterialTheme.typography.titleLarge)
            Text(section.name, style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f), maxLines = 2, overflow = TextOverflow.Ellipsis)
            Text(
                when (showPercent) {
                    ScoreDisplay.Percent -> score?.scorepercent?.let {"${"%.2f".sprintf(it)}%"} ?: "-"
                    ScoreDisplay.Points -> score?.scorepoints?.let { "${it * section.weight} / ${section.totalpointvalue}" } ?: "-"
                    ScoreDisplay.Both -> score?.scorepoints?.let { "${it * section.weight} / ${section.totalpointvalue} (${"%.2f".sprintf((it * section.weight / section.totalpointvalue)*100f)}%)" } ?: "-"
                },
                style = MaterialTheme.typography.titleLarge)
        }

    }
}