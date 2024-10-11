package com.chrissytopher.source

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

@Composable
fun SettingsScreen() {
    val kvault = LocalKVault.current
    var usernameState by remember { mutableStateOf(kvault?.string(forKey = "USERNAME") ?: "") }
    var passwordState by remember { mutableStateOf(kvault?.string(forKey = "PASSWORD") ?: "") }
    Column {
        Text("Settings")
        TextField(
            value = usernameState,
            onValueChange = { usernameState = it },
            label = { Text("Username") }
        )
        TextField(
            value = passwordState,
            onValueChange = { passwordState = it },
            label = { Text("Password") }
        )
        Button(onClick = { changeLogin(kvault, usernameState, passwordState) }) {
            Text("Change")
        }

    }
}