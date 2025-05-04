package com.chrissytopher.source

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateOffsetAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.round
import androidx.compose.ui.zIndex
import coil3.compose.AsyncImage
import com.chrissytopher.source.navigation.NavigationStack
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import kotlinx.datetime.Clock
import kotlinx.datetime.Month
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import net.sergeych.sprintf.sprintf
import org.jetbrains.compose.resources.vectorResource
import source2.composeapp.generated.resources.Res
import source2.composeapp.generated.resources.settings_customized
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(viewModel: AppViewModel, navHost: NavigationStack<NavScreen>, outerPaddingValues: PaddingValues) {
    val refreshingInProgress by viewModel.refreshingInProgress
    val refreshSuccess: Boolean? by viewModel.refreshSuccess

    val sourceData by viewModel.sourceData()
//    val json = LocalJson.current
    val platform = LocalPlatform.current
//    val kvault = LocalKVault.current
    val quarters = listOf("Q1", "Q2", "S1", "Q3", "Q4", "S2")
    val selectedQuarter by viewModel.selectedQuarter()
//    val lastClassMeta = LastClassMeta.current
    var classMetas: List<ClassMeta>? by viewModel.lastClassMeta
    LaunchedEffect(sourceData, selectedQuarter) {
        classMetas = sourceData?.get(selectedQuarter)?.classes?.map { ClassMeta(it) }
    }
    val refreshedAlready by viewModel.refreshedAlready
    LaunchedEffect(true) {
        if (!refreshedAlready) {
            viewModel.refresh()
        }
    }
//    val pullState = rememberStatusPullRefreshState(refreshingInProgress, refreshSuccess, onRefresh = { viewModel.refresh() } )
    val pullState = rememberPullToRefreshState()
    val gradeColors by viewModel.gradeColors()
    val isRefreshing = refreshingInProgress || refreshSuccess != null
    PullToRefreshBox(
        state = pullState,
        isRefreshing = isRefreshing,
        onRefresh = { viewModel.refresh() },
        indicator = {
            StatusPullRefreshIndicator(isRefreshing, refreshSuccess, pullState, gradeColors.AColor, gradeColors.EColor, modifier = Modifier.align(Alignment.TopCenter).zIndex(3f))
        },
        modifier = Modifier.fillMaxSize()
    ) {
        val updatedClasses by viewModel.updateClasses()
        Scaffold(topBar = {
            Column(Modifier.zIndex(1.1f).hazeEffect(state = viewModel.hazeState, style = hazeMaterial()).padding(bottom = 8.dp, top = outerPaddingValues.calculateTopPadding(), start = outerPaddingValues.calculateStartPadding(LocalLayoutDirection.current), end = outerPaddingValues.calculateEndPadding(LocalLayoutDirection.current))) {
                Row(Modifier.zIndex(2f).fillMaxWidth().padding(5.dp), verticalAlignment = Alignment.CenterVertically) {
                    var bigPfp by remember { mutableStateOf(false) }
                    var normalPfpSize by remember { mutableStateOf(IntSize.Zero) }
                    val screenSize = getScreenSize()
                    val pfpBigWidth = 0.9f
                    val bigScalar = (screenSize.width.toFloat() * pfpBigWidth) / normalPfpSize.width.toFloat()
                    val pfpScale by animateFloatAsState(if (bigPfp) bigScalar else 1f)
                    val pfpPosition by animateOffsetAsState(
                        if (bigPfp) Offset(
                            screenSize.width.toFloat() * pfpBigWidth / 2f,
                            screenSize.height.toFloat() * pfpBigWidth / 2f
                        ) else Offset(0f, 0f)
                    )
                    Box(Modifier.zIndex(2f).onSizeChanged { if (!bigPfp) normalPfpSize = it }.offset { pfpPosition.round() }.scale(pfpScale)) {
                        val pfpImage = remember { "file://${platform.filesDir()}/pfp.jpeg" }
                        AsyncImage(
                            pfpImage,
                            "Content",
                            Modifier.size(50.dp).shadow(8.dp, CircleShape).clip(CircleShape).clickable { bigPfp = !bigPfp },
                            filterQuality = FilterQuality.High,
                            alignment = Alignment.Center,
                            contentScale = ContentScale.FillWidth,
                        )
                    }
                    var studentName = sourceData?.get(selectedQuarter)?.student_name ?: ""
                    val showMiddleName by viewModel.showMiddleName()
                    if (!showMiddleName) {
                        val names = studentName.split(" ")
                        studentName = "${names.first()} ${names.last()}"
                    }
                    Text(text = studentName, style = MaterialTheme.typography.titleLarge, textAlign = TextAlign.Center, modifier = Modifier.weight(1f))
                    val isLincoln = true //sourceData?.get(getCurrentQuarter())?.let { rememberSchoolFromClasses(it) }?.contains("15") == true
                    if (isLincoln) {
                        Box(
                            Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                                .clickable { navHost.navigateTo(NavScreen.Settings, animateWidth = screenSize.width.toFloat()) }
                        ) {
                            Icon(
                                imageVector = vectorResource(Res.drawable.settings_customized),
                                contentDescription = "Settings",
                                modifier = Modifier.size(40.dp).align(Alignment.Center),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    } else {
                        Spacer(Modifier.size(50.dp))
                    }
                }

                val selectionDisabledAlpha by animateFloatAsState(
                    if (refreshingInProgress) 0.5f else 1.0f,
                    animationSpec = tween(200)
                )
                Row(Modifier.alpha(selectionDisabledAlpha)) {
                    for (quarter in quarters) {
                        BadgedBox(modifier = Modifier.weight(1f).padding(5.dp, 0.dp), badge = {
                            if (quarter == getCurrentQuarter() && updatedClasses.isNotEmpty()) {
                                Badge(Modifier.size(15.dp), containerColor = gradeColors.EColor)
                            }
                        }) {
                            Box(Modifier
                                    .fillMaxWidth()
                                    .background(if (selectedQuarter == quarter) CardDefaults.cardColors().containerColor else CardDefaults.cardColors().disabledContainerColor, CardDefaults.outlinedShape)
                                    .border(CardDefaults.outlinedCardBorder(selectedQuarter == quarter), CardDefaults.outlinedShape)
                                    .clickable(remember { MutableInteractionSource() }, null, enabled = !refreshingInProgress) { viewModel.setSelectedQuarter(quarter) }
                            ) {
                                Text(quarter, style = MaterialTheme.typography.titleLarge, modifier = Modifier.align(Alignment.Center), textAlign = TextAlign.Center)
                            }
                        }
                    }
                }
            }
        }) { paddingValues ->
            Column(Modifier.hazeSource(viewModel.hazeState).padding(start = 10.dp+paddingValues.calculateStartPadding(LocalLayoutDirection.current), end = 10.dp+paddingValues.calculateEndPadding(LocalLayoutDirection.current)).verticalScroll(viewModel.homeScrollState)) {
                Spacer(Modifier.height(paddingValues.calculateTopPadding()+10.dp))
                val hideMentorship by viewModel.hideMentorship()
                val (filteredClasses, filteredClassMetas) = if (hideMentorship && (updatedClasses[MENTORSHIP_NAME] != true || getCurrentQuarter() != selectedQuarter)) {
                    val mentorshipIndex = sourceData?.get(selectedQuarter)?.classes?.indexOfFirst {
                        it.name == MENTORSHIP_NAME
                    }
                    Pair(sourceData?.get(selectedQuarter)?.classes?.filterIndexed { index, _ -> index != mentorshipIndex }, classMetas?.filterIndexed { index, _ -> index != mentorshipIndex })
                } else {
                    Pair(sourceData?.get(selectedQuarter)?.classes, classMetas)
                }
                val columns = if (getScreenSize().width > getScreenSize().height) {
                    3
                } else {
                    2
                }
                filteredClasses?.chunked(columns)?.forEachIndexed {row, it ->
                    Row(modifier = Modifier.fillMaxWidth()) {
                        it.forEachIndexed { column, it ->
                            val index = row*columns + column
                            val meta = filteredClassMetas?.getOrNull(index)
                            val classForGradePage = viewModel.classForGradePage
                            val preferReported by viewModel.preferReported()
                            val screenSize = getScreenSize()
                            Box (modifier = Modifier.fillMaxSize().weight(1f).padding(10.dp)) {
                                key(sourceData) {
                                    ClassCard(it, meta, updatedClasses[it.name] ?: false, false, gradeColors, preferReported = preferReported) {
                                        classForGradePage.value = it
                                        navHost.navigateTo(NavScreen.Grades, animateWidth = screenSize.width.toFloat())
                                    }
                                }
                            }
                        }

                        (0 until columns-it.size).forEach {
                            Box (modifier = Modifier.fillMaxSize().weight(1f).padding(10.dp)) {}
                        }
                    }
                }
                Spacer(Modifier.height(outerPaddingValues.calculateBottomPadding()))
            }
        }
    }
    val now = Clock.System.now()
    val today = now.toLocalDateTime(TimeZone.currentSystemDefault()).date
    if (today.month == Month.DECEMBER && today.dayOfMonth == 25) {
        Snow(viewModel)
    }
}

@Composable
fun ClassCard(`class`: Class, meta: ClassMeta?, updates: Boolean, showDecimal: Boolean, gradeColors: GradeColors, preferReported: Boolean = false, onClick: (() -> Unit)? = null) {
    val reportedGrade = when (`class`.reported_grade) {
        "[ i ]" -> null
        else -> `class`.reported_grade
    }
    val grade = if (preferReported) {
        null
    } else {
        meta?.grade
    }
    val inner = @Composable {
        Box(Modifier.fillMaxSize()) {
            Column(Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {

                Text(grade ?: reportedGrade ?: "-", style = MaterialTheme.typography.titleLarge.copy(fontSize = MaterialTheme.typography.titleLarge.fontSize*2f, fontWeight = FontWeight.SemiBold))
                val score = if (preferReported) {
                    null
                } else {
                    if (showDecimal) {
                        meta?.finalScore?.let { "%.2f".sprintf(it) }
                    } else {
                        meta?.finalScore?.roundToInt()?.toString()
                    }
                }
                Text(score ?: `class`.reported_score ?: " ", style = MaterialTheme.typography.titleLarge)
                Text(`class`.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
            }
        }
    }
    val modifier = Modifier.fillMaxWidth().aspectRatio(1f)
    val themeModifier = darkModeColorModifier()
    val colors = (grade ?: reportedGrade)?.first()?.toString()?.let {gradeColors.gradeColor(it)?.let {CardDefaults.cardColors(containerColor = it*themeModifier) } } ?: CardDefaults.cardColors()
    BadgedBox(badge = {
        if (updates) {
            Badge(Modifier.size(15.dp), containerColor = gradeColors.EColor)
        }
    }) {
        if (onClick == null) {
            Card(modifier, colors = colors, shape = RoundedCornerShape(15)) {
                inner()
            }
        } else {
            ElevatedCard(onClick, modifier = modifier, colors = colors, elevation = CardDefaults.elevatedCardElevation(10.dp), shape = RoundedCornerShape(10)) {
                inner()
            }
        }
    }
}

operator fun Color.times(x: Float): Color {
    return copy(red = this.red*x, green = this.green*x, blue = this.blue*x)
}