package com.chrissytopher.source

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.chrissytopher.source.navigation.NavigationStack
import dev.chrisbanes.haze.hazeSource
import io.github.aakira.napier.Napier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import kotlin.math.roundToInt

@Composable
fun LoginScreen(viewModel: AppViewModel, done: () -> Unit) {
    val sourceDataState by viewModel.sourceData()
    val selectedQuarter by viewModel.selectedQuarter()
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var error by remember { mutableStateOf(false) }
    var loading by remember { mutableStateOf(false) }

    Box(Modifier.fillMaxSize().imePadding()) {
        Column(Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Source log in", style = MaterialTheme.typography.titleLarge, modifier = Modifier.fillMaxWidth(0.7f), textAlign = TextAlign.Center)
            Spacer(Modifier.height(20.dp))
            Text("Use your SPS username and password to log into the source", style = MaterialTheme.typography.titleSmall, modifier = Modifier.fillMaxWidth(0.7f), textAlign = TextAlign.Center)
            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Username") },
                singleLine = true,
                keyboardOptions = KeyboardOptions.Default.copy(capitalization = KeyboardCapitalization.None, autoCorrectEnabled = false, keyboardType = KeyboardType.Email, imeAction = ImeAction.Next)
            )
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                visualTransformation = PasswordVisualTransformation(),
                label = { Text("Password") },
                singleLine = true,
                keyboardOptions = KeyboardOptions.Default.copy(capitalization = KeyboardCapitalization.None, autoCorrectEnabled = false, keyboardType = KeyboardType.Password, imeAction = ImeAction.Done)
            )
            if (error) {
                Text("Failed to log in", color = MaterialTheme.colorScheme.error)
            }
            Button(onClick = {
                CoroutineScope(Dispatchers.IO).launch {
                    loading = true
                    error = false
                    username = username.removeSuffix("@seattleschools.org")
                    val sourceDataRes = viewModel.gradeSyncManager.getSourceData(username, password, selectedQuarter, true)
                    Napier.d("got data: $sourceDataRes")
                    val sourceData = sourceDataRes.getOrNullAndThrow()
                    if (sourceData != null) {
                        val newSourceData = HashMap<String, SourceData>().apply {
                            set(selectedQuarter, sourceData)
                        }
                        viewModel.setSourceData(newSourceData)
                        viewModel.changeLogin(username, password, newSourceData)
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
fun NotificationsScreen(viewModel: AppViewModel, done: () -> Unit) {
    val platform = LocalPlatform.current
    Column(Modifier.fillMaxSize()) {
        Box(Modifier.weight(1f)) {
            Column(Modifier.align(Alignment.Center).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Want updates on your grades?", style = MaterialTheme.typography.titleLarge.copy(fontSize = MaterialTheme.typography.titleLarge.fontSize*1.2f), textAlign = TextAlign.Center)
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
            val permissionGranted by viewModel.notificationsAllowed()
            val everyAssignment by viewModel.notificationsEveryAssignment()
            val letterGradeChange by viewModel.notificationsLetterGradeChange()
            val threshold by viewModel.notificationsThreshold()
            val points by viewModel.notificationsPoints()
            AnimatedVisibility(permissionGranted) {
                NotificationSettings(
                    everyAssignment, letterGradeChange, threshold, points,
                    setEvery = { viewModel.setNotificationsEveryAssignment(it) },
                    setLetter = { viewModel.setNotificationsLetterGradeChanged(it) },
                    setThreshold = { viewModel.setNotificationsThreshold(it) },
                    setPointThreshold = { viewModel.setNotificationsThresholdPoints(it) }
                )
            }
            if (!permissionGranted) {
                Button(
                    onClick = {
                        viewModel.requestNotificationPermissions()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onSurface
                    )
                ) {
                    Text(text = "Allow Notifications")
                }
            } else {
                Button(onClick = {
                    done()
                }, colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
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
fun NotificationSettings(everyAssignment: Boolean, letterGradeChange: Boolean, threshold: Boolean, points: Float, setEvery: (Boolean) -> Unit, setLetter: (Boolean) -> Unit, setThreshold: (Boolean) -> Unit, setPointThreshold: (Float) -> Unit, modifier: Modifier = Modifier) {
    Column(modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
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
                    Slider(points, onValueChange = { setPointThreshold(it) }, valueRange = 0f..300f, steps = 29, modifier = Modifier.fillMaxWidth(0.75f).align(Alignment.Center), colors = SliderDefaults.colors(inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant))
                }
            }
        }
    }
}

@Composable
fun OnboardingScreen(viewModel: AppViewModel, navHost: NavigationStack<NavScreen>, innerPadding: PaddingValues) {
    Box(Modifier.hazeSource(viewModel.hazeState).fillMaxSize().padding(innerPadding)) {
        var loggedIn by remember { mutableStateOf(false) }
        if (!loggedIn) {
            LoginScreen(viewModel) {
                loggedIn = true
            }
        } else {
            NotificationsScreen(viewModel) {
                navHost.clearStack(NavScreen.Home)
            }
        }
    }
}