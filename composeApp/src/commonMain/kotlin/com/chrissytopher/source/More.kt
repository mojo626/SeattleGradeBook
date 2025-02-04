package com.chrissytopher.source

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.unit.dp
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.foundation.border 
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.CardDefaults
import androidx.compose.ui.text.font.FontWeight

@Composable
fun MoreScreen() {
    val kvault = LocalKVault.current
    val json = LocalJson.current
    val sourceDataState = LocalSourceData.current
    val navHost = LocalNavHost.current
    var currentClass by ClassForGradePage.current
    val screenSize = getScreenSize()

    Column(Modifier.fillMaxWidth().padding(12.dp)) {
        Text("Grade Analysis", modifier = Modifier, style = MaterialTheme.typography.titleMedium.copy(color = MaterialTheme.colorScheme.primary))
        Row (modifier = Modifier.padding(0.dp, 2.dp).clip(RoundedCornerShape(15.dp, 15.dp, 5.dp, 5.dp)).background(MaterialTheme.colorScheme.surfaceContainerHigh).clickable {
            navHost?.navigateTo(NavScreen.GPA, animateWidth = screenSize.width.toFloat())
        }.padding(10.dp)) {
            Text("GPA Calculator", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Medium), modifier = Modifier.weight(1f))
            Icon(Icons.Outlined.ChevronRight, contentDescription = "right arrow", modifier = Modifier)
        }

        Row (modifier = Modifier.padding(0.dp, 2.dp).clip(RoundedCornerShape(5.dp, 5.dp, 15.dp, 15.dp)).background(MaterialTheme.colorScheme.surfaceContainerHigh).clickable {
            currentClass = null
            navHost?.navigateTo(NavScreen.Calculator, animateWidth = screenSize.width.toFloat())
        }.padding(10.dp)) {
            Text("Grade Calculator", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Medium), modifier = Modifier.weight(1f))
            Icon(Icons.Outlined.ChevronRight, contentDescription = "right arrow", modifier = Modifier)
        }
    }
}