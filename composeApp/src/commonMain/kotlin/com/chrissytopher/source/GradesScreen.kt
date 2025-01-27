package com.chrissytopher.source

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.sp
import io.github.aakira.napier.Napier
import kotlinx.coroutines.launch
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
        launch {
            var updateClasses = kvault?.string(CLASS_UPDATES_KEY)?.let { json.decodeFromString<List<String>>(it) } ?: listOf()
            currentClass?.name?.let { currentName -> updateClasses = updateClasses.filter { it != currentName } }
            kvault?.set(CLASS_UPDATES_KEY, json.encodeToString(updateClasses))
        }
    }

    val navHost = LocalNavHost.current

    var openedAssignment: Pair<AssignmentSection, AssignmentScore?>? by remember { mutableStateOf(null) }

    if (currentClass == null) {
        navHost?.popStack(getScreenSize().width.toFloat())
        return
    }

    var meta: ClassMeta? by remember { mutableStateOf(null) }

    LaunchedEffect(currentClass) {
        launch {
            meta = ClassMeta(currentClass!!)
        }
    }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        Box(Modifier.fillMaxWidth()) {
            Row(Modifier.align(Alignment.CenterStart)) {
                Spacer(Modifier.width(20.dp))
                val screenSize = getScreenSize()
                IconButton({ navHost?.popStack(screenSize.width.toFloat()) }) {
                    Icon(Icons.Outlined.ChevronLeft, contentDescription = "left arrow", modifier = Modifier.padding(5.dp))
                }
            }

            Text(currentClass!!.name, style = MaterialTheme.typography.titleLarge, modifier = Modifier.align(Alignment.Center).padding(5.dp), textAlign = TextAlign.Center)
        }
        
        Row {
            Box (modifier = Modifier.aspectRatio(1f).weight(1f).padding(10.dp)) {
                ClassCard(currentClass!!, meta, false, true)
            }
            Column (modifier = Modifier.aspectRatio(1f).weight(1f).padding(10.dp)) {
                Box(Modifier.fillMaxWidth().weight(1f)) {
                    Row {
                        Text(currentClass?.teacher_name ?: "Contact Teacher", style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
                        Icon(Icons.AutoMirrored.Outlined.OpenInNew, "Open teacher contact", modifier = Modifier.size(40.dp).clickable {
                            kotlin.runCatching {
                                currentClass?.teacher_contact?.let { platform.openLink(it) }
                            }
                        })
                    }
                }
                val screenSize = getScreenSize()
                Box(Modifier.background(MaterialTheme.colorScheme.primaryContainer, CardDefaults.shape).padding(10.dp).fillMaxWidth().clickable {
                    navHost?.navigateTo(NavScreen.Calculator, animateWidth = screenSize.width.toFloat())
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

        var assignmentsSorted: List<Assignment>? by remember { mutableStateOf(null) }
        LaunchedEffect(currentClass) {
            launch {
                assignmentsSorted = currentClass?.assignments_parsed?.sortedByDescending { it._assignmentsections.maxOf { LocalDate.parse(it.duedate) } }
            }
        }

        Column(Modifier.fillMaxSize()) {
            assignmentsSorted?.forEach { assignment ->
                Box(Modifier.padding(5.dp)) {
                    AssignmentCard(assignment, if (showPercent) ScoreDisplay.Percent else ScoreDisplay.Points) {
                        val newestSection = assignment._assignmentsections.maxByOrNull { LocalDate.parse(it.duedate) } ?: return@AssignmentCard
                        val newestScore = newestSection._assignmentscores.maxByOrNull { LocalDateTime.parse(it.scoreentrydate) }
                        openedAssignment = Pair(newestSection, newestScore)
                    }
                }
            }
        }

        val sheetState = rememberModalBottomSheetState( skipPartiallyExpanded = true )

        openedAssignment?.let { (newestSection, newestScore) ->
            ModalBottomSheet(
                onDismissRequest = { openedAssignment = null },
                sheetState = sheetState,
            ) {
                Box(Modifier.padding(5.dp)) {
                    AssignmentCard(newestSection, newestScore, ScoreDisplay.Both, null)
                }

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
                    val gradeColors by LocalGradeColors.current
                    Row(Modifier.padding(20.dp, bottom = 0.dp)) {
                        FlagIcon(gradeColors.AColor, it.iscollected, Icons.Filled.Check, "Collected") {
                            val changedScore = newestScore.copy(iscollected = !it.iscollected)
                            openedAssignment = Pair(newestSection, changedScore)
                        }
                        FlagIcon(gradeColors.EColor, it.islate, Icons.Filled.Schedule, "Late") {
                            val changedScore = newestScore.copy(islate = !it.islate)
                            openedAssignment = Pair(newestSection, changedScore)
                        }
                        FlagIcon(gradeColors.DColor, it.ismissing, Icons.Outlined.Error, "Missing") {
                            val changedScore = newestScore.copy(ismissing = !it.ismissing)
                            openedAssignment = Pair(newestSection, changedScore)
                        }
                        FlagIcon(Color(0xffa218e7), it.isexempt, Icons.Outlined.HideSource, "Exempt") {
                            val changedScore = newestScore.copy(isexempt = !it.isexempt)
                            openedAssignment = Pair(newestSection, changedScore)
                        }
                        FlagIcon(gradeColors.AColor, it.isabsent, Icons.Filled.Chair, "Absent") {
                            val changedScore = newestScore.copy(isabsent = !it.isabsent)
                            openedAssignment = Pair(newestSection, changedScore)
                        }
                        FlagIcon(gradeColors.BColor, it.isincomplete, Icons.Filled.IncompleteCircle, "Incomplete") {
                            val changedScore = newestScore.copy(isincomplete = !it.isincomplete)
                            openedAssignment = Pair(newestSection, changedScore)
                        }
                        FlagIcon(gradeColors.DColor, !newestSection.iscountedinfinalgrade, Icons.Rounded.Star, "Incomplete") {
                            val changedSection = newestSection.copy(iscountedinfinalgrade = !newestSection.iscountedinfinalgrade)
                            openedAssignment = Pair(changedSection, it)
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

@Composable
fun FlagIcon(color: Color, selected: Boolean, icon: ImageVector, contentDescription: String?, onClick: (() -> Unit)) {
    IconButton(onClick, colors = IconButtonDefaults.iconButtonColors(containerColor = if (selected) color else CardDefaults.cardColors().containerColor, contentColor = if (selected) MaterialTheme.colorScheme.inverseOnSurface else CardDefaults.cardColors().containerColor)) {
        Icon(icon, contentDescription)
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
fun AssignmentCard(section: AssignmentSection, score: AssignmentScore?, showPercent: ScoreDisplay, onClick: (() -> Unit)?, showOutline: Boolean = true) {
    val themeModifier = darkModeColorModifier()
    val gradeColors by LocalGradeColors.current
    val colors = if (section.iscountedinfinalgrade) {
        score?.scorelettergrade?.firstOrNull()?.let {
            gradeColors.gradeColor(it.toString())?.let {
                CardDefaults.cardColors(
                    containerColor = it*themeModifier
                )
            }
        } ?: CardDefaults.cardColors()
    } else {
        CardDefaults.cardColors()
    }

    var iconColor = score?.let {
        when (true) {
            score.islate -> gradeColors.EColor
            score.ismissing -> gradeColors.DColor
            score.isexempt -> Color(0xffa218e7)
            score.isincomplete -> gradeColors.BColor
            !section.iscountedinfinalgrade -> gradeColors.DColor
            else -> null
        }
    }
    if (!showOutline) {
        iconColor = null
    }
    val iconModifier = iconColor?.let {
        Modifier.dashedBorder(3.dp, iconColor, 12.0.dp)
    } ?: Modifier
    Card(modifier = Modifier.then(onClick?.let { Modifier.clickable(onClick = onClick) } ?: Modifier).then(iconModifier), colors = colors) {
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

fun Modifier.dashedBorder(strokeWidth: Dp, color: Color, cornerRadiusDp: Dp) = composed(
    factory = {
        val density = LocalDensity.current
        val strokeWidthPx = density.run { strokeWidth.toPx() }
        val cornerRadiusPx = density.run { cornerRadiusDp.toPx() }

        this.then(
            Modifier.drawWithCache {
                onDrawBehind {
                    val stroke = Stroke(
                        width = strokeWidthPx,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                    )

                    drawRoundRect(
                        color = color,
                        style = stroke,
                        cornerRadius = CornerRadius(cornerRadiusPx)
                    )
                }
            }
        )
    }
)