package com.chrissytopher.source

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.KeyboardArrowUp
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.icerock.moko.permissions.Permission
import io.github.aakira.napier.Napier
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    currentTheme: ThemeVariant,
    onThemeChange: (ThemeVariant) -> Unit
) {
    Column(Modifier.verticalScroll(rememberScrollState())) {
        val kvault = LocalKVault.current

        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(20.dp)) {
            var hideMentorship by remember { mutableStateOf(kvault?.bool(HIDE_MENTORSHIP_KEY) ?: false) }
            Text("Hide mentorship from home", modifier = Modifier.weight(1f), style = MaterialTheme.typography.titleLarge)
            Switch(hideMentorship, onCheckedChange = {
                hideMentorship = it
                kvault?.set(HIDE_MENTORSHIP_KEY, it)
            })
        }

        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(20.dp)) {
            var showMiddleName by ShowMiddleName.current
            if (showMiddleName == null) {
                showMiddleName = kvault?.bool(SHOW_MIDDLE_NAME_KEY)
            }
            Text("Show middle name", modifier = Modifier.weight(1f), style = MaterialTheme.typography.titleLarge)
            Switch(showMiddleName == true, onCheckedChange = {
                kvault?.set(SHOW_MIDDLE_NAME_KEY, it)
                showMiddleName = it
            })
        }

        var expanded by remember { mutableStateOf(false) }
        var selectedTheme by remember { mutableStateOf(currentTheme.toString()) }

        Row( verticalAlignment = Alignment.CenterVertically ) {
            Text("Choose Theme: ", modifier = Modifier.padding(20.dp), fontSize = 20.sp)

            Box (modifier = Modifier.fillMaxWidth().padding(20.dp)) {
                Row( modifier = Modifier.clickable{ expanded = true }.border(2.dp, SolidColor(MaterialTheme.colorScheme.secondary),shape = RoundedCornerShape(15.dp)) ) {
                    Text(selectedTheme, modifier = Modifier.padding(15.dp), fontSize = 20.sp)
                    Spacer(modifier = Modifier.weight(1f))
                    Icon(if (expanded) Icons.Outlined.KeyboardArrowUp else Icons.Outlined.KeyboardArrowDown, contentDescription = "", modifier = Modifier.padding(10.dp))
                }

                DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false })
                {
                    ThemeVariant.entries.forEach { theme ->
                        DropdownMenuItem (
                            text = { Text(theme.toString()) },
                            onClick = { selectedTheme = theme.toString(); onThemeChange(theme); expanded = false; kvault?.set("THEME", theme.toString())}
                        )
                    }
                }
            }
        }

        val permissionsController = LocalPermissionsController.current
        var notificationsAllowed by remember { mutableStateOf(false) }
        val coroutineScope = rememberCoroutineScope()
        LaunchedEffect(true) {
            launch {
                notificationsAllowed = permissionsController.isPermissionGranted(Permission.REMOTE_NOTIFICATION)
            }
        }
        if (notificationsAllowed) {
            var everyAssignment by remember { mutableStateOf(kvault?.bool(NEW_ASSIGNMENTS_NOTIFICATIONS_KEY) ?: false) }
            var letterGradeChange by remember { mutableStateOf(kvault?.bool(LETTER_GRADE_CHANGES_NOTIFICATIONS_KEY) ?: false) }
            var threshold by remember { mutableStateOf(kvault?.bool(THRESHOLD_NOTIFICATIONS_KEY) ?: false) }
            var points by remember { mutableStateOf(kvault?.float(THRESHOLD_VALUE_NOTIFICATIONS_KEY) ?: 100f) }
            NotificationSettings(
                everyAssignment, letterGradeChange, threshold, points,
                setEvery = { everyAssignment = it; kvault?.set(NEW_ASSIGNMENTS_NOTIFICATIONS_KEY, it) },
                setLetter = { letterGradeChange = it; kvault?.set(LETTER_GRADE_CHANGES_NOTIFICATIONS_KEY, it) },
                setThreshold = { threshold = it; kvault?.set(THRESHOLD_NOTIFICATIONS_KEY, it) },
                setPointThreshold = { points = it; kvault?.set(THRESHOLD_VALUE_NOTIFICATIONS_KEY, it) },
            )
        } else {
            Button(onClick = {
                coroutineScope.launch {
                    try {
                        permissionsController.providePermission(Permission.REMOTE_NOTIFICATION)
                        notificationsAllowed = true
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }, modifier = Modifier.align(Alignment.CenterHorizontally)) {
                Text("Enable Notifications")
            }
        }

        val platform = LocalPlatform.current
        Button(onClick = {
            kvault?.deleteObject(USERNAME_KEY)
            kvault?.deleteObject(PASSWORD_KEY)
            kvault?.deleteObject(SOURCE_DATA_KEY)
            platform.closeApp()
        }, modifier = Modifier.align(Alignment.CenterHorizontally).padding(20.dp)) {
            Text("Log out", fontSize = 20.sp)
        }

    }
}