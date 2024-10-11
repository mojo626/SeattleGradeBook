package com.chrissytopher.source

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.liftric.kvault.KVault
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.ui.tooling.preview.Preview

import androidx.navigation.compose.rememberNavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import source2.composeapp.generated.resources.Res
import source2.composeapp.generated.resources.compose_multiplatform

import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import source2.composeapp.generated.resources.*
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.navigation.compose.composable
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.Subscriptions
import androidx.compose.material.icons.outlined.Camera
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Subscriptions
import androidx.compose.material3.IconButton
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TextField
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavBackStackEntry
import androidx.navigation.compose.currentBackStackEntryAsState
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

val LocalKVault = compositionLocalOf<KVault?> { null }
val LocalJson = compositionLocalOf { Json { ignoreUnknownKeys = true } }
val LocalSourceData = compositionLocalOf<MutableState<List<Class>?>> { mutableStateOf(null) }

enum class NavScreen(val selectedIcon: ImageVector, val unselectedIcon: ImageVector) {
    Home(Icons.Filled.Home, Icons.Outlined.Home),
    Grades(Icons.Filled.Home, Icons.Outlined.Home),
    Settings(Icons.Filled.Settings, Icons.Outlined.Settings)
}

@Composable
fun AppBottomBar(currentScreenState: State<NavBackStackEntry?>, select: (NavScreen) -> Unit) {
    val currentScreen by currentScreenState
    NavigationBar {
        NavScreen.entries.forEach { item ->
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
fun App( navController : NavHostController = rememberNavController()) {
    val localJson = LocalJson.current
    val kvault = LocalKVault.current
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
                AppBottomBar(
                    navController.currentBackStackEntryAsState()
                ) { navController.navigate(it.name) }
            }
        ) {
            NavHost(
                navController = navController,
                startDestination = NavScreen.Home.name,
                modifier = Modifier
                    .fillMaxSize()
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
            }
        }
    }
}