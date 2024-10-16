package com.chrissytopher.source

import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.liftric.kvault.KVault
import org.jetbrains.compose.ui.tooling.preview.Preview

import androidx.navigation.compose.rememberNavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.navigation.compose.composable
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.outlined.Lightbulb
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
import com.chrissytopher.source.themes.blueTheme.BlueAppTheme
import com.chrissytopher.source.themes.redTheme.RedAppTheme
import com.chrissytopher.source.themes.theme.AppTheme
import io.github.aakira.napier.Napier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
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
    More(Icons.Filled.Lightbulb, Icons.Outlined.Lightbulb),
    GPA(Icons.Filled.Lightbulb, Icons.Outlined.Lightbulb, showInNavBar = false),
    Calculator(Icons.Filled.Lightbulb, Icons.Outlined.Lightbulb, showInNavBar = false),
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
            kvault?.string(forKey = SOURCE_DATA_KEY)?.let { gradeData ->
                LocalSourceData.current.value = runCatching { localJson.decodeFromString<SourceData>(gradeData) }.getOrNullAndThrow()
            }
        }
        LaunchedEffect(true) {
            CoroutineScope(Dispatchers.IO).launch {
                kvault?.string(USERNAME_KEY)?.let { username ->
                    kvault.string(PASSWORD_KEY)?.let { password ->
                        CoroutineScope(Dispatchers.IO).launch {
                            getSourceData(username, password).getOrNullAndThrow()?.let {
                                kvault.set(SOURCE_DATA_KEY, localJson.encodeToString(it))
                                sourceData = it
                            }
                        }
                    }
                }
            }
        }

        var currentTheme by remember { mutableStateOf( ThemeVariant.valueOf(kvault?.string("THEME") ?: "Classic")) }

        ThemeSwitcher(currentTheme) {
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
                val loggedIn = remember { kvault?.existsObject(USERNAME_KEY) == true }
                NavHost(
                    navController = navController,
                    startDestination = if (loggedIn) NavScreen.Home.name else NavScreen.Onboarding.name,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)

                ) {
                    composable(route = NavScreen.Home.name) {
                        HomeScreen()
                    }
                    composable(route = NavScreen.Grades.name) {
                        GradesScreen()
                    }
                    composable(route = NavScreen.Settings.name) {
                        SettingsScreen(currentTheme = currentTheme, onThemeChange = {newTheme -> currentTheme = newTheme})
                    }
                    composable(route = NavScreen.Onboarding.name) {
                        OnboardingScreen()
                    }
                    composable(route = NavScreen.More.name) {
                        MoreScreen()
                    }
                    composable(route = NavScreen.GPA.name) {
                        GPAScreen()
                    }
                    composable(route = NavScreen.Calculator.name) {
                        GradeCalculatorScreen()
                    }
                }
            }
        }
    }
}

fun <T> Result<T>.getOrNullAndThrow(): T? {
    exceptionOrNull()?.let { Napier.w("Caught error $it") }
    return getOrNull()
}

enum class ThemeVariant {
    Classic, Red, Blue
}

@Composable
fun ThemeSwitcher(
    themeVariant: ThemeVariant,
    content: @Composable () -> Unit
) {
    when (themeVariant) {
        ThemeVariant.Classic -> AppTheme(content = content)
        ThemeVariant.Red -> RedAppTheme(content = content)
        ThemeVariant.Blue -> BlueAppTheme(content = content)
    }
}