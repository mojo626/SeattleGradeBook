package com.chrissytopher.source

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
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsDraggedAsState
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Slider
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.TextUnit
import kotlin.math.round

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GradeCalculatorScreen() {
    
    val sourceDataState = LocalSourceData.current

    val currClasses = sourceDataState.value?.classes

    var newAssignmentsChanged by remember { mutableStateOf(false) } //toggle to recompose new classes when something changes

    var expanded by remember { mutableStateOf(false) }
    var selectedClassName by remember { mutableStateOf("Select a Class") }

    var newAssignments by remember { mutableStateOf(emptyList<Pair<Float, Float>>()) }

    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.verticalScroll(rememberScrollState())) {
        Text("Grade Calculator", fontSize = 30.sp, modifier = Modifier.padding(20.dp))

        if (selectedClassName != "Select a Class") {
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
                        ClassMeta(currClass, newAssignments),
                        false,
                    )
                }
            }
        }

        Box (modifier = Modifier.fillMaxWidth().padding(20.dp)) {
            Row( modifier = Modifier.clickable{ expanded = true }.border(2.dp, SolidColor(MaterialTheme.colorScheme.secondary),shape = RoundedCornerShape(15.dp)) ) {
                Text(selectedClassName.toString(), modifier = Modifier.padding(15.dp), fontSize = 20.sp)
                Spacer(modifier = Modifier.weight(1f))
                Icon(if (expanded) Icons.Outlined.KeyboardArrowUp else Icons.Outlined.KeyboardArrowDown, contentDescription = "", modifier = Modifier.padding(10.dp))
            }

            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false })
            {
                currClasses?.forEach { curClass ->
                    DropdownMenuItem (
                        text = { Text(curClass.name) },
                        onClick = { selectedClassName = curClass.name; expanded = false; newAssignments = emptyList<Pair<Float, Float>>() }
                    )
                }
            }
        }

        if (selectedClassName != "Select a Class")
        {
            newAssignments.forEachIndexed { index, assignment ->
                OutlinedCard (
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                    ),
                    border = BorderStroke(2.dp, MaterialTheme.colorScheme.secondary),
                    modifier = Modifier.padding(20.dp).fillMaxWidth()
                ) {
                    Column ( modifier = Modifier.padding(20.dp) )
                    {
                        Row () {
                            Text("New Assignment", fontSize = 20.sp)
                            Spacer( modifier = Modifier.weight(1f) )
                            IconButton( onClick = { newAssignments = newAssignments.filterIndexed { i, _ -> i != index }; newAssignmentsChanged = !newAssignmentsChanged } ) {
                                Icon(Icons.Outlined.Close, contentDescription = "Close button")
                            }
                        }

                        Row()
                        {
                            Text("If you got ", fontSize = 20.sp)

                            key (newAssignmentsChanged) //This is to force the field to recompose when an assignment is removed
                            {
                                CustomTextField(
                                    onValueChange = { it ->
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
                                    modifier = Modifier.height(30.dp).width(50.dp),
                                    placeholderText = "",
                                    fontSize = 20.sp,
                                    value = assignment.first.toString(),
                                )
                            }

                            Text(" out of ", fontSize = 20.sp)
                            key (newAssignmentsChanged)
                            {
                                CustomTextField(
                                    onValueChange = { it ->
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
                                    modifier = Modifier.height(30.dp).width(50.dp),
                                    placeholderText = "",
                                    fontSize = 20.sp,
                                    value = assignment.second.toString()
                                )
                            }
                        }

                        val interaction = remember { MutableInteractionSource() }
                        val isDragging by interaction.collectIsDraggedAsState()

                        Slider( value = (if (assignment.second == 0.0f) 0.0f else assignment.first/assignment.second),  onValueChange = {
                            if (isDragging)
                            {
                                newAssignments = (newAssignments.toMutableList().apply{
                                    this[index] = Pair(round(this[index].second * it*10.0f)/10.0f, this[index].second)
                                })
                                newAssignmentsChanged = !newAssignmentsChanged
                            }},
                            interactionSource = interaction
                        )
                    }

                }
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



