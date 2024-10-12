package com.chrissytopher.source

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import com.liftric.kvault.KVault
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString

@Composable
fun OnboardingScreen() {
    val kvault = LocalKVault.current
    val json = LocalJson.current
    val sourceDataState = LocalSourceData.current
    val navHost = LocalNavHost.current
    var username by remember { mutableStateOf(kvault?.string(forKey = "USERNAME") ?: "") }
    var password by remember { mutableStateOf(kvault?.string(forKey = "PASSWORD") ?: "") }
    var error by remember { mutableStateOf(false) }
    var loading by remember { mutableStateOf(false) }
    var loggedIn by remember { mutableStateOf(false) }
    LaunchedEffect(loggedIn) {
        if (loggedIn) {
            navHost?.graph?.setStartDestination(NavScreen.Home.name)
            var done: Boolean
            do done = navHost?.navigateUp() == true while (!done)

        }
    }
    Box {
        Column(Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Source log in")
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
                    error = false
                    val sourceData = getSourceData(username, password).getOrNull()
                    if (sourceData != null) {
                        if (sourceData.isEmpty()) {
                            error = true
                            loading = false
                            return@launch
                        }
                        changeLogin(kvault, username, password, json.encodeToString(sourceData))
                        sourceDataState.value = sourceData
                        loggedIn = true
                    } else {
                        error = true
                    }
                    loading = false
                }
            }, enabled = !loading) {
                if (loading) {
                    CircularProgressIndicator()
                } else {
                    Text("Log in")
                }
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