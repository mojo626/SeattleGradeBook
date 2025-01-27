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
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.School
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavBackStackEntry
import androidx.navigation.compose.currentBackStackEntryAsState
import com.chrissytopher.source.navigation.NavigationController
import com.chrissytopher.source.navigation.NavigationStack
import com.chrissytopher.source.themes.blueTheme.BlueAppTheme
import com.chrissytopher.source.themes.redTheme.RedAppTheme
import com.chrissytopher.source.themes.theme.AppTheme
import io.github.aakira.napier.Napier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import dev.icerock.moko.permissions.PermissionsController
import kotlinx.coroutines.IO
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.Month
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime


val LocalKVault = compositionLocalOf<KVault?> { null }
val LocalJson = compositionLocalOf { Json { ignoreUnknownKeys = true } }
val LocalSourceData = compositionLocalOf<MutableState<HashMap<String, SourceData>?>> { mutableStateOf(null) }
val LocalNavHost = compositionLocalOf<NavigationStack<NavScreen>?> { null }
val ClassForGradePage = compositionLocalOf<MutableState<Class?>> { mutableStateOf(null) }
val LocalPermissionsController = compositionLocalOf<PermissionsController> { error("no permissions controller provided") }
val AssignmentForPage = compositionLocalOf<MutableState<AssignmentSection?>> { mutableStateOf(null) }
val LocalNotificationSender = compositionLocalOf<NotificationSender?> { null }
val LocalPlatform = compositionLocalOf<Platform> { error("no platform provided") }
val RefreshedAlready = compositionLocalOf { mutableStateOf(false) }
val ShowMiddleName = compositionLocalOf<MutableState<Boolean?>> { mutableStateOf(null) }
val LastClassMeta = compositionLocalOf<MutableState<List<ClassMeta>?>> { mutableStateOf(null) }
val LocalGradeColors = compositionLocalOf<MutableState<GradeColors>> { error("no colors provided") }

enum class NavScreen(val selectedIcon: ImageVector, val unselectedIcon: ImageVector, val showInNavBar: Boolean = true, val hideNavBar: Boolean = false) {
    School(Icons.Filled.School, Icons.Outlined.School, showInNavBar = false),
    Grades(Icons.Filled.Home, Icons.Outlined.Home, showInNavBar = false),
    Settings(Icons.Filled.Settings, Icons.Outlined.Settings, showInNavBar = false),
    Colors(Icons.Filled.Settings, Icons.Outlined.Settings, showInNavBar = false),
    Home(Icons.Filled.Home, Icons.Outlined.Home),
    Onboarding(Icons.Filled.Settings, Icons.Outlined.Settings, showInNavBar = false, hideNavBar = true),
    More(Icons.Filled.Lightbulb, Icons.Outlined.Lightbulb),
    GPA(Icons.Filled.Lightbulb, Icons.Outlined.Lightbulb, showInNavBar = false),
    Calculator(Icons.Filled.Lightbulb, Icons.Outlined.Lightbulb, showInNavBar = false),
    Assignments(Icons.Filled.Home, Icons.Outlined.Home, showInNavBar = false),
}

@Composable
fun AppBottomBar(currentScreenState: State<NavScreen>, select: (NavScreen) -> Unit) {
    val currentScreen by currentScreenState
    NavigationBar {
        NavScreen.entries.filter { it.showInNavBar }.forEach { item ->
            NavigationBarItem(
                icon = {
                    Icon(
                        if (currentScreen == item) item.selectedIcon else item.unselectedIcon,
                        contentDescription = item.name
                    )
                },
                label = { Text(item.name) },
                selected = currentScreen == item,
                onClick = { select(item) }
            )
        }
    }
}

@Composable
@Preview
fun App() {
    val kvault = LocalKVault.current
    val loggedIn = remember { kvault?.existsObject(USERNAME_KEY) == true }
    val navigationStack : NavigationStack<NavScreen> = remember { NavigationStack(if (loggedIn) NavScreen.Home else NavScreen.Onboarding) }
    CompositionLocalProvider(LocalNavHost provides navigationStack) {
        CompositionLocalProvider(LocalGradeColors provides mutableStateOf(kvault?.string(GRADE_COLORS_KEY)?.let { runCatching { Json.decodeFromString<GradeColors>(it) }.getOrNull() } ?: GradeColors.default())) {
            val localJson = LocalJson.current
            if (LocalSourceData.current.value == null) {
                kvault?.string(forKey = SOURCE_DATA_KEY)?.let { gradeData ->
                    LocalSourceData.current.value = runCatching { localJson.decodeFromString<HashMap<String, SourceData>>(gradeData) }.getOrNullAndThrow()
                }
            }
            var currentTheme by remember { mutableStateOf( ThemeVariant.valueOf(kvault?.string("THEME") ?: "Classic")) }

            ThemeSwitcher(currentTheme) {
                Scaffold(
                    bottomBar = {
                        val currentNav by navigationStack.routeState
                        if (!currentNav.hideNavBar) {
                            AppBottomBar(
                                navigationStack.routeState
                            ) {
                                navigationStack.clearStack(NavScreen.Home)
                                if (it != NavScreen.Home) {
                                    navigationStack.navigateTo(it)
                                }
                            }
                        }
                    }
                ) { paddingValues ->
                    NavigationController(
                        navigationStack = navigationStack,
    //                    startDestination = if (loggedIn) NavScreen.Home.name else NavScreen.Onboarding.name,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues)

                    ) {
                        composable(route = NavScreen.Home) {
                            HomeScreen()
                        }
                        composable(route = NavScreen.Grades) {
                            GradesScreen()
                        }
                        composable(route = NavScreen.Settings) {
                            SettingsScreen()
                        }
                        composable(route = NavScreen.Onboarding) {
                            OnboardingScreen()
                        }
                        composable(route = NavScreen.More) {
                            MoreScreen()
                        }
                        composable(route = NavScreen.GPA) {
                            GPAScreen()
                        }
                        composable(route = NavScreen.Calculator) {
                            GradeCalculatorScreen()
                        }
                        composable(route = NavScreen.Assignments) {
                            AssignmentScreen()
                        }
                        composable(route = NavScreen.School) {
                            SchoolScreen()
                        }
                        composable(route = NavScreen.Colors) {
                            ColorsScreen(currentTheme = currentTheme, onThemeChange = {newTheme -> currentTheme = newTheme})
                        }
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

fun getCurrentQuarter(): String {
    val date = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
    val (s1Offset, s2Offset) = if (date.month > Month.AUGUST) {
        Pair(0, 1)
    } else {
        Pair(-1, 0)
    }
    val q4Start = LocalDate(date.year+s2Offset, Month.APRIL, 9)
    val q3Start = LocalDate(date.year+s2Offset, Month.JANUARY, 29)
    val q2Start = LocalDate(date.year+s1Offset, Month.NOVEMBER, 7)
    val q1Start = LocalDate(date.year+s1Offset, Month.SEPTEMBER, 4)
    return if (date >= q4Start) {
        "Q4"
    } else if (date >= q3Start) {
        "Q3"
    } else if (date >= q2Start) {
        "Q2"
    } else {
        "Q1"
    }
}