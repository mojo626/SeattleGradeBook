package com.chrissytopher.source

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Chip
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.Slider
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.liftric.kvault.KVault
import dev.icerock.moko.permissions.Permission
import dev.icerock.moko.permissions.PermissionsController
import dev.icerock.moko.permissions.compose.PermissionsControllerFactory
import dev.icerock.moko.permissions.compose.rememberPermissionsControllerFactory
import io.github.aakira.napier.Napier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import kotlin.math.roundToInt

@Composable
fun LoginScreen(done: () -> Unit) {
    val kvault = LocalKVault.current
    val json = LocalJson.current
    val sourceDataState = LocalSourceData.current
    val platform = LocalPlatform.current
    var username by remember { mutableStateOf(kvault?.string(forKey = USERNAME_KEY) ?: "") }
    var password by remember { mutableStateOf(kvault?.string(forKey = PASSWORD_KEY) ?: "") }
    var error by remember { mutableStateOf(false) }
    var loading by remember { mutableStateOf(false) }

    Box(Modifier.imePadding()) {
        Column(Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Source log in", style = MaterialTheme.typography.titleLarge, modifier = Modifier.fillMaxWidth(0.7f), textAlign = TextAlign.Center)
            Spacer(Modifier.height(20.dp))
            Text("Use your SPS username and password to log into the source", style = MaterialTheme.typography.titleSmall, modifier = Modifier.fillMaxWidth(0.7f), textAlign = TextAlign.Center)
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
                    val sourceDataRes = platform.getSourceData(username, password)
                    val sourceData = sourceDataRes.getOrNullAndThrow()
                    if (sourceData != null) {
                        changeLogin(kvault, username, password, json.encodeToString(sourceData))
                        sourceDataState.value = sourceData
                        done()
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
            Text("This app is not associated with Seattle Public Schools or PowerSchool.", style = MaterialTheme.typography.bodySmall, modifier = Modifier.fillMaxWidth(0.9f), textAlign = TextAlign.Center)
            Text("Login info stays private and is only used to connect directly to The Source", style = MaterialTheme.typography.bodySmall, modifier = Modifier.fillMaxWidth(0.9f), textAlign = TextAlign.Center)
        }
    }
}

@Composable
fun NotificationsScreen(done: () -> Unit) {
    val permissionsController = LocalPermissionsController.current
    val coroutineScope = rememberCoroutineScope()
    val platform = LocalPlatform.current
    Column(Modifier.fillMaxSize()) {
        Box(Modifier.weight(1f)) {
            Column(Modifier.align(Alignment.Center).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Want updates on your grades?", style = MaterialTheme.typography.titleLarge.copy(fontSize = MaterialTheme.typography.titleLarge.fontSize*1.2f))
                Spacer(Modifier.height(25.dp))
                Row(Modifier.background(MaterialTheme.colorScheme.surfaceContainer, RoundedCornerShape(20.dp)).padding(10.dp)) {
                    Image(painterResource(platform.appIcon()), "App Icon", Modifier.clip(platform.iconRounding()).size(75.dp))
                    Spacer(Modifier.width(25.dp))
                    Column(verticalArrangement = Arrangement.SpaceAround, modifier = Modifier.height(75.dp)) {
                        Text("New grade in Algebra", style = MaterialTheme.typography.titleLarge)
                        Text("Unit 1 Exam - Tap to view", style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }
        }
        Column(Modifier.weight(1f).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            var permissionGranted by remember { mutableStateOf(false) }
            LaunchedEffect(true) {
                launch {
                    permissionGranted = permissionsController.isPermissionGranted(Permission.REMOTE_NOTIFICATION)
                }
            }
            var everyAssignment by remember { mutableStateOf(false) }
            var letterGradeChange by remember { mutableStateOf(false) }
            var threshold by remember { mutableStateOf(false) }
            var points by remember { mutableStateOf(100f) }
            AnimatedVisibility(permissionGranted) {
                NotificationSettings(
                    everyAssignment, letterGradeChange, threshold, points,
                    setEvery = { everyAssignment = it },
                    setLetter = { letterGradeChange = it },
                    setThreshold = { threshold = it },
                    setPointThreshold = { points = it }
                )
            }
            if (!permissionGranted) {
                Button(
                    onClick = {
                        coroutineScope.launch {
                            try {
                                permissionsController.providePermission(Permission.REMOTE_NOTIFICATION)
                                permissionGranted = true
                            } catch (e: Exception) {
                                e.printStackTrace()
                                done()
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.inversePrimary,
                        contentColor = MaterialTheme.colorScheme.onSurface
                    )
                ) {
                    Text(text = "Allow Notifications")
                }
            } else {
                val kvault = LocalKVault.current
                Button(onClick = {
                    kvault?.set(NEW_ASSIGNMENTS_NOTIFICATIONS_KEY, everyAssignment)
                    kvault?.set(LETTER_GRADE_CHANGES_NOTIFICATIONS_KEY, letterGradeChange)
                    kvault?.set(THRESHOLD_NOTIFICATIONS_KEY, threshold)
                    kvault?.set(THRESHOLD_VALUE_NOTIFICATIONS_KEY, points)
                    done()
                }, colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.inversePrimary,
                    contentColor = MaterialTheme.colorScheme.onSurface
                )) {
                    Text("Done")
                }
            }
            AnimatedVisibility(!permissionGranted, exit = fadeOut()) {
                TextButton(onClick = done) {
                    Text(text = "Not Now")
                }
            }
        }

    }
}

@Composable
fun NotificationSettings(everyAssignment: Boolean, letterGradeChange: Boolean, threshold: Boolean, points: Float, setEvery: (Boolean) -> Unit, setLetter: (Boolean) -> Unit, setThreshold: (Boolean) -> Unit, setPointThreshold: (Float) -> Unit) {
    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("When are notifications necessary?")
        FilterChip(onClick = {
            setEvery(!everyAssignment)
        }, selected = everyAssignment || (points.roundToInt() == 0 && threshold), label = {
            Text("New Assignments")
        })
        FilterChip(onClick = {
            setLetter(!letterGradeChange)
        }, selected = letterGradeChange, label = {
            Text("Letter Changed")
        })
        FilterChip(onClick = {
            setThreshold(!threshold)
        }, selected = threshold, label = {
            Text("Point Threshold")
        })
        AnimatedVisibility(threshold, modifier = Modifier.fillMaxWidth()) {
            Column {
                Text("Send notifications for assignments over ${points.roundToInt()} points", modifier = Modifier.align(Alignment.CenterHorizontally), textAlign = TextAlign.Center)
                Box(Modifier.align(Alignment.CenterHorizontally)) {
                    Slider(points, onValueChange = { setPointThreshold(it) }, valueRange = 0f..300f, steps = 29, modifier = Modifier.fillMaxWidth(0.75f).align(Alignment.Center))
                }
            }
        }
    }
}

@Composable
fun OnboardingScreen() {
    val navHost = LocalNavHost.current
    val platform = LocalPlatform.current
    var done by remember { mutableStateOf(false) }
    LaunchedEffect(done) {
//        if (done) {
//            navHost?.graph?.setStartDestination(NavScreen.Home.name)
//            if (platform.livingInFearOfBackGestures()) {
//                var doneBacking: Boolean
//                do doneBacking = navHost?.navigateUp() == true while (!doneBacking)
//            } else {
//                navHost?.navigate(NavScreen.Home.name)
//            }
//        }
        navHost?.clearStack(NavScreen.Home)
    }
    var loggedIn by remember { mutableStateOf(false) }
    if (!loggedIn) {
        LoginScreen {
            loggedIn = true
        }
    } else {
        NotificationsScreen {
            done = true
        }
    }
}

fun changeLogin(kvault : KVault?, username : String, password : String, sourceData: String) {
    kvault?.set(key = USERNAME_KEY, stringValue = username)
    kvault?.set(key = PASSWORD_KEY, stringValue = password)
    kvault?.set(key = SOURCE_DATA_KEY, stringValue = sourceData)
}