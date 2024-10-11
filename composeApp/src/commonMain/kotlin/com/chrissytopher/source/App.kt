package com.chrissytopher.source

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
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

val LocalKVault = compositionLocalOf<KVault?> { null }
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import source2.composeapp.generated.resources.*
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.navigation.compose.composable
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.School
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.IconButton
import androidx.compose.material.BottomAppBar
import androidx.compose.material.Scaffold
import androidx.navigation.compose.currentBackStackEntryAsState


enum class AppScreen(val title : StringResource) {
    Home(title = Res.string.home),
    Grades(title = Res.string.grades)
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
        }
    )
}


@Composable
@Preview
fun App( navController : NavHostController = rememberNavController()) {
    MaterialTheme {
        var showContent by remember { mutableStateOf(false) }

        var testingSourceData = remember { getSourceData("1cjhuntwork", "joemama") }

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
                    Text("Home")
                    Text(testingSourceData)
                }
                composable(route = AppScreen.Grades.name) {
                    Text("Grades")
                }
            }
        }
    }
}