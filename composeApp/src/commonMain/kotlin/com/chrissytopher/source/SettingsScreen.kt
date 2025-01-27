package com.chrissytopher.source

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.outlined.ChevronRight
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
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.icerock.moko.permissions.Permission
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen() {
    Column(Modifier.verticalScroll(rememberScrollState())) {
        val kvault = LocalKVault.current
        val navHost = LocalNavHost.current

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

        val screenSize = getScreenSize()
        Row(Modifier.padding(20.dp).fillMaxWidth().clickable {
            navHost?.navigateTo(NavScreen.Colors, animateWidth = screenSize.width.toFloat())
        }, horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Colors and Themes", fontSize = 20.sp)
            Icon(Icons.Outlined.ChevronRight, "Open")
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

        Button(onClick = {
            navHost?.let {
                kvault?.deleteObject(USERNAME_KEY)
                kvault?.deleteObject(PASSWORD_KEY)
                kvault?.deleteObject(SOURCE_DATA_KEY)
                navHost.clearStack(NavScreen.Onboarding)
            }
        }, modifier = Modifier.align(Alignment.CenterHorizontally).padding(20.dp)) {
            Text("Log out", fontSize = 20.sp)
        }

    }
}