package com.chrissytopher.source

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.liftric.kvault.KVault
import io.github.aakira.napier.Napier
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
    var username by remember { mutableStateOf(kvault?.string(forKey = USERNAME_KEY) ?: "") }
    var password by remember { mutableStateOf(kvault?.string(forKey = PASSWORD_KEY) ?: "") }
    var error by remember { mutableStateOf(false) }
    var loading by remember { mutableStateOf(false) }
    var loggedIn by remember { mutableStateOf(false) }
    LaunchedEffect(loggedIn) {
        if (loggedIn) {
            navHost?.graph?.setStartDestination(NavScreen.Home.name)
            if (livingInFearOfBackGestures()) {
                var done: Boolean
                do done = navHost?.navigateUp() == true while (!done)
            } else {
                navHost?.navigate(NavScreen.Home.name)
            }
        }
    }
    Box(Modifier.imePadding()) {
        Column(Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Source log in", style = MaterialTheme.typography.titleLarge, modifier = Modifier.fillMaxWidth(0.7f))
            Spacer(Modifier.height(20.dp))
            Text("Use your SPS username and password to log into the source", style = MaterialTheme.typography.titleSmall, modifier = Modifier.fillMaxWidth(0.7f))
            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Username") },
                singleLine = true,
                keyboardOptions = KeyboardOptions.Default.copy(capitalization = KeyboardCapitalization.None, autoCorrect = false, keyboardType = KeyboardType.Email, imeAction = ImeAction.Next)
            )
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                visualTransformation = PasswordVisualTransformation(),
                label = { Text("Password") },
                singleLine = true,
                keyboardOptions = KeyboardOptions.Default.copy(capitalization = KeyboardCapitalization.None, autoCorrect = false, keyboardType = KeyboardType.Password, imeAction = ImeAction.Done)
            )
            if (error) {
                Text("Failed to log in", color = MaterialTheme.colorScheme.error)
            }
            Button(onClick = {
                CoroutineScope(Dispatchers.IO).launch {
                    loading = true
                    error = false
                    val sourceDataRes = getSourceData(username, password)
                    Napier.d("source data: $sourceDataRes")
                    val sourceData = sourceDataRes.getOrNullAndThrow()
                    if (sourceData != null) {
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
        Column(Modifier.align(Alignment.BottomCenter)) {
            Text("This app is not associated with Seattle Public Schools or PowerSchool.", style = MaterialTheme.typography.bodySmall, modifier = Modifier.fillMaxWidth(0.9f))
            Text("Login info stays private and is only used to connect directly to The Source", style = MaterialTheme.typography.bodySmall, modifier = Modifier.fillMaxWidth(0.9f))
        }
    }
}

fun changeLogin(kvault : KVault?, username : String, password : String, sourceData: String) {
    Napier.d("username: $username")
    kvault?.set(key = USERNAME_KEY, stringValue = username)
    kvault?.set(key = PASSWORD_KEY, stringValue = password)
    kvault?.set(key = SOURCE_DATA_KEY, stringValue = sourceData)
}

@Composable
fun StyledTextInput(modifier: Modifier = Modifier, label: @Composable (Color) -> Unit, value: String, onValueChange: (String) -> Unit) {
    Surface(modifier) {
        Surface(
            Modifier
                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(10.dp))
                .padding(10.dp)
        ) {
//            val brush = remember {
//                Brush.linearGradient(
//                    colors = rainbowColors
//                )
//            }
            val background = MaterialTheme.colorScheme.surface
            var size by remember { mutableStateOf(IntSize.Zero) }
            Box(Modifier.onSizeChanged {
                size = it
            }) {
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
//                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                    modifier = Modifier,
//                        .fillMaxWidth()
//                        .background(background),
                    decorationBox = { innerTextField ->
                        var outlineModifier = Modifier
                            .fillMaxWidth()
                        if (value.isNotEmpty()) {
                            outlineModifier = outlineModifier.border(
                                brush = SolidColor(MaterialTheme.colorScheme.primary),
                                width = 1.dp,
                                shape = RoundedCornerShape(5.dp)
                            )
                        } else {
                            outlineModifier = outlineModifier.border(
                                color = MaterialTheme.colorScheme.onSurface,
                                width = 1.dp,
                                shape = RoundedCornerShape(5.dp)
                            )
                        }
                        outlineModifier = outlineModifier.padding(12.dp)
                        Surface(
                            modifier = outlineModifier
                        ) {
                            if (value.isEmpty()) {
                                label(MaterialTheme.colorScheme.onSurface)
                            }

                            innerTextField()
                        }
                    },
//                    textStyle = TextStyle.Companion.Default.copy(
//                        color = MaterialTheme.colorScheme.onSurface,
//                        fontWeight = if (value.isNotEmpty()) FontWeight.Bold else FontWeight.Normal,
//                    )
                )
            }

        }
    }
}