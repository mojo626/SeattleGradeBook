package com.chrissytopher.source

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun GradesScreen() {
    val currentClass by ClassForGradePage.current
    if (currentClass == null) {
        val navHost = LocalNavHost.current
        navHost?.popBackStack()
        return
    }
    val meta = key(currentClass) {
        remember { ClassMeta(currentClass!!) }
    }
    Column {
        Text(currentClass!!.name)
        Row {
            Box (modifier = Modifier.aspectRatio(1f).weight(1f).padding(10.dp)) {
                ClassCard(currentClass!!, meta)
            }
            Box (modifier = Modifier.aspectRatio(1f).weight(1f).padding(10.dp)) {
                
            }
        }
    }
}