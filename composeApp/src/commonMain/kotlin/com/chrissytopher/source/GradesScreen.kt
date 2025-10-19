package com.chrissytopher.source

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material.icons.filled.Chair
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.IncompleteCircle
import androidx.compose.material.icons.filled.Percent
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.outlined.ChevronLeft
import androidx.compose.material.icons.outlined.Error
import androidx.compose.material.icons.outlined.HideSource
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chrissytopher.source.navigation.NavigationStack
import dev.chrisbanes.haze.hazeSource
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import net.sergeych.sprintf.sprintf
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GradesScreen(viewModel: AppViewModel, navigateBack: () -> Unit, navigateTo: (NavScreen) -> Unit, innerPadding: PaddingValues) {
    val currentClassNullable by viewModel.classForGradePage.collectAsState()
    val currentClass = currentClassNullable ?: run {
        navigateBack()
        return
    }
    val platform = LocalPlatform.current

    val updatedAssignments by viewModel.updatedAssignments()
    val lockedUpdates = remember(currentClass) { updatedAssignments }
    LaunchedEffect(currentClass) {
        launch {
            viewModel.markClassRead(currentClass)
        }
    }

    var openedAssignment: Pair<AssignmentSection, AssignmentScore?>? by remember { mutableStateOf(null) }

    var meta: ClassMeta? by remember { mutableStateOf(null) }

    LaunchedEffect(currentClass) {
        launch {
            meta = ClassMeta(currentClass)
        }
    }

    Column(Modifier.hazeSource(viewModel.hazeState).fillMaxSize().verticalScroll(rememberScrollState()).padding(innerPadding).padding(6.dp)) {
        if (WithinApp.current) {
            Box(Modifier.fillMaxWidth()) {
                Row(Modifier.align(Alignment.CenterStart)) {
                    Spacer(Modifier.width(20.dp))
                    val screenSize = getScreenSize()
                    IconButton({ navigateBack() }) {
                        Icon(Icons.Outlined.ChevronLeft, contentDescription = "left arrow", modifier = Modifier.padding(5.dp))
                    }
                }

                Text(currentClass.name, style = MaterialTheme.typography.titleLarge, modifier = Modifier.align(Alignment.Center).padding(5.dp), textAlign = TextAlign.Center)
            }
        }

        val gradeColors by viewModel.gradeColors()
        Row {
            Box (modifier = Modifier.aspectRatio(1f).weight(1f).padding(10.dp)) {
                ClassCard(currentClass, meta, 0, true, gradeColors)
            }
            Column (modifier = Modifier.aspectRatio(1f).weight(1f).padding(10.dp)) {
                Box(Modifier.fillMaxWidth().weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(currentClass.teacher_name, style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
                        Icon(Icons.AutoMirrored.Outlined.OpenInNew, "Open teacher contact", modifier = Modifier.size(40.dp).clickable {
                            runCatching {
                                platform.openLink(currentClass.teacher_contact)
                            }
                        })
                    }
                }
                Box(Modifier.background(MaterialTheme.colorScheme.surfaceContainerHigh, RoundedCornerShape(15.dp)).padding(10.dp).fillMaxWidth().clickable {
                    navigateTo(NavScreen.Calculator)
                }) {
                    Text("Grade Calculator", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Medium))
                }
            }
        }
        var showPercent by remember { mutableStateOf(true) }
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(12.dp, 5.dp)) {
            Text("Assignments", style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
            Switch(
                checked = showPercent,
                onCheckedChange = { showPercent = it },
                thumbContent = {
                    Icon(
                        imageVector = Icons.Filled.Percent,
                        contentDescription = null,
                        modifier = Modifier.size(SwitchDefaults.IconSize),
                        tint = if (showPercent) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.surfaceVariant
                    )
                }
            )
        }

        var assignmentsSorted: List<Assignment>? by remember { mutableStateOf(null) }
        LaunchedEffect(currentClass) {
            launch {
                assignmentsSorted = currentClass.assignments_parsed.sortedByDescending { it._assignmentsections.maxOf { LocalDate.parse(it.duedate) } }
            }
        }

        Column(Modifier.fillMaxSize()) {
            assignmentsSorted?.forEach { assignment ->
                BadgedBox(badge = {
                    if (assignment._assignmentsections.firstOrNull()?.let { lockedUpdates[it._id] } == true) {
                        Badge(Modifier.size(15.dp), containerColor = gradeColors.EColor)
                    }
                }, modifier = Modifier.padding(0.dp, 5.dp)) {
                    AssignmentCard(assignment, if (showPercent) ScoreDisplay.Percent else ScoreDisplay.Points, gradeColors) {
                        val newestSection = assignment._assignmentsections.maxByOrNull { LocalDate.parse(it.duedate) } ?: return@AssignmentCard
                        val newestScore = newestSection._assignmentscores.maxByOrNull { LocalDateTime.parse(it.scoreentrydate) }
                        openedAssignment = Pair(newestSection, newestScore)
                    }
                }
            }
        }

        val sheetState = rememberModalBottomSheetState( skipPartiallyExpanded = true )
        var flagInfoOpen by remember { mutableStateOf(false) }
        val infoSheetState = rememberModalBottomSheetState( skipPartiallyExpanded = true )
        if (flagInfoOpen) {
            ModalBottomSheet(
                onDismissRequest = { flagInfoOpen = false },
                sheetState = infoSheetState,
            ) {
                FlagsExplanation(gradeColors)
            }
        }

        openedAssignment?.let { (newestSection, newestScore) ->
            ModalBottomSheet(
                onDismissRequest = { openedAssignment = null },
                sheetState = sheetState,
            ) {
                Box(Modifier.padding(5.dp)) {
                    AssignmentCard(newestSection, newestScore, ScoreDisplay.Both, gradeColors, null)
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

                val withoutAssignment = ClassMeta(currentClass.copy(assignments_parsed = currentClass.assignments_parsed.filter { !it._assignmentsections.any { it._id == newestSection._id } }))
                val withAssignment = newestScore?.scorepoints?.let {
                    ClassMeta(currentClass.copy(assignments_parsed = currentClass.assignments_parsed.map {
                        if (it._assignmentsections.any { it._id == newestSection._id })  {
                            return@map it.copy(_assignmentsections = listOf(newestSection.copy(_assignmentscores = listOf(newestScore))))
                        }
                        return@map it
                    }), allowLessThanE = true)
                } ?: withoutAssignment
                val percentChange = (withAssignment.finalScore ?: 0.0f) - (withoutAssignment.finalScore ?: 0.0f)

                Text(
                    "${if (percentChange >= 0.0) "+" else {""}}${"%.2f".sprintf(percentChange)}% ${withAssignment?.finalScore?.let { "(${it}%)" } ?: ""}",
                    modifier = Modifier.padding(20.dp, top = 10.dp),
                    fontSize = 20.sp
                )

                newestScore?.let {
                    Row(Modifier.padding(20.dp, top = 20.dp, bottom = 0.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "Flags",
                            fontWeight = FontWeight.Bold,
                            fontSize = 25.sp,
                            textAlign = TextAlign.Center
                        )
                        IconButton(onClick = {
                            flagInfoOpen = true
                        }) {
                            Icon(Icons.Outlined.Info, "Flag info")
                        }
                    }
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

                val username by viewModel.username()
                if (username == "1cjhuntwork") {
                    Text("Assignment ID: ${newestSection._id}, Update: ${lockedUpdates[newestSection._id]}")
                }
            }
        }
    }
}

@Composable
fun FlagIcon(color: Color, selected: Boolean, icon: ImageVector, contentDescription: String?, onClick: (() -> Unit)) {
    IconButton(onClick, colors = if (selected) IconButtonDefaults.iconButtonColors(containerColor = color, contentColor = MaterialTheme.colorScheme.inverseOnSurface) else IconButtonDefaults.iconButtonColors(containerColor = CardDefaults.cardColors().containerColor)) {
        Icon(icon, contentDescription)
    }
}

enum class ScoreDisplay {
    Percent,
    Points,
    Both
}

@Composable
fun AssignmentCard(assignment: Assignment, showPercent: ScoreDisplay, gradeColors: GradeColors, onClick: (() -> Unit)?) {
    val newestSection =
        assignment._assignmentsections.maxByOrNull { LocalDate.parse(it.duedate) }
    val newestScore = newestSection?._assignmentscores?.maxByOrNull { LocalDateTime.parse(it.scoreentrydate) }
    if (newestSection != null) {
        AssignmentCard(newestSection, newestScore, showPercent, gradeColors, onClick)
    }
}

@Composable
fun AssignmentCard(section: AssignmentSection, score: AssignmentScore?, showPercent: ScoreDisplay, gradeColors: GradeColors, onClick: (() -> Unit)?, showOutline: Boolean = true) {
    val themeModifier = darkModeColorModifier()
    val colors = if (section.iscountedinfinalgrade && score?.isexempt != true) {
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
    Card(modifier = iconModifier.then(onClick?.let { Modifier.clickable(onClick = onClick).shadow(3.dp, CardDefaults.shape) } ?: Modifier), colors = colors) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(10.dp)) {
            val localDensity = LocalDensity.current
            Text(score?.scorelettergrade ?: "", modifier = Modifier.width( with (localDensity) { MaterialTheme.typography.titleMedium.fontSize.toDp()*1.5f } ), style = MaterialTheme.typography.titleMedium)
            Text(section.name, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f), maxLines = 2, overflow = TextOverflow.Ellipsis)
            Text(
                when (showPercent) {
                    ScoreDisplay.Percent -> score?.scorepercent?.let {"${"%.2f".sprintf(it)}%"} ?: "-"
                    ScoreDisplay.Points -> score?.scorepoints?.let { "${it * section.weight} / ${section.totalpointvalue}" } ?: "-"
                    ScoreDisplay.Both -> score?.scorepoints?.let { "${it * section.weight} / ${section.totalpointvalue} (${"%.2f".sprintf((it * section.weight / section.totalpointvalue)*100f)}%)" } ?: "-"
                },
                style = MaterialTheme.typography.titleMedium)
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