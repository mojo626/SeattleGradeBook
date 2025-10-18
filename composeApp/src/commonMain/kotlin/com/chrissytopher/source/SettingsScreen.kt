package com.chrissytopher.source

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChevronLeft
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewModelScope
import com.chrissytopher.source.navigation.NavigationStack
import dev.chrisbanes.haze.hazeSource
import io.github.aakira.napier.Napier
import io.ktor.http.decodeURLPart
import io.ktor.utils.io.asByteWriteChannel
import io.ktor.utils.io.writeSource
import kotlinx.coroutines.launch
import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.io.readByteArray
import kotlinx.io.readString
import kotlinx.serialization.json.Json

@Composable
fun SettingsScreen(viewModel: AppViewModel, navHost: NavigationStack<NavScreen>, innerPadding: PaddingValues) {
    Column(Modifier.hazeSource(viewModel.hazeState).fillMaxSize().verticalScroll(rememberScrollState()).padding(innerPadding).padding(12.dp)) {
        Box(Modifier.fillMaxWidth()) {
            Row(Modifier.align(Alignment.CenterStart)) {
                Spacer(Modifier.width(8.dp))
                val screenSize = getScreenSize()
                Icon(Icons.Outlined.ChevronLeft, contentDescription = "left arrow", modifier = Modifier.padding(0.dp, 5.dp).clip(
                    CircleShape
                ).clickable { navHost.popStack(screenSize.width.toFloat()) })
            }

            Text("Settings", style = MaterialTheme.typography.titleLarge, modifier = Modifier.align(Alignment.Center), textAlign = TextAlign.Center)
        }

        Text("Home Options", modifier = Modifier, style = MaterialTheme.typography.titleMedium.copy(color = MaterialTheme.colorScheme.primary))

        val hideMentorship by viewModel.hideMentorship()
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(0.dp, 2.dp).background(MaterialTheme.colorScheme.surfaceContainerHigh, RoundedCornerShape(15.dp, 15.dp, 5.dp, 5.dp)).padding(10.dp, 5.dp)) {
            Text("Hide mentorship from home", modifier = Modifier.weight(1f), style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Medium))
            Switch(hideMentorship, onCheckedChange = { viewModel.setHideMentorship(it) })
        }

        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(0.dp, 2.dp).background(MaterialTheme.colorScheme.surfaceContainerHigh, RoundedCornerShape(5.dp, 5.dp, 5.dp, 5.dp)).padding(10.dp, 5.dp)) {
            val scrollHomeScreen by viewModel.scrollHomeScreen()
            Text("Scroll Home Screen", modifier = Modifier.weight(1f), style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Medium))
            Switch(scrollHomeScreen, onCheckedChange = { viewModel.setScrollHomeScreen(it) })
        }

        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(0.dp, 2.dp).background(MaterialTheme.colorScheme.surfaceContainerHigh, RoundedCornerShape(5.dp, 5.dp, 5.dp, 5.dp)).padding(10.dp, 5.dp)) {
            val showMiddleName by viewModel.showMiddleName()
            Text("Show middle name", modifier = Modifier.weight(1f), style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Medium))
            Switch(showMiddleName, onCheckedChange = { viewModel.setShowMiddleName(it) })
        }

        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(0.dp, 2.dp).background(MaterialTheme.colorScheme.surfaceContainerHigh, RoundedCornerShape(5.dp, 5.dp, 15.dp, 15.dp)).padding(10.dp, 5.dp)) {
            val preferReported by viewModel.preferReported()
            Text("Prefer Website Grades", modifier = Modifier.weight(1f), style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Medium))
            Switch(preferReported, onCheckedChange = { viewModel.setPreferReported(it) })
        }

        val screenSize = getScreenSize()
        Spacer(Modifier.height(4.dp))
        Text("Customization", modifier = Modifier, style = MaterialTheme.typography.titleMedium.copy(color = MaterialTheme.colorScheme.primary))
        Row(Modifier.fillMaxWidth().padding(0.dp, 5.dp).clip(RoundedCornerShape(15.dp)).background(MaterialTheme.colorScheme.surfaceContainerHigh).clickable {
            navHost.navigateTo(NavScreen.Colors, animateWidth = screenSize.width.toFloat())
        }.padding(10.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Colors and Themes", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Medium))
            Icon(Icons.Outlined.ChevronRight, "Open")
        }

        Spacer(Modifier.height(4.dp))
        Text("Notifications", modifier = Modifier, style = MaterialTheme.typography.titleMedium.copy(color = MaterialTheme.colorScheme.primary))
        val notificationsAllowed by viewModel.notificationsAllowed()
        if (notificationsAllowed) {
            val everyAssignment by viewModel.notificationsEveryAssignment()
            val letterGradeChange by viewModel.notificationsLetterGradeChange()
            val threshold by viewModel.notificationsThreshold()
            val points by viewModel.notificationsPoints()

            NotificationSettings(
                everyAssignment, letterGradeChange, threshold, points,
                setEvery = { viewModel.setNotificationsEveryAssignment(it) },
                setLetter = { viewModel.setNotificationsLetterGradeChanged(it) },
                setThreshold = { viewModel.setNotificationsThreshold(it) },
                setPointThreshold = { viewModel.setNotificationsThresholdPoints(it) },
                modifier = Modifier.padding(0.dp, 5.dp).clip(RoundedCornerShape(15.dp)).background(MaterialTheme.colorScheme.surfaceContainerHigh).padding(10.dp)
            )
        } else {
            Button(onClick = {
                viewModel.requestNotificationPermissions()
            }, modifier = Modifier.align(Alignment.CenterHorizontally)) {
                Text("Enable Notifications")
            }
        }
        val username by viewModel.username()
        if (username == "1cjhuntwork") {
            Text("Developer", modifier = Modifier, style = MaterialTheme.typography.titleMedium.copy(color = MaterialTheme.colorScheme.primary))
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(0.dp, 2.dp).background(MaterialTheme.colorScheme.surfaceContainerHigh, RoundedCornerShape(15.dp, 15.dp, 5.dp, 5.dp)).padding(10.dp, 5.dp)) {
                val autoSync by viewModel.autoSync()
                Text("Auto Sync", modifier = Modifier.weight(1f), style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Medium))
                Switch(autoSync, onCheckedChange = { viewModel.setAutoSync(it) })
            }
            val platform = LocalPlatform.current
            val selectedQuarter by viewModel.selectedQuarter()
            val coroutineScope = rememberCoroutineScope()
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(0.dp, 2.dp).shadow(3.dp, RoundedCornerShape(5.dp, 5.dp, 5.dp, 5.dp)).background(MaterialTheme.colorScheme.surfaceContainerHigh, RoundedCornerShape(5.dp, 5.dp, 5.dp, 5.dp)).clickable {
                viewModel.viewModelScope.launch {
                    Napier.d("start sourcedata")
                    val sourceData = runCatching { viewModel.json.decodeFromString<SourceData>(platform.pickFile(platform.jsonTypeDescriptor())!!.buffered().readString()) }.getOrNullAndThrow() ?: return@launch
                    Napier.d("Done sourcedata")
                    val newSourceData = HashMap<String, SourceData>().apply {
                        for (quarter in listOf("Q1", "Q2", "S1", "Q3", "Q4", "S2")) {
                            set(quarter, sourceData)
                        }
                    }
                    SystemFileSystem.delete(Path("${platform.filesDir().decodeURLPart()}/pfp.jpeg"), mustExist = false)
                    viewModel.setSourceData(newSourceData)
                }
            }.padding(10.dp, 10.dp)) {
                Text("Load SourceData From JSON", modifier = Modifier.weight(1f), style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Medium))
            }
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(0.dp, 2.dp).shadow(3.dp, RoundedCornerShape(5.dp, 5.dp, 5.dp, 5.dp)).background(MaterialTheme.colorScheme.surfaceContainerHigh, RoundedCornerShape(5.dp, 5.dp, 5.dp, 5.dp)).clickable {
                navHost.navigateTo(NavScreen.Congraduation, screenSize.width.toFloat())
            }.padding(10.dp, 10.dp)) {
                Text("Open Congraduations", modifier = Modifier.weight(1f), style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Medium))
            }
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(0.dp, 2.dp).shadow(3.dp, RoundedCornerShape(5.dp, 5.dp, 5.dp, 5.dp)).background(MaterialTheme.colorScheme.surfaceContainerHigh, RoundedCornerShape(5.dp, 5.dp, 15.dp, 15.dp)).clickable {
                coroutineScope.launch {
                    val assignments = sourceData?.get(getCurrentQuarter())?.classes.orEmpty().map { it.assignments_parsed }.flatten().mapNotNull { it._assignmentsections.firstOrNull()?._id }
                    Napier.d("adding assignments $assignments")
                    val newUpdates = updatedAssignments + hashMapOf(*assignments.map { Pair(it, true) }.toTypedArray())
                    viewModel._setUpdatedAssignments(newUpdates)
                }
            }.padding(10.dp, 10.dp)) {
                Text("Add Updates", modifier = Modifier.weight(1f), style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Medium))
            }
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(0.dp, 2.dp).shadow(3.dp, RoundedCornerShape(5.dp, 5.dp, 15.dp, 15.dp)).background(MaterialTheme.colorScheme.surfaceContainerHigh, RoundedCornerShape(5.dp, 5.dp, 15.dp, 15.dp)).clickable {
                viewModel.viewModelScope.launch {
                    val image = runCatching { platform.pickFile(platform.imageTypeDescriptor())!!.buffered() }.getOrThrow()// ?: return@launch
                    SystemFileSystem.sink(Path("${platform.filesDir().decodeURLPart()}/pfp.jpeg")).asByteWriteChannel().writeSource(image)
                }
            }.padding(10.dp, 10.dp)) {
                Text("Load Pfp from Image", modifier = Modifier.weight(1f), style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Medium))
            }
        }
        val platform = LocalPlatform.current
        Button(onClick = {
            viewModel.logOut()
            SystemFileSystem.delete(Path("${platform.filesDir()}/pfp.jpeg"), mustExist = false)
            navHost.clearStack(NavScreen.Onboarding)
        }, modifier = Modifier.align(Alignment.CenterHorizontally).padding(20.dp)) {
            Text("Log out", fontSize = 20.sp)
        }

    }
}