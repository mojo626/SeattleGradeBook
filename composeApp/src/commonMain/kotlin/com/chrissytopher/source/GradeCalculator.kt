package com.chrissytopher.source

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.foundation.border
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.runtime.key
import io.github.aakira.napier.Napier
import androidx.compose.foundation.clickable
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.CardDefaults
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.KeyboardArrowUp
import androidx.compose.material.icons.Icons
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Slider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable
import kotlin.math.round

@Serializable
data class ChangedAssignment (
    var totalPointValue : Float = 0.0f,
    var receivedPointvalue: Float = 0.0f,
    var hidden : Boolean = false,
    var assignmentId : Int,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GradeCalculatorScreen() {
    
    val sourceDataState = LocalSourceData.current

    val currClasses = remember { sourceDataState.value?.classes }

    var newAssignmentsChanged by remember { mutableStateOf(false) } //toggle to recompose new classes when something changes

    var expanded by remember { mutableStateOf(false) }
    var selectedClassName by remember { mutableStateOf("Select a Class") }
    var selectedClass by remember { mutableStateOf<Class?>(null) }

    var newAssignments by remember { mutableStateOf(emptyList<Pair<Float, Float>>()) }
    var changedAssignments by remember { mutableStateOf( emptyList<ChangedAssignment>()) }

    var recompose by remember { mutableStateOf(false) }

    LaunchedEffect(recompose) {
        Napier.d(changedAssignments.toString())
    } //this is the only way that I could find to force a recompose :(
    //all g sometimes you just gotta do that

    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.verticalScroll(rememberScrollState())) {
        Text("Grade Calculator", fontSize = 30.sp, modifier = Modifier.padding(20.dp))

        if (selectedClassName != "Select a Class") {

            //this is to generate the list of ChangedAssignments so that the app won't crash
            selectedClass!!.assignments_parsed.forEachIndexed { index, assignment ->
                val newestSection =
                    assignment._assignmentsections.maxByOrNull { LocalDate.parse(it.duedate) }!!
                val newestScore =
                    newestSection._assignmentscores.maxByOrNull { LocalDateTime.parse(it.scoreentrydate) }
                if (changedAssignments.size <= index) {
                    changedAssignments += ChangedAssignment(
                        totalPointValue = newestSection.totalpointvalue,
                        receivedPointvalue = newestScore?.scorepoints?.let { it * newestSection.weight }
                            ?: -1.0f,
                        hidden = false,
                        assignmentId = newestSection._id
                    )
                    Napier.d("${(newestScore?.scorepoints?.let { it * newestSection.weight }
                        ?: -1.0f).toString()} out of ${newestSection.totalpointvalue}")
                }
            }

            Row( verticalAlignment = Alignment.CenterVertically ) {
                Box(modifier = Modifier.aspectRatio(1f).weight(1f).padding(15.dp)) {
                    var currClass = sourceDataState.value?.classes?.first { it.name == selectedClassName }!!
                    ClassCard(
                        currClass,
                        ClassMeta(currClass),
                        false,
                    )
                }

                Icon(Icons.AutoMirrored.Outlined.ArrowForward, contentDescription = "Right arrow", modifier = Modifier.fillMaxHeight().weight(0.3f).size(50.dp))

                Box(modifier = Modifier.aspectRatio(1f).weight(1f).padding(15.dp)) {
                    var currClass = sourceDataState.value?.classes?.first { it.name == selectedClassName }!!
                    ClassCard(
                        currClass,
                        ClassMeta(currClass, newAssignments, changedAssignments),
                        false,
                    )
                }


            }
        }

        Box (modifier = Modifier.fillMaxWidth().padding(20.dp)) {
            Row( modifier = Modifier.clickable{ expanded = true }.border(2.dp, SolidColor(MaterialTheme.colorScheme.secondary),shape = RoundedCornerShape(15.dp)) ) {
                Text(selectedClassName, modifier = Modifier.padding(15.dp), fontSize = 20.sp)
                Spacer(modifier = Modifier.weight(1f))
                Icon(if (expanded) Icons.Outlined.KeyboardArrowUp else Icons.Outlined.KeyboardArrowDown, contentDescription = "", modifier = Modifier.padding(10.dp))
            }

            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false })
            {
                currClasses?.forEach { curClass ->
                    DropdownMenuItem (
                        text = { Text(curClass.name) },
                        onClick = { selectedClassName = curClass.name; selectedClass = curClass; expanded = false; newAssignments = emptyList<Pair<Float, Float>>() }
                    )
                }
            }
        }

        if (selectedClassName != "Select a Class")
        {
            newAssignments.forEachIndexed { index, assignment ->
                    GradeCalculatorCard(
                        onRemove = {
                            newAssignments = newAssignments.filterIndexed { i, _ -> i != index }
                            newAssignmentsChanged = !newAssignmentsChanged
                        },
                        onReceivedValueChange = { it ->
                            if (it.length in 1..4) {
                                newAssignments = (newAssignments.toMutableList().apply{
                                    this[index] = Pair(it.toFloat(), this[index].second)
                                })
                                true
                            } else if (it.isEmpty()) {
                                true
                            } else {
                                false
                            }
                        },
                        onTotalValueChanged = { it ->
                            if (it.length in 1..4) {
                                newAssignments = (newAssignments.toMutableList().apply{
                                    this[index] = Pair(this[index].first, it.toFloat())
                                })
                                true
                            } else if (it.isEmpty()) {
                                true
                            } else {
                                false
                            }
                        },
                        totalPointvalue = assignment.second,
                        receivedPointvalue = assignment.first,
                        onSliderChanged = { it, isDragging ->
                            if (isDragging)
                            {
                                newAssignments = (newAssignments.toMutableList().apply{
                                    this[index] = Pair(round(this[index].second * it*10.0f)/10.0f, this[index].second)
                                })
                                newAssignmentsChanged = !newAssignmentsChanged
                            }
                        },
                        newAssignmentsChanged = newAssignmentsChanged
                    )
                }



            

            OutlinedCard (
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
                border = BorderStroke(2.dp, MaterialTheme.colorScheme.secondary),
                onClick = { newAssignments = newAssignments + Pair(0f, 0f); Napier.d("added new assignment")},
                modifier = Modifier.padding(20.dp).fillMaxWidth()
            ) {
                Text("+ Add Assignment", modifier = Modifier.padding(15.dp), fontSize = 20.sp)
            }


            var currentAssignmentsOpened by remember { mutableStateOf(false) }

            OutlinedCard(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
                border = BorderStroke(2.dp, MaterialTheme.colorScheme.secondary),
                onClick = { currentAssignmentsOpened = !currentAssignmentsOpened },
                modifier = Modifier.padding(20.dp).fillMaxWidth()
            ) {
                Row( verticalAlignment = Alignment.CenterVertically ) {
                    Text("Current Assignments", modifier = Modifier.padding(15.dp), fontSize = 20.sp)

                    Spacer( modifier = Modifier.weight(1f) )

                    Icon(
                        if (currentAssignmentsOpened) Icons.Outlined.KeyboardArrowUp else Icons.Outlined.KeyboardArrowDown,
                        contentDescription = "current assignments collapse arrow",
                        modifier = Modifier.padding(10.dp)
                    )
                }
            }

            AnimatedVisibility( currentAssignmentsOpened ) {
                Column () {
                    selectedClass!!.assignments_parsed.forEachIndexed { index, assignment ->


                        val newestSection = assignment._assignmentsections.maxByOrNull { LocalDate.parse(it.duedate) }!!
                        val newestScore = newestSection._assignmentscores.maxByOrNull { LocalDateTime.parse(it.scoreentrydate) }


                        if (changedAssignments[index].receivedPointvalue == -1.0f) {
                            return@forEachIndexed
                        }

                        var newAssignmentsChanged by remember { mutableStateOf(false) }

                        GradeCalculatorCard(
                            assignmentName = newestSection.name,
                            isNewAssignment = false,
                            totalPointvalue = changedAssignments[index].totalPointValue,
                            receivedPointvalue = changedAssignments[index].receivedPointvalue,
                            onRemove = {

                                changedAssignments[index].hidden = !changedAssignments[index].hidden
                                recompose = !recompose

                            },
                            onReceivedValueChange = {
                                changedAssignments[index].receivedPointvalue = if (it.isEmpty()) 0.0f else it.toFloat()
                                recompose = !recompose
                                true
                            },
                            onTotalValueChanged = {
                                changedAssignments[index].totalPointValue = it.toFloat()
                                recompose = !recompose
                                true
                            },
                            onSliderChanged = {it, isDragging ->
                                if (isDragging) {
                                    changedAssignments[index].receivedPointvalue = round(changedAssignments[index].totalPointValue * it*10.0f)/10.0f
                                    recompose = !recompose
                                    newAssignmentsChanged = !newAssignmentsChanged
                                }
                            },
                            newAssignmentsChanged = newAssignmentsChanged,
                            shown = !changedAssignments[index].hidden
                        )




                    }
                }

            }
        }

    }

}


