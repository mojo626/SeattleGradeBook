package com.chrissytopher.source

import androidx.compose.foundation.layout.Column
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.liftric.kvault.KVault
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString

@Composable
fun SettingsScreen() {
    val kvault = LocalKVault.current
    val json = LocalJson.current
    val sourceDataState = LocalSourceData.current
    var username by remember { mutableStateOf(kvault?.string(forKey = "USERNAME") ?: "") }
    var password by remember { mutableStateOf(kvault?.string(forKey = "PASSWORD") ?: "") }
    var error by remember { mutableStateOf(false) }
    var loading by remember { mutableStateOf(false) }
    Column {
        Text("Settings")
        TextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("Username") }
        )
        TextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") }
        )
        if (error) {
            Text("Failed to log in", color = MaterialTheme.colorScheme.error)
        }
        Button(onClick = {
            CoroutineScope(Dispatchers.IO).launch {
                loading = true
                val sourceData = getSourceData(username, password).getOrNull()
                if (sourceData != null) {
                    if (sourceData.isEmpty()) {
                        error = true
                        loading = false
                        return@launch
                    }
                    changeLogin(kvault, username, password, json.encodeToString(sourceData))
                    sourceDataState.value = sourceData
                    error = false
                } else {
                    error = true
                }
                loading = false
            }
        }, enabled = !loading) {
            if (loading) {
                CircularProgressIndicator()
            } else {
                Text("Change")
            }
        }
    }
}

fun changeLogin(kvault : KVault?, username : String, password : String, sourceData: String) {
    println("username: $username")
    kvault?.set(key = "USERNAME", stringValue = username)
    kvault?.set(key = "PASSWORD", stringValue = password)
    kvault?.set(key = "GRADE_DATA", stringValue = sourceData)
}