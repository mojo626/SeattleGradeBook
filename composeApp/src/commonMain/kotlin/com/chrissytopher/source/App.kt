package com.chrissytopher.source

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.liftric.kvault.KVault
import org.jetbrains.compose.ui.tooling.preview.Preview

import androidx.navigation.compose.rememberNavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.navigation.compose.composable
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.dp
import androidx.navigation.NavBackStackEntry
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.createGraph
import io.github.aakira.napier.Napier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

val LocalKVault = compositionLocalOf<KVault?> { null }
val LocalJson = compositionLocalOf { Json { ignoreUnknownKeys = true } }
val LocalSourceData = compositionLocalOf<MutableState<SourceData?>> { mutableStateOf(null) }
val LocalNavHost = compositionLocalOf<NavHostController?> { null }
val ClassForGradePage = compositionLocalOf<MutableState<Class?>> { mutableStateOf(null) }

enum class NavScreen(val selectedIcon: ImageVector, val unselectedIcon: ImageVector, val showInNavBar: Boolean = true, val hideNavBar: Boolean = false) {
    Grades(Icons.Filled.Home, Icons.Outlined.Home, showInNavBar = false),
    Settings(Icons.Filled.Settings, Icons.Outlined.Settings),
    Home(Icons.Filled.Home, Icons.Outlined.Home),
    Onboarding(Icons.Filled.Settings, Icons.Outlined.Settings, showInNavBar = false, hideNavBar = true),
}

@Composable
fun AppBottomBar(currentScreenState: State<NavBackStackEntry?>, select: (NavScreen) -> Unit) {
    val currentScreen by currentScreenState
    NavigationBar {
        NavScreen.entries.filter { it.showInNavBar }.forEach { item ->
            NavigationBarItem(
                icon = {
                    Icon(
                        if (currentScreen?.destination?.route == item.name) item.selectedIcon else item.unselectedIcon,
                        contentDescription = item.name
                    )
                },
                label = { Text(item.name) },
                selected = currentScreen?.destination?.route == item.name,
                onClick = { select(item) }
            )
        }
    }
}

@Composable
@Preview
fun App(navController : NavHostController = rememberNavController()) {
    CompositionLocalProvider(LocalNavHost provides navController) {
        val kvault = LocalKVault.current
        val localJson = LocalJson.current
        var sourceData by LocalSourceData.current
        if (LocalSourceData.current.value == null) {
            kvault?.string(forKey = "GRADE_DATA")?.let { gradeData ->
                LocalSourceData.current.value = runCatching { localJson.decodeFromString<SourceData>(gradeData) }.getOrNull()
            }
        }
        LaunchedEffect(true) {
            CoroutineScope(Dispatchers.IO).launch {
                kvault?.string("USERNAME")?.let { username ->
                    kvault.string("PASSWORD")?.let { password ->
                        CoroutineScope(Dispatchers.IO).launch {
                            val newSourceData = getSourceData(username, password).getOrNull()
                            kvault.set("SOURCE_DATA", localJson.encodeToString(sourceData))
                            sourceData = newSourceData
                        }
                    }
                }
            }
        }
        MaterialTheme {
            Napier.d("testingSourceData: $sourceData")

            Scaffold(
                bottomBar = {
                    val currentNav by navController.currentBackStackEntryAsState()
                    if (currentNav?.destination?.route?.let { NavScreen.valueOf(it).hideNavBar } == false) {
                        AppBottomBar(
                            navController.currentBackStackEntryAsState()
                        ) { navController.navigate(it.name) }
                    }
                }
            ) { paddingValues ->
                val loggedIn = remember { kvault?.existsObject("USERNAME") == true }
                Box(Modifier.safeDrawingPadding()) {
                    NavHost(
                        navController = navController,
                        startDestination = if (loggedIn) NavScreen.Home.name else NavScreen.Onboarding.name,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues.calculateStartPadding(LocalLayoutDirection.current), 0.dp, paddingValues.calculateEndPadding(LocalLayoutDirection.current), paddingValues.calculateBottomPadding())
                            .verticalScroll(rememberScrollState())
                    ) {
                        composable(route = NavScreen.Home.name) {
                            HomeScreen()
                        }
                        composable(route = NavScreen.Grades.name) {
                            GradesScreen()
                        }
                        composable(route = NavScreen.Settings.name) {
                            SettingsScreen()
                        }
                        composable(route = NavScreen.Onboarding.name) {
                            OnboardingScreen()
                        }
                    }
                }
            }
        }
    }
}