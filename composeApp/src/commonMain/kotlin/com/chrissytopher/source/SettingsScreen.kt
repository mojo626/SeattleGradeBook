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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

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

        Button(onClick = {
            kvault?.deleteObject(USERNAME_KEY)
            kvault?.deleteObject(PASSWORD_KEY)
            kvault?.deleteObject(SOURCE_DATA_KEY)
            closeApp()
        }, modifier = Modifier.align(Alignment.CenterHorizontally).padding(20.dp)) {
            Text("Log out", fontSize = 20.sp)
        }

    }
}