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
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.CardDefaults
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontWeight
import com.chrissytopher.source.navigation.NavigationStack

@Composable
fun MoreScreen(viewModel: AppViewModel, navHost: NavigationStack<NavScreen>) {
    val platform = LocalPlatform.current
    var currentClass by viewModel.classForGradePage
    val sourceData by viewModel.sourceData()
    val hideMentorship by viewModel.hideMentorship()
    val selectedQuarter by viewModel.selectedQuarter()
    val screenSize = getScreenSize()

    Column(Modifier.fillMaxWidth().padding(12.dp)) {
        Text("Grade Analysis", modifier = Modifier, style = MaterialTheme.typography.titleMedium.copy(color = MaterialTheme.colorScheme.primary))
        Row (modifier = Modifier.padding(0.dp, 2.dp).clip(RoundedCornerShape(15.dp, 15.dp, 5.dp, 5.dp)).background(MaterialTheme.colorScheme.surfaceContainerHigh).clickable {
            navHost.navigateTo(NavScreen.GPA, animateWidth = screenSize.width.toFloat())
        }.padding(10.dp)) {
            Text("GPA Calculator", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Medium), modifier = Modifier.weight(1f))
            Icon(Icons.Outlined.ChevronRight, contentDescription = "right arrow", modifier = Modifier.align(Alignment.CenterVertically))
        }

        Row (modifier = Modifier.padding(0.dp, 2.dp).clip(RoundedCornerShape(5.dp, 5.dp, 15.dp, 15.dp)).background(MaterialTheme.colorScheme.surfaceContainerHigh).clickable {
            currentClass = null
            navHost.navigateTo(NavScreen.Calculator, animateWidth = screenSize.width.toFloat())
        }.padding(10.dp)) {
            Text("Grade Calculator", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Medium), modifier = Modifier.weight(1f))
            Icon(Icons.Outlined.ChevronRight, contentDescription = "right arrow", modifier = Modifier.align(Alignment.CenterVertically))
        }

        Text("Sharing", modifier = Modifier, style = MaterialTheme.typography.titleMedium.copy(color = MaterialTheme.colorScheme.primary))
        Row(modifier = Modifier.padding(0.dp, 2.dp).clip(RoundedCornerShape(15.dp, 15.dp, 15.dp, 15.dp)).background(MaterialTheme.colorScheme.surfaceContainerHigh).clickable {
            val classMetas = sourceData?.get(selectedQuarter)?.classes?.filter { !hideMentorship || it.name != MENTORSHIP_NAME }?.map { ClassMeta(it) }
            val gradesText = classMetas?.map {
                when (it.grade?.first()) {
                    'A' -> "\uD83D\uDFE9"
                    'B' -> "\uD83D\uDFE6"
                    'C' -> "\uD83D\uDFE8"
                    'D' -> "\uD83D\uDFE7"
                    'E' -> "\uD83D\uDFE5"
                    else -> "⬛"
                }
            }?.chunked(2) { it.joinToString("") }?.joinToString("\n")
            platform.shareText(
                    "$gradesText\n" +
                    "Look at my cool grades from the Seattle Gradebook app!")
        }.padding(10.dp)) {
            Text("Share grades like a wordle score", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Medium), modifier = Modifier.weight(1f))
            Icon(Icons.Rounded.Share, contentDescription = "share", modifier = Modifier.align(Alignment.CenterVertically))
        }
    }
}