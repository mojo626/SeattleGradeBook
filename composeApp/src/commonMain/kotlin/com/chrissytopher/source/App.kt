package com.chrissytopher.source

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
import androidx.navigation.NavBackStackEntry
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.createGraph
import kotlinx.serialization.json.Json

val LocalKVault = compositionLocalOf<KVault?> { null }
val LocalJson = compositionLocalOf { Json { ignoreUnknownKeys = true } }
val LocalSourceData = compositionLocalOf<MutableState<List<Class>?>> { mutableStateOf(null) }
val LocalNavHost = compositionLocalOf<NavHostController?> { null }

enum class NavScreen(val selectedIcon: ImageVector, val unselectedIcon: ImageVector, val special: Boolean = false) {
    Home(Icons.Filled.Home, Icons.Outlined.Home),
    Grades(Icons.Filled.Home, Icons.Outlined.Home),
    Settings(Icons.Filled.Settings, Icons.Outlined.Settings),
    Onboarding(Icons.Filled.Settings, Icons.Outlined.Settings, special = true),
}

@Composable
fun AppBottomBar(currentScreenState: State<NavBackStackEntry?>, select: (NavScreen) -> Unit) {
    val currentScreen by currentScreenState
    NavigationBar {
        NavScreen.entries.filter { !it.special }.forEach { item ->
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
    val kvault = LocalKVault.current
    CompositionLocalProvider(LocalNavHost provides navController) {
        val localJson = LocalJson.current
        if (LocalSourceData.current.value == null) {
            kvault?.string(forKey = "GRADE_DATA")?.let { gradeData ->
                LocalSourceData.current.value = localJson.decodeFromString<List<Class>>(gradeData)
            }
        }
        MaterialTheme {
            val sourceData: List<Class>? by LocalSourceData.current

            println("testingSourceData: $sourceData")

            Scaffold(
                bottomBar = {
                    val currentNav by navController.currentBackStackEntryAsState()
                    if (currentNav?.destination?.route?.let { NavScreen.valueOf(it).special } == false) {
                        AppBottomBar(
                            navController.currentBackStackEntryAsState()
                        ) { navController.navigate(it.name) }
                    }
                }
            ) {paddingValues ->
                val loggedIn = remember { kvault?.existsObject("USERNAME") == true }
                NavHost(
                    navController = navController,
                    startDestination = if (loggedIn) NavScreen.Home.name else NavScreen.Onboarding.name,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
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