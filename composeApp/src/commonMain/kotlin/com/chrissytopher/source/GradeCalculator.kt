package com.chrissytopher.source

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.filled.Chair
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.IncompleteCircle
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.outlined.ChevronLeft
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Error
import androidx.compose.material.icons.outlined.HideSource
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chrissytopher.source.navigation.NavigationStack
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import io.github.aakira.napier.Napier
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format
import kotlinx.datetime.toLocalDateTime
import net.sergeych.mptools.Now
import net.sergeych.sprintf.sprintf
import kotlin.math.roundToInt
import kotlin.random.Random

//@OptIn(ExperimentalHazeMaterialsApi::class)
@Composable
fun GradeCalculatorScreen(viewModel: AppViewModel, navHost: NavigationStack<NavScreen>, outerPadding: PaddingValues) {
    val sourceDataState = viewModel.sourceData()
    val selectedQuarter by viewModel.selectedQuarter()
    val currClasses = remember { sourceDataState.value?.get(selectedQuarter)?.classes }
    val gradeColors by viewModel.gradeColors()

    val currentClassState = viewModel.classForGradePage
    val currentClass by currentClassState

//    var newAssignmentsChanged by remember { mutableStateOf(false) } //toggle to recompose new classes when something changes

    var expanded by remember { mutableStateOf(false) }
    var selectedClassName by remember { mutableStateOf(currentClass?.name ?: "Select a Class") }

//    var selectedClass by remember { mutableStateOf(currentClass) }

    var newAssignments by remember { mutableStateOf(emptyList<Pair<Float, Float>>()) }
//    var changedAssignments by remember { mutableStateOf( emptyList<ChangedAssignment>()) }
    var changedAssignments: List<Assignment>? by remember { mutableStateOf(currentClass?.assignments_parsed?.sortedByDescending { it._assignmentsections.maxByOrNull { LocalDate.parse(it.duedate) }?.duedate?.let { LocalDate.parse(it) } }) }
    var addedAssignments: List<Assignment> by remember { mutableStateOf(listOf()) }

    var recompose by remember { mutableStateOf(false) }

    LaunchedEffect(addedAssignments) {
//        Napier.d("changed addedAssignments: $addedAssignments")
    } //this is the only way that I could find to force a recompose :(
    //all g sometimes you just gotta do that
    Scaffold(topBar = {
        Column(Modifier.hazeEffect(viewModel.hazeState, hazeMaterial()).padding(bottom = 8.dp, top = outerPadding.calculateTopPadding(), start = outerPadding.calculateStartPadding(LocalLayoutDirection.current), end = outerPadding.calculateEndPadding(LocalLayoutDirection.current)).padding(6.dp)) {
            Box(Modifier.fillMaxWidth().padding(bottom = 4.dp)) {
                Row(Modifier.align(Alignment.CenterStart)) {
                    Spacer(Modifier.width(8.dp))
                    val screenSize = getScreenSize()
                    Icon(Icons.Outlined.ChevronLeft, contentDescription = "left arrow", modifier = Modifier.padding(0.dp, 5.dp).clip(
                        CircleShape
                    ).clickable { navHost.popStack(screenSize.width.toFloat()) })
                }

                Text("Grade Calculator", style = MaterialTheme.typography.titleLarge, modifier = Modifier.align(Alignment.Center), textAlign = TextAlign.Center)
            }
            if (selectedClassName != "Select a Class") {
                currentClass?.let { currentClass ->
                    Row(Modifier.padding(6.dp, 2.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.aspectRatio(1f).weight(1f)) {
                            ClassCard(
                                currentClass,
                                ClassMeta(currentClass),
                                false, true,
                                gradeColors,
                            )
                        }

                        Icon(Icons.AutoMirrored.Outlined.ArrowForward, contentDescription = "Right arrow", modifier = Modifier.weight(0.3f).size(50.dp))

                        Box(modifier = Modifier.aspectRatio(1f).weight(1f)) {
                            ClassCard(
                                currentClass,
                                ClassMeta(currentClass.copy(assignments_parsed = (changedAssignments ?: listOf()) + addedAssignments), allowLessThanE = true),
                                false, true,
                                gradeColors,
                            )
                        }
                    }
                }
            }
        }
    }) { innerPadding ->
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxSize().hazeSource(viewModel.hazeState).verticalScroll(rememberScrollState()).padding(top = innerPadding.calculateTopPadding(), start = innerPadding.calculateStartPadding(LocalLayoutDirection.current), end = innerPadding.calculateEndPadding(LocalLayoutDirection.current)).padding(6.dp, 12.dp)) {
            Row {
                Box (modifier = Modifier.weight(1f).padding(4.dp).clip(RoundedCornerShape(15.dp)).background(MaterialTheme.colorScheme.surfaceContainerHigh).clickable{ expanded = true }.padding(10.dp)) {
                    Row {
                        Text(selectedClassName, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Medium))
                    }

                    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false })
                    {
                        currClasses?.forEach { curClass ->
                            DropdownMenuItem (
                                text = { Text(curClass.name, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                                onClick = {
                                    selectedClassName = curClass.name
                                    currentClassState.value = curClass
                                    expanded = false
                                    newAssignments = emptyList()
                                    changedAssignments = curClass.assignments_parsed.sortedByDescending { it._assignmentsections.maxByOrNull { LocalDate.parse(it.duedate) }?.duedate?.let { LocalDate.parse(it) } }
                                    addedAssignments = mutableListOf()
                                }
                            )
                        }
                    }
                }
                if (currentClass != null) {
                    Row(modifier = Modifier.weight(1f).padding(4.dp).clip(RoundedCornerShape(15.dp)).background(MaterialTheme.colorScheme.surfaceContainerHigh).clickable {
                        addedAssignments = addedAssignments.toMutableList().apply { add(
                            Assignment(listOf(
                                AssignmentSection(
                                    duedate = Now().toLocalDateTime(TimeZone.currentSystemDefault()).date.format(LocalDate.Formats.ISO),
                                    name = "New Assignment",
                                    _id = Random.nextInt(),
                                    _name = "New Assignment",
                                    totalpointvalue = 10.0f,
                                    iscountedinfinalgrade = true,
                                    scoretype = "REAL_SCORE",
                                    sectionsdcid = 0,
                                    scoreentrypoints = 10.0f,
                                    assignmentsectionid = 0,
                                    weight = 1.0f,
                                    isscorespublish = true,
                                    isscoringneeded = false,
                                    _assignmentscores = listOf(AssignmentScore(
                                        _name = "assignmentscore",
                                        authoredbyuc = false,
                                        whenmodified = "",
                                        studentsdcid = 0,
                                        ismissing = false,
                                        isincomplete = false,
                                        iscollected = false,
                                        islate = false,
                                        isabsent = false,
                                        isexempt = false,
                                        scorepercent = 1.0f,
                                        scorelettergrade = "A",
                                        actualscoreentered = "10.0",
                                        actualscorekind = "REAL_SCORE",
                                        scoreentrydate = Now().toLocalDateTime(TimeZone.currentSystemDefault()).format(LocalDateTime.Formats.ISO),
                                        scorepoints = 10.0f,
                                    ))
                                )
                            ))
                        ) }
                    }.padding(10.dp), horizontalArrangement = Arrangement.End) {
                        Text("+ Assignment", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Medium))
                    }
                }
            }

            addedAssignments.forEachIndexed { i, assignment ->
                val newestSection = assignment._assignmentsections.maxByOrNull { LocalDate.parse(it.duedate) }
                if (newestSection == null) return@forEachIndexed
                val newestScore = newestSection._assignmentscores.maxByOrNull { LocalDateTime.parse(it.scoreentrydate) }
                Box(Modifier.padding(0.dp, 5.dp)) {
                    GradeCalculatorCard(newestSection, newestScore, gradeColors, updateAssignment = { (newSection, newScore) ->
                        addedAssignments = addedAssignments.toMutableList().apply { set(i,
                            Assignment(listOf(newScore?.let { newSection.copy(_assignmentscores = listOf(newScore)) } ?: newSection))
                        ) }
                    }, removeAssignment = {
                        addedAssignments = addedAssignments.toMutableList().apply { removeAt(i) }
                    })
                }
            }
            if (addedAssignments.isNotEmpty() && currentClass?.assignments_parsed?.isNotEmpty() == true) {
                HorizontalDivider(Modifier.padding(10.dp, 5.dp).fillMaxWidth())
            }
            currentClass?.let {
                val assignments = changedAssignments?.toMutableList()?.map { mutableStateOf(it) }
                assignments?.forEachIndexed { i, it ->
                    var assignment by it
                    val newestSection = assignment._assignmentsections.maxByOrNull { LocalDate.parse(it.duedate) }
                    if (newestSection == null) return@forEachIndexed
                    val newestScore = newestSection._assignmentscores.maxByOrNull { LocalDateTime.parse(it.scoreentrydate) }
                    Box(Modifier.padding(0.dp, 5.dp)) {
                        key(newestSection._id) {
                            GradeCalculatorCard(newestSection, newestScore, gradeColors, updateAssignment = {
                                val now = Now().toLocalDateTime(TimeZone.currentSystemDefault())
                                if (assignment._assignmentsections.isNotEmpty() && assignment._assignmentsections.firstOrNull()?._assignmentscores?.isNotEmpty() == true) {
                                    val newAssignment = assignment.copy()
                                    newAssignment._assignmentsections = newAssignment._assignmentsections.toMutableList().apply { this[0] = it.first.copy(duedate = now.date.format(LocalDate.Formats.ISO)) }
                                    it.second?.let {
                                        newAssignment._assignmentsections[0]._assignmentscores = listOf(it)
                                    }
                                    assignment = newAssignment

                                    changedAssignments?.let {
                                        changedAssignments = it.toMutableList().apply {
                                            this[i] = newAssignment
                                        }
                                        recompose = !recompose
                                    }
                                } else {
                                    assignment = Assignment(listOf(it.second?.let { score -> it.first.copy(_assignmentscores = listOf(score.copy(scoreentrydate = now.format(LocalDateTime.Formats.ISO))), duedate = now.date.format(LocalDate.Formats.ISO)) } ?: it.first.copy(duedate = now.date.format(LocalDate.Formats.ISO))))
                                    changedAssignments?.let {
                                        changedAssignments = it.toMutableList().apply {
                                            this[i] = assignment
                                        }
                                        recompose = !recompose
                                    }
                                }
                            })
                        }
                    }
                }
            }
            Spacer(Modifier.height(outerPadding.calculateBottomPadding()))
        }
    }
}