//Just some random code copied from stack overflow and slightly modified
@Composable
private fun CustomTextField(
    modifier: Modifier = Modifier,
    leadingIcon: (@Composable () -> Unit)? = null,
    trailingIcon: (@Composable () -> Unit)? = null,
    placeholderText: String = "Placeholder",
    value: String = "",
    fontSize: TextUnit = MaterialTheme.typography.bodyLarge.fontSize,
    onValueChange: (String) -> (Boolean),
) {
    var text by rememberSaveable { mutableStateOf(value) }
    BasicTextField(
        modifier = modifier
            .background(
                MaterialTheme.colorScheme.surfaceContainer,
                MaterialTheme.shapes.small,
            )
            .fillMaxWidth(),
        value = text,
        onValueChange = {if (onValueChange(it)) text = it},
        singleLine = true,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Number
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
                if (leadingIcon != null) leadingIcon()

                Box(Modifier.weight(1f)) {
                    if (text.isEmpty()) {
                        Text(
                            text = placeholderText,
                            style = LocalTextStyle.current.copy(
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                                fontSize = fontSize
                            )
                        )
                    }
                    innerTextField()
                }
                if (trailingIcon != null) trailingIcon()
            }
        }
    )
}


@Composable
fun GradeCalculatorCard (
    assignmentName : String = "New Assignment",
    isNewAssignment : Boolean = true,
    totalPointvalue : Float = 0.0f,
    receivedPointvalue : Float = 0.0f,
    onRemove : () -> Unit,
    onReceivedValueChange : (String) -> Boolean,
    onTotalValueChanged : (String) -> Boolean,
    onSliderChanged : (Float, Boolean) -> Unit,
    newAssignmentsChanged : Boolean,
    shown : Boolean = true,
) {
    var opened by remember { mutableStateOf(isNewAssignment) }
    OutlinedCard (
        colors = CardDefaults.cardColors(
            containerColor = if (shown) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceDim,
            contentColor = if (shown) MaterialTheme.colorScheme.onSurface else Color(100, 100,100)
        ),
        border = BorderStroke(2.dp, MaterialTheme.colorScheme.secondary),
        modifier = Modifier.padding(20.dp).fillMaxWidth(),
        onClick = {
            opened = !opened
        }
    ) {
        Column ( modifier = Modifier.padding(20.dp) )
        {
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text(assignmentName, fontSize = 20.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                //ben the iconbutton seriously messes up the padding
//                IconButton(onClick = { onRemove() }) {
                    Icon(
                        if (isNewAssignment) Icons.Outlined.Close else (if(shown) Icons.Outlined.Visibility else Icons.Outlined.VisibilityOff),
                        contentDescription = "Close button",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(25.dp).clickable { onRemove() }
                    )
//                }
            }
            AnimatedVisibility(opened) {
                Column {
                    Row {
                        Text("If you got ", fontSize = 20.sp)

                        key (newAssignmentsChanged) {
                            CustomTextField(
                                onValueChange = { it ->
                                    onReceivedValueChange(it)
                                },
                                modifier = Modifier.height(30.dp).width(50.dp),
                                placeholderText = "",
                                fontSize = 20.sp,
                                value = receivedPointvalue.toString(),
                            )
                        }



                        Text(" out of ", fontSize = 20.sp)

                        key (newAssignmentsChanged) {
                            CustomTextField(
                                onValueChange = { it ->
                                    onTotalValueChanged(it)
                                },
                                modifier = Modifier.height(30.dp).width(50.dp),
                                placeholderText = "",
                                fontSize = 20.sp,
                                value = totalPointvalue.toString()
                            )
                        }


                    }

                    val interaction = remember { MutableInteractionSource() }
                    val isDragging by interaction.collectIsDraggedAsState()

                    Slider( value = (if (totalPointvalue == 0.0f) 0.0f else receivedPointvalue/totalPointvalue),
                        onValueChange = { onSliderChanged(it, isDragging) },
                        interactionSource = interaction
                    )
                }
            }
        }

    }
}



