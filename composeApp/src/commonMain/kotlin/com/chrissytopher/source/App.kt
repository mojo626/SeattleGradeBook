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
import androidx.compose.material3.IconButton
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TextField
import androidx.navigation.compose.currentBackStackEntryAsState
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

val LocalKVault = compositionLocalOf<KVault?> { null }
val LocalJson = compositionLocalOf { Json { ignoreUnknownKeys = true } }
 val LocalSourceData = compositionLocalOf<MutableState<List<Class>?>> { mutableStateOf(null) }

enum class AppScreen(val title : StringResource) {
    Home(title = Res.string.home),
    Grades(title = Res.string.grades),
    Settings(title = Res.string.settings)
}

@Composable
fun AppBar (
    currentScreen : AppScreen,
    canNavigateBack : Boolean,
    navigateUp : () -> Unit,
    modifier : Modifier = Modifier,
    navController : NavHostController
) {
    BottomAppBar(
        content = {
            IconButton(onClick = { navController.navigate(AppScreen.Home.name)} ) {
                Icon(Icons.Filled.Home, contentDescription = "Localized description")
            }
            IconButton(onClick = { navController.navigate(AppScreen.Grades.name)} ) {
                Icon(Icons.Filled.School, contentDescription = "Localized description")
            }
            IconButton(onClick = { navController.navigate(AppScreen.Settings.name)} ) {
                Icon(Icons.Filled.Settings, contentDescription = "Localized description")
            }
        }
    )
}


fun changeLogin( kvault : KVault?, username : String, password : String) {
    println("username: $username")
    kvault?.set(key = "USERNAME", stringValue = username)
    kvault?.set(key = "PASSWORD", stringValue = password)
    getSourceData(username, password).getOrNull()?.let {
        kvault?.set(key = "GRADE_DATA", stringValue = Json.encodeToString(it))
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

        // Get current back stack entry
        val backStackEntry by navController.currentBackStackEntryAsState()

        val currentScreen = AppScreen.valueOf(
            backStackEntry?.destination?.route ?: AppScreen.Home.name
        )
        
        Scaffold(
            bottomBar = {
                AppBar(
                    currentScreen = currentScreen,
                    canNavigateBack = navController.previousBackStackEntry != null,
                    navigateUp = { navController.navigateUp() },
                    navController = navController
                )
            }
        ) {
            NavHost(
                navController = navController,
                startDestination = AppScreen.Home.name,
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {

                composable(route = AppScreen.Home.name) {
                    HomeScreen()
                }
                composable(route = AppScreen.Grades.name) {
                    GradesScreen()
                }
                composable(route = AppScreen.Settings.name) {
                    SettingsScreen()
                }
            }
        }
    }
}