//Just some random code copied from stack overflow and slightly modified
@Composable
private fun CustomTextField(
    modifier: Modifier = Modifier,
//    leadingIcon: (@Composable () -> Unit)? = null,
//    trailingIcon: (@Composable () -> Unit)? = null,
    placeholderText: String = "-",
    value: String = "",
    fontSize: TextUnit = MaterialTheme.typography.bodyLarge.fontSize,
    onValueChange: (String) -> (Boolean),
) {
    BasicTextField(
        modifier = modifier
            .background(
                MaterialTheme.colorScheme.surfaceContainer,
                MaterialTheme.shapes.small,
            ).width(IntrinsicSize.Min),
        value = value,
        onValueChange = { onValueChange(it) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Decimal
        ),
        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
        textStyle = LocalTextStyle.current.copy(
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = fontSize
        ),
        decorationBox = { innerTextField ->
            Row(
                modifier,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box {
                    if (value.isEmpty()) {
                        Text(text = placeholderText, style = LocalTextStyle.current.copy(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)), modifier = Modifier.widthIn(30.dp), textAlign = TextAlign.Center)
                    }
                    innerTextField()
                }
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GradeCalculatorCard(section: AssignmentSection, score: AssignmentScore?, gradeColors: GradeColors, updateAssignment: (Pair<AssignmentSection, AssignmentScore?>) -> Unit, removeAssignment: (() -> Unit)? = null) {
    val grade = score?.scorelettergrade?.first()?.toString()
    val themeModifier = darkModeColorModifier()
    val containerColor = if (section.iscountedinfinalgrade && score?.isexempt != true) {
        grade?.let { gradeColors.gradeColor(it)?.let { it*themeModifier } } ?: CardDefaults.cardColors().containerColor
    } else {
        CardDefaults.cardColors().containerColor
    }
    val actualColor by animateColorAsState(containerColor)
    val colors = CardDefaults.cardColors(containerColor = actualColor)
    val iconColor = score?.let {
        when (true) {
            score.islate -> gradeColors.EColor
            score.ismissing -> gradeColors.DColor
            score.isexempt -> Color(0xffa218e7)
            score.isincomplete -> gradeColors.BColor
            !section.iscountedinfinalgrade -> gradeColors.DColor
            else -> null
        }
    }
    val iconModifier = iconColor?.let {
        Modifier.dashedBorder(3.dp, iconColor, 12.0.dp)
    } ?: Modifier
    var expanded by remember { mutableStateOf(false) }
    val keyboard = LocalSoftwareKeyboardController.current
    LaunchedEffect(expanded) {
        if (!expanded) {
            keyboard?.hide()
        }
    }
    var pointsString by remember { mutableStateOf(score?.scorepoints?.toString() ?: "") }
    var totalPointsString by remember { mutableStateOf(section.totalpointvalue.toString()) }
    ElevatedCard(colors = colors, modifier = iconModifier, onClick = { expanded = !expanded }, elevation = CardDefaults.elevatedCardElevation(defaultElevation = 3.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(10.dp)) {
            val localDensity = LocalDensity.current
            Text(score?.scorelettergrade ?: "", modifier = Modifier.width( with (localDensity) { MaterialTheme.typography.titleMedium.fontSize.toDp()*1.5f } ), style = MaterialTheme.typography.titleMedium)
            Text(section.name, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f), maxLines = 2, overflow = TextOverflow.Ellipsis)
            CustomTextField(
                onValueChange = {
                    pointsString = it
                    val newPoints = it.toFloatOrNull() ?: return@CustomTextField true
                    Napier.d("score: $score")
                    val percent = if (section.totalpointvalue != 0f) newPoints/section.totalpointvalue else Float.NaN
                    val changedScore = (score ?: AssignmentScore("", "", "", true, false, false, false, false, false, false, "", "", percent, newPoints, 0, "")).copy(scorepoints = newPoints, scorelettergrade = gradeForScore(percent*100f))
                    updateAssignment(Pair(section, changedScore))
                    return@CustomTextField true
                },
                fontSize = MaterialTheme.typography.titleMedium.fontSize,
                value = pointsString,
                modifier = Modifier.align(Alignment.CenterVertically),
            )
            Text(" / ", style = MaterialTheme.typography.titleMedium)
            CustomTextField(
                onValueChange = {
                    totalPointsString = it
                    Napier.d("it: $it, float: ${it.toFloatOrNull()}")
                    val newTotalPoints = it.toFloatOrNull() ?: return@CustomTextField true
                    val changedSection = section.copy(totalpointvalue = newTotalPoints)
                    val newPoints = if (section.totalpointvalue != 0f) score?.scorepoints?.let { it/section.totalpointvalue * newTotalPoints } else score?.scorepoints
                    newPoints?.toString()?.let { pointsString = it }
                    val percent = if (newTotalPoints != 0f && newPoints != null) newPoints/newTotalPoints else Float.NaN
                    Napier.d("percent: $percent, newPoints: $newPoints, newtotalPoints: $newTotalPoints")
                    val changedScore = score?.copy(scorepoints = newPoints, scorepercent = percent, scorelettergrade = newPoints?.let { gradeForScore(percent*100f) } )
                    updateAssignment(Pair(changedSection, changedScore))
                    return@CustomTextField true
                },
                fontSize = MaterialTheme.typography.titleMedium.fontSize,
                value = totalPointsString,
                modifier = Modifier.align(Alignment.CenterVertically),
            )

            score?.scorepoints?.let {
                val percent = if (section.totalpointvalue != 0f) it * section.weight/section.totalpointvalue else Float.NaN
                Text("(${"%.2f".sprintf(percent*100f)}%)", style = MaterialTheme.typography.titleMedium)
            }
        }
        AnimatedVisibility(expanded) { Column {
            val sliderColor = MaterialTheme.colorScheme.onSurface
            Slider(
                value = score?.scorepoints ?: section.totalpointvalue,
                onValueChange = {
                    val rounded = it.roundToInt().toFloat()
                    pointsString = rounded.toString()
                    val changedScore = (score ?: AssignmentScore("", "", "", true, false, false, false, false, false, false, "", "", rounded/section.totalpointvalue, rounded, 0, "")).copy(scorepoints = rounded, scorelettergrade = gradeForScore((rounded/section.totalpointvalue)*100f))
                    updateAssignment(Pair(section, changedScore))
                },
                valueRange = 0.0f..(section.totalpointvalue),
                modifier = Modifier.padding(horizontal = 20.dp),
                colors = SliderDefaults.colors(thumbColor = sliderColor, activeTrackColor = sliderColor, activeTickColor = sliderColor, inactiveTrackColor = MaterialTheme.colorScheme.surfaceContainer)
            )

            var flagInfoOpen by remember { mutableStateOf(false) }
            val sheetState = rememberModalBottomSheetState( skipPartiallyExpanded = true )
            if (flagInfoOpen) {
                ModalBottomSheet(
                    onDismissRequest = { flagInfoOpen = false },
                    sheetState = sheetState,
                ) {
                    FlagsExplanation(gradeColors)
                }
            }

            Row(Modifier.padding(10.dp, 0.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "Flags",
                    fontWeight = FontWeight.Medium,
                    fontSize = MaterialTheme.typography.titleLarge.fontSize,
                    textAlign = TextAlign.Center
                )
                IconButton(onClick = {
                    flagInfoOpen = true
                }) {
                    Icon(Icons.Outlined.Info, "Flag info")
                }
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Row {
                    score?.let {
                        IconButton(onClick = {
                            val changedScore = score.copy(iscollected = !score.iscollected)
                            updateAssignment(Pair(section, changedScore))
                        }, colors = if (score.iscollected) IconButtonDefaults.iconButtonColors(containerColor = gradeColors.AColor, contentColor = MaterialTheme.colorScheme.inverseOnSurface) else IconButtonDefaults.iconButtonColors(containerColor = CardDefaults.cardColors().containerColor)) {
                            Icon(Icons.Filled.Check, "Collected")
                        }
                        IconButton(onClick = {
                            val changedScore = score.copy(islate = !score.islate)
                            updateAssignment(Pair(section, changedScore))
                        }, colors = if (score.islate) IconButtonDefaults.iconButtonColors(containerColor = gradeColors.EColor, contentColor = MaterialTheme.colorScheme.inverseOnSurface) else IconButtonDefaults.iconButtonColors(containerColor = CardDefaults.cardColors().containerColor)) {
                            Icon(Icons.Filled.Schedule, "Late")
                        }
                        IconButton(onClick = {
                            val changedScore = score.copy(ismissing = !score.ismissing)
                            updateAssignment(Pair(section, changedScore))
                        }, colors = if (score.ismissing) IconButtonDefaults.iconButtonColors(containerColor =  gradeColors.DColor, contentColor = MaterialTheme.colorScheme.inverseOnSurface) else IconButtonDefaults.iconButtonColors(containerColor = CardDefaults.cardColors().containerColor)) {
                            Icon(Icons.Outlined.Error, "Missing")
                        }
                        IconButton(onClick = {
                            val changedScore = score.copy(isexempt = !score.isexempt)
                            updateAssignment(Pair(section, changedScore))
                        }, colors = if (score.isexempt) IconButtonDefaults.iconButtonColors(containerColor = Color(0xffa218e7), contentColor = MaterialTheme.colorScheme.inverseOnSurface) else IconButtonDefaults.iconButtonColors(containerColor = CardDefaults.cardColors().containerColor)) {
                            Icon(Icons.Outlined.HideSource, "Exempt")
                        }
                        IconButton(onClick = {
                            val changedScore = score.copy(isabsent = !score.isabsent)
                            updateAssignment(Pair(section, changedScore))
                        }, colors = if (score.isabsent) IconButtonDefaults.iconButtonColors(containerColor = gradeColors.AColor, contentColor = MaterialTheme.colorScheme.inverseOnSurface) else IconButtonDefaults.iconButtonColors(containerColor = CardDefaults.cardColors().containerColor)) {
                            Icon(Icons.Filled.Chair, "Absent")
                        }
                        IconButton(onClick = {
                            val changedScore = score.copy(isincomplete = !score.isincomplete)
                            updateAssignment(Pair(section, changedScore))
                        }, colors = if (score.isincomplete) IconButtonDefaults.iconButtonColors(containerColor = gradeColors.BColor, contentColor = MaterialTheme.colorScheme.inverseOnSurface) else IconButtonDefaults.iconButtonColors(containerColor = CardDefaults.cardColors().containerColor)) {
                            Icon(Icons.Filled.IncompleteCircle, "Incomplete")
                        }
                    }
                    IconButton(onClick = {
                        val changedSection = section.copy(iscountedinfinalgrade = !section.iscountedinfinalgrade)
                        updateAssignment(Pair(changedSection, score))
                    }, colors = if (!section.iscountedinfinalgrade && score?.isexempt != true) IconButtonDefaults.iconButtonColors(containerColor = gradeColors.DColor, contentColor = MaterialTheme.colorScheme.inverseOnSurface) else IconButtonDefaults.iconButtonColors(containerColor = CardDefaults.cardColors().containerColor)) {
                        Icon(Icons.Rounded.Star, "Excluded")
                    }
                }
                Box {
                    if (removeAssignment != null) {
                        IconButton(onClick = { removeAssignment() }, colors = IconButtonDefaults.iconButtonColors(containerColor = CardDefaults.cardColors().containerColor)) {
                            Icon(Icons.Outlined.Delete, "Remove Assignment")
                        }
                    }
                }
            }
        } }
    }
}

@Composable
fun FlagsExplanation(gradeColors: GradeColors) {
    Column(Modifier.fillMaxWidth().padding(20.dp)) {
        Text("Flags", fontWeight = FontWeight.Bold, fontSize = 25.sp)
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = {},
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = gradeColors.AColor,
                    contentColor = MaterialTheme.colorScheme.inverseOnSurface
                )
            ) {
                Icon(Icons.Filled.Check, "Collected")
            }
            Text("Collected", fontSize = 20.sp)
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = {},
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = gradeColors.EColor,
                    contentColor = MaterialTheme.colorScheme.inverseOnSurface
                )
            ) {
                Icon(Icons.Filled.Schedule, "Late")
            }
            Text("Late", fontSize = 20.sp)
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = {},
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = gradeColors.DColor,
                    contentColor = MaterialTheme.colorScheme.inverseOnSurface
                )
            ) {
                Icon(Icons.Outlined.Error, "Missing")
            }
            Text("Missing", fontSize = 20.sp)
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = {},
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = Color(0xffa218e7),
                    contentColor = MaterialTheme.colorScheme.inverseOnSurface
                )
            ) {
                Icon(Icons.Outlined.HideSource, "Exempt")
            }
            Text("Exempt", fontSize = 20.sp)
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = {},
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = gradeColors.AColor,
                    contentColor = MaterialTheme.colorScheme.inverseOnSurface
                )
            ) {
                Icon(Icons.Filled.Chair, "Absent")
            }
            Text("Absent", fontSize = 20.sp)
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = {},
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = gradeColors.BColor,
                    contentColor = MaterialTheme.colorScheme.inverseOnSurface
                )
            ) {
                Icon(Icons.Filled.IncompleteCircle, "Incomplete")
            }
            Text("Incomplete", fontSize = 20.sp)
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = {},
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = gradeColors.DColor,
                    contentColor = MaterialTheme.colorScheme.inverseOnSurface
                )
            ) {
                Icon(Icons.Rounded.Star, "Incomplete")
            }
            Text("Excluded", fontSize = 20.sp)
        }
    }
}