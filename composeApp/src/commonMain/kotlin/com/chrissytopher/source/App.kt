package com.chrissytopher.source

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material.icons.outlined.School
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.chrissytopher.source.navigation.NavigationController
import com.chrissytopher.source.navigation.NavigationStack
import com.chrissytopher.source.themes.blueTheme.BlueAppTheme
import com.chrissytopher.source.themes.blueTheme.lightScheme
import com.chrissytopher.source.themes.redTheme.RedAppTheme
import com.chrissytopher.source.themes.theme.AppTheme
import com.chrissytopher.source.themes.theme.AppTypography
import com.github.ajalt.colormath.model.RGB
import com.materialkolor.ktx.toHex
import com.materialkolor.rememberDynamicColorScheme
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.materials.ExperimentalHazeMaterialsApi
import dev.chrisbanes.haze.materials.HazeMaterials
import io.github.aakira.napier.Napier
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import kotlinx.datetime.TimeZone
import kotlinx.datetime.daysUntil
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.ui.tooling.preview.Preview

val LocalPlatform = compositionLocalOf<Platform> { error("no platform provided") }
val WithinApp = compositionLocalOf { false }

@OptIn(ExperimentalHazeMaterialsApi::class)
@Composable
fun hazeMaterial() = HazeMaterials.regular(MaterialTheme.colorScheme.surface)

enum class NavScreen(val selectedIcon: ImageVector, val unselectedIcon: ImageVector, val showInNavBar: Boolean = true, val hideNavBar: Boolean = false, val displayName: String? = null) {
//    School(Icons.Filled.School, Icons.Outlined.School, showInNavBar = false),
    Grades(Icons.Filled.Home, Icons.Outlined.Home, showInNavBar = false),
    Settings(Icons.Filled.Settings, Icons.Outlined.Settings, showInNavBar = false),
    Colors(Icons.Filled.Settings, Icons.Outlined.Settings, showInNavBar = false),
    Home(Icons.Filled.Home, Icons.Outlined.Home, displayName = "Grades"),
    Onboarding(Icons.Filled.Settings, Icons.Outlined.Settings, showInNavBar = false, hideNavBar = true),
    More(Icons.Filled.Lightbulb, Icons.Outlined.Lightbulb),
    GPA(Icons.Filled.Lightbulb, Icons.Outlined.Lightbulb, showInNavBar = false),
    Calculator(Icons.Filled.Lightbulb, Icons.Outlined.Lightbulb, showInNavBar = false),
    Congraduation(Icons.Filled.Settings, Icons.Outlined.Settings, showInNavBar = false, hideNavBar = true),
}

@OptIn(ExperimentalHazeMaterialsApi::class)
@Composable
fun AppBottomBar(currentScreenState: State<NavScreen>, viewModel: AppViewModel, select: (NavScreen) -> Unit) {
    val currentScreen by currentScreenState
    NavigationBar(containerColor = Color.Transparent, modifier = Modifier.hazeEffect(viewModel.hazeState, style = hazeMaterial())) {
        var entries = NavScreen.entries.filter { it.showInNavBar }
        val sourceData by viewModel.sourceData()
        val isLincoln = sourceData?.getSchool() == School.Lincoln
//        if (isLincoln) {
//            entries = listOf(NavScreen.School) + entries
//        }
        entries.forEach { item ->
            NavigationBarItem(
                icon = {
                    Icon(
                        if (currentScreen == item) item.selectedIcon else item.unselectedIcon,
                        contentDescription = item.displayName ?: item.name
                    )
                },
                label = { Text(item.displayName ?: item.name) },
                selected = currentScreen == item,
                onClick = { select(item) }
            )
        }
    }
}

