package com.chrissytopher.source

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
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
import androidx.compose.ui.unit.dp

@Composable
fun SettingsScreen() {
    Column {
        val kvault = LocalKVault.current
        Button(onClick = {
            kvault?.deleteObject(USERNAME_KEY)
            kvault?.deleteObject(PASSWORD_KEY)
            kvault?.deleteObject(SOURCE_DATA_KEY)
            closeApp()
        }) {
            Text("Log out")
        }
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(5.dp)) {
            var hideMentorship by remember { mutableStateOf(kvault?.bool(HIDE_MENTORSHIP_KEY) ?: false) }
            Text("Hide mentorship from home", modifier = Modifier.weight(1f), style = MaterialTheme.typography.titleLarge)
            Switch(hideMentorship, onCheckedChange = {
                hideMentorship = it
                kvault?.set(HIDE_MENTORSHIP_KEY, it)
            })
        }
    }
}