@Composable
@Preview
fun App(viewModel: AppViewModel) {
    val username by viewModel.username()
    val loggedIn = remember { username != null }
    val navigationStack : NavigationStack<NavScreen> = remember { NavigationStack(if (loggedIn) NavScreen.Home else NavScreen.Onboarding) }
    val platform = LocalPlatform.current
    val classForGradePage by viewModel.classForGradePage.collectAsState()
    navigationStack.onStackChanged = { fromRoute, toRoute ->
        if (toRoute == NavScreen.Home && fromRoute == NavScreen.Grades) {
            if (classForGradePage?.let { ClassMeta(it) }?.grade == "P") {
                platform.implementPluey(reverse = true)
            }
        }
    }
//    CompositionLocalProvider(LocalNavHost provides navigationStack) {
//        CompositionLocalProvider(LocalGradeColors provides mutableStateOf(kvault?.string(GRADE_COLORS_KEY)?.let { runCatching { Json.decodeFromString<GradeColors>(it) }.getOrNull() } ?: GradeColors.default())) {
//            val localJson = LocalJson.current
//            if (LocalSourceData.current.value == null) {
//                kvault?.string(forKey = SOURCE_DATA_KEY)?.let { gradeData ->
//                    LocalSourceData.current.value = runCatching { localJson.decodeFromString<HashMap<String, SourceData>>(gradeData) }.getOrNullAndThrow()
//                }
//            }
            val currentTheme by viewModel.currentTheme()

            ThemeSwitcher(currentTheme, viewModel) {
                Scaffold(
                    bottomBar = {
                        val currentNav by navigationStack.routeState
                        if (!currentNav.hideNavBar) {
                            AppBottomBar(
                                navigationStack.routeState,
                                viewModel,
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
                            .background(MaterialTheme.colorScheme.surfaceContainerLow)
                    ) {
                        val screenWidth = getScreenSize().width.toFloat()
                        fun navigateTo(route: NavScreen) {
                            navigationStack.navigateTo(route, animateWidth = screenWidth)
                        }
                        fun navigateBack() {
                            navigationStack.popStack(animateWidth = screenWidth)
                        }
                        fun clearStack(newInitialRoute: NavScreen) {
                            navigationStack.clearStack(newInitialRoute, animateWidth = screenWidth)
                        }
                        composable(route = NavScreen.Home) {
                            HomeScreen(viewModel, ::navigateTo, paddingValues)
                        }
                        composable(route = NavScreen.Grades) {
                            GradesScreen(viewModel, ::navigateBack, ::navigateTo, paddingValues)
                        }
                        composable(route = NavScreen.Settings) {
                            SettingsScreen(viewModel, ::navigateBack, ::navigateTo, ::clearStack, paddingValues)
                        }
                        composable(route = NavScreen.Onboarding) {
                            OnboardingScreen(viewModel, ::clearStack, paddingValues)
                        }
                        composable(route = NavScreen.More) {
                            MoreScreen(viewModel, ::navigateTo, paddingValues)
                        }
                        composable(route = NavScreen.GPA) {
                            GPAScreen(viewModel, paddingValues)
                        }
                        composable(route = NavScreen.Calculator) {
                            GradeCalculatorScreen(viewModel, ::navigateBack, paddingValues)
                        }
//                        composable(route = NavScreen.School) {
//                            Box(Modifier.padding(paddingValues)) {
//                                SchoolScreen(viewModel)
//                            }
//                        }
                        composable(route = NavScreen.Colors) {
                            ColorsScreen(viewModel, ::navigateBack, paddingValues)
                        }
                        composable(route = NavScreen.Congraduation) {
                            CongraduationPage(viewModel, ::navigateBack, paddingValues)
                        }
                    }
                }
            }
//        }
//    }
}
@Composable
fun NavSwitcher(viewModel: AppViewModel, screen: NavScreen?, paddingValues: PaddingValues, navigateTo: (NavScreen) -> Unit, navigateBack: () -> Unit, navigationStackCleared: (NavScreen) -> Unit) {
    when (screen) {
        NavScreen.Grades -> GradesScreen(viewModel, navigateBack, navigateTo, paddingValues)
        NavScreen.Settings -> SettingsScreen(viewModel, navigateBack, navigateTo, navigationStackCleared, paddingValues)
        NavScreen.Colors -> ColorsScreen(viewModel, navigateBack, paddingValues)
        NavScreen.Home -> HomeScreen(viewModel, navigateTo, paddingValues)
        NavScreen.Onboarding -> OnboardingScreen(viewModel, navigationStackCleared, paddingValues)
        NavScreen.More -> MoreScreen(viewModel, navigateTo, paddingValues)
        NavScreen.GPA -> GPAScreen(viewModel, paddingValues)
        NavScreen.Calculator -> GradeCalculatorScreen(viewModel, navigateBack, paddingValues)
        NavScreen.Congraduation -> CongraduationPage(viewModel, navigateBack, paddingValues)
        else -> {}
    }
}

fun <T> Result<T>.getOrNullAndThrow(): T? {
    exceptionOrNull()?.let { Napier.w("Caught error $it") }
    return getOrNull()
}

enum class ThemeVariant {
    Classic, Red, Blue, Dynamic
}

@Composable
fun ThemeSwitcher(
    themeVariant: ThemeVariant,
    viewModel: AppViewModel,
    content: @Composable () -> Unit
) {
    when (themeVariant) {
        ThemeVariant.Classic -> AppTheme(content = content)
        ThemeVariant.Red -> RedAppTheme(content = content)
        ThemeVariant.Blue -> BlueAppTheme(content = content)
        ThemeVariant.Dynamic -> run {
            val isDark = isSystemInDarkTheme()
            val colorScheme = rememberDynamicColorScheme(seedColor = averageGradeColor(viewModel), isDark = isDark)
            MaterialTheme(colorScheme = if (isDark) colorScheme else colorScheme.copy(
                onSurface = lightScheme.onSurface,
                onSurfaceVariant = lightScheme.onSurfaceVariant,
                onBackground = lightScheme.onBackground,
                primaryContainer = colorScheme.primary,
                onPrimaryContainer = colorScheme.onPrimary,
                secondaryContainer = colorScheme.primary,
                onSecondaryContainer = colorScheme.onPrimary,
            ), typography = AppTypography) {
                CompositionLocalProvider(LocalTextStyle provides LocalTextStyle.current, content = content)
            }
        }
    }
}

@Composable
fun averageGradeColor(viewModel: AppViewModel): Color {
    val metas by viewModel.lastClassMeta
    val sourceData by viewModel.sourceData()
    val selectedQuarter by viewModel.selectedQuarter()
    val currentClasses = sourceData?.get(selectedQuarter)?.classes
    val preferReported by viewModel.preferReported()
    val grades = metas?.mapIndexed { i, it ->
        (if (preferReported) currentClasses?.getOrNull(i)?.reported_grade else null) ?: it.grade ?: currentClasses?.getOrNull(i)?.reported_grade
    }
    val gradeColors by viewModel.gradeColors()
    val nothingColor = RGB(CardDefaults.cardColors().containerColor.toHex()).toOklab()
    val colors = (grades?.map { gradeColors.gradeColor(it?.firstOrNull()?.toString() ?: "")?.let { RGB(it.red, it.green, it.blue).toOklab() } ?: nothingColor } ?: listOf(nothingColor)).toMutableList()
    var averageColor = colors.removeLastOrNull() ?: nothingColor
    for (color in colors) {
        averageColor = averageColor.copy(l = (averageColor.l+color.l)/2f, a = (averageColor.a+color.a)/2f, b = (averageColor.b+color.b)/2f)
    }
    val averageSrgb = averageColor.toSRGB()
    return Color(averageSrgb.r, averageSrgb.g, averageSrgb.b)
}

fun getCurrentQuarter(): String {
    val date = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
    val (s1Offset, s2Offset) = if (date.month > Month.AUGUST) {
        Pair(0, 1)
    } else {
        Pair(-1, 0)
    }
    val finalDay = LocalDate(date.year+s2Offset, Month.JUNE, 18)
    val q4Start = LocalDate(date.year+s2Offset, Month.APRIL, 9)
    val q3Start = LocalDate(date.year+s2Offset, Month.JANUARY, 29)
    val q2Start = LocalDate(date.year+s1Offset, Month.NOVEMBER, 7)
    val q1Start = LocalDate(date.year+s1Offset, Month.SEPTEMBER, 4)
    if (date.daysUntil(finalDay) <= 7*3 && date.daysUntil(finalDay) >= 0) {
        return "S2"
    }
    if (date.daysUntil(q3Start) <= 7*3 && date.daysUntil(q3Start) > -3) {
        return "S1"
    }
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
