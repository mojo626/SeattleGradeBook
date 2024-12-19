package com.chrissytopher.source

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Card
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.BadgedBox
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.Badge
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import coil3.compose.AsyncImage
import io.github.aakira.napier.Napier
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun HomeScreen() {
    var refreshingInProgress by remember { mutableStateOf(false) }
    var refreshSuccess: Boolean? by remember { mutableStateOf(null) }

    var sourceData by LocalSourceData.current
    val json = LocalJson.current
    val platform = LocalPlatform.current
    val kvault = LocalKVault.current
    val quarters = listOf("Q1", "Q2", "S1", "Q3", "Q4", "S2")
    var selectedQuarter by remember { mutableStateOf(kvault?.string(QUARTER_KEY) ?: getCurrentQuarter()) }
    var classMetas: List<ClassMeta>? by remember { mutableStateOf(sourceData?.get(selectedQuarter)?.classes?.map { ClassMeta(it) }) }
    LaunchedEffect(sourceData, selectedQuarter) {
        classMetas = sourceData?.get(selectedQuarter)?.classes?.map { ClassMeta(it) }
    }
    val refreshCoroutineScope = rememberCoroutineScope()
    var refreshedAlready by RefreshedAlready.current
    val refresh = {
        kvault?.string(USERNAME_KEY)?.let { username ->
            kvault.string(PASSWORD_KEY)?.let { password ->
                val quarter = kvault.string(QUARTER_KEY) ?: getCurrentQuarter()
                refreshCoroutineScope.launch {
                    refreshingInProgress = true
                    val newSourceData = platform.gradeSyncManager.getSourceData(username, password, quarter, false).getOrNullAndThrow()
                    if (newSourceData != null && !(newSourceData.classes.isEmpty() && newSourceData.past_classes.isEmpty())) {
                        if (quarter == getCurrentQuarter()) {
                            val currentUpdates = kvault.string(CLASS_UPDATES_KEY)?.let { json.decodeFromString<List<String>>(it) } ?: listOf()
                            val updatedClasses = newSourceData.classes.filter { newClass ->
                                val oldClass = sourceData?.get(quarter)?.classes?.find { it.name == newClass.name} ?: return@filter false
                                (oldClass.totalSections() != newClass.totalSections())
                            }
                            kvault.set(CLASS_UPDATES_KEY, json.encodeToString(currentUpdates + updatedClasses.map { it.name }))
                        }
                        sourceData = HashMap(sourceData ?: HashMap()).apply {
                            set(quarter, newSourceData)
                        }
                        kvault.set(SOURCE_DATA_KEY, json.encodeToString(sourceData))
                        refreshSuccess = true
                    } else {
                        refreshSuccess = false
                    }
                    refreshedAlready = true
                    refreshingInProgress = false
                    delay(500)
                    refreshSuccess = null
                }
            }
        }
    }
    LaunchedEffect(true) {
        if (!refreshedAlready) {
            refresh()
        }
    }
    val pullState = rememberStatusPullRefreshState(refreshingInProgress, refreshSuccess, onRefresh = { refresh() } )
    Box {
        Column (
            modifier = Modifier
                .fillMaxSize()
                .pullRefresh(pullState)
        ) {
            Row(Modifier.fillMaxWidth().padding(5.dp), verticalAlignment = Alignment.CenterVertically) {
                val pfpImage = remember { "file://${platform.filesDir()}/pfp.jpeg" }
                Napier.d("pfp path: $pfpImage")
                AsyncImage(
                    pfpImage,
                    "Content",
                    Modifier.size(50.dp).clip(CircleShape),
                    alignment = Alignment.Center,
                    contentScale = ContentScale.FillWidth
                )
                var studentName = sourceData?.get(selectedQuarter)?.student_name ?: ""
                var showMiddleName by ShowMiddleName.current
                if (showMiddleName == null) {
                    showMiddleName = kvault?.bool(SHOW_MIDDLE_NAME_KEY)
                }
                if (showMiddleName != true) {
                    val names = studentName.split(" ")
                    studentName = "${names.first()} ${names.last()}"
                }
                Text(text = studentName, style = MaterialTheme.typography.titleLarge, textAlign = TextAlign.Center, modifier = Modifier.weight(1f))
//                if (!refreshingInProgress) {
//                    Icon(if (!refreshError) Icons.Outlined.Refresh else Icons.Outlined.Error , "Refresh grades", modifier = Modifier.size(50.dp).clickable {
//                        refresh()
//                    }.then(if (refreshError) Modifier.background(MaterialTheme.colorScheme.error, CircleShape) else Modifier).clip(CircleShape))
//                } else {
//                    CircularProgressIndicator(modifier = Modifier.size(50.dp))
//                }
                val isLincoln = true //sourceData?.get(getCurrentQuarter())?.let { rememberSchoolFromClasses(it) }?.contains("15") == true
                if (isLincoln) {
                    val navHost = LocalNavHost.current
                    Box(Modifier.size(50.dp).background(MaterialTheme.colorScheme.surfaceContainer, CircleShape).clickable {
                        navHost?.navigateTo(NavScreen.Settings)
                    }) {
                        Image(
                            imageVector = NavScreen.Settings.unselectedIcon,
                            contentDescription = "Settings",
                            modifier = Modifier.size(40.dp).align(Alignment.Center),
                            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurface),
                        )
                    }
                } else {
                    Spacer(Modifier.size(50.dp))
                }
            }
            val selectionDisabledAlpha by animateFloatAsState(if (refreshingInProgress) 0.5f else 1.0f, animationSpec = tween(200))
            Row(Modifier.alpha(selectionDisabledAlpha)) {
                for (quarter in quarters) {
                    Box(Modifier.weight(1f).padding(5.dp, 0.dp)) {
                        Box(Modifier
                            .fillMaxWidth()
                            .background(if (selectedQuarter == quarter) CardDefaults.cardColors().containerColor else CardDefaults.cardColors().disabledContainerColor, CardDefaults.outlinedShape)
                            .border(CardDefaults.outlinedCardBorder(selectedQuarter == quarter), CardDefaults.outlinedShape)
                            .clickable(remember { MutableInteractionSource() }, null, enabled = !refreshingInProgress) {
                                if (quarter == getCurrentQuarter()) {
                                    kvault?.deleteObject(QUARTER_KEY)
                                } else {
                                    kvault?.set(QUARTER_KEY, quarter)
                                }
                                selectedQuarter = quarter
                                refresh()
                            }
                        ) {
                            Text(quarter, style = MaterialTheme.typography.titleLarge, modifier = Modifier.align(Alignment.Center), textAlign = TextAlign.Center)
                        }
                    }

                }
            }
            Column(Modifier.verticalScroll(rememberScrollState())) {
                val hideMentorship = remember { mutableStateOf(kvault?.bool(HIDE_MENTORSHIP_KEY) ?: false) }
                val (filteredClasses, filteredClassMetas) = if (hideMentorship.value) {
                    val mentorshipIndex = sourceData?.get(selectedQuarter)?.classes?.indexOfFirst {
                        it.name == MENTORSHIP_NAME
                    }
                    Pair(sourceData?.get(selectedQuarter)?.classes?.filterIndexed { index, _ -> index != mentorshipIndex }, classMetas?.filterIndexed { index, _ -> index != mentorshipIndex })
                } else {
                    Pair(sourceData?.get(selectedQuarter)?.classes, classMetas)
                }
                //converting to a hashmap saves looping through the list for each of the ui cards below
                val updateClasses = kvault?.string(CLASS_UPDATES_KEY)?.let { json.decodeFromString<List<String>>(it) }
                var updateClassesMap = hashMapOf(
                    *(updateClasses?.map { Pair(it, true) }?.toTypedArray() ?: arrayOf())
                )
//                updateClasses?.forEach { updateClassesMap[it] = true }
                if (getCurrentQuarter() != kvault?.string(QUARTER_KEY)) {
                    updateClassesMap = hashMapOf()
                }
                filteredClasses?.chunked(2)?.forEachIndexed {row, it ->
                    Row(modifier = Modifier.fillMaxWidth()) {
                        it.forEachIndexed { column, it ->
                            val index = row*2 + column
                            val meta = filteredClassMetas?.getOrNull(index)
                            val classForGradePage = ClassForGradePage.current
                            val navHost = LocalNavHost.current
                            Box (modifier = Modifier.fillMaxSize().weight(1f).padding(10.dp)) {
                                key(sourceData) {
                                    ClassCard(it, meta, updateClassesMap[it.name] ?: false) {
                                        classForGradePage.value = it
                                        navHost?.navigateTo(NavScreen.Grades)
                                    }
                                }
                            }
                        }

                        if (it.size == 1) {
                            Box (modifier = Modifier.fillMaxSize().weight(1f).padding(10.dp)) {}
                        }
                    }
                }
            }
        }
        StatusPullRefreshIndicator(
            refreshing = refreshingInProgress,
            success = refreshSuccess,
            state = pullState,
            modifier = Modifier.align(Alignment.TopCenter),
            backgroundColor = MaterialTheme.colorScheme.surfaceContainer,
            contentColor = MaterialTheme.colorScheme.primary,
            successColor = greenColor,
            failureColor = redColor,
            statusContentColor = MaterialTheme.colorScheme.surfaceContainer,
        )
    }
}

@Composable
fun ClassCard(`class`: Class, meta: ClassMeta?, updates: Boolean, onClick: (() -> Unit)? = null) {
    val inner = @Composable {
        Box(Modifier.fillMaxSize()) {
            Column(Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(meta?.grade ?: "-", style = MaterialTheme.typography.titleLarge.copy(fontSize = MaterialTheme.typography.titleLarge.fontSize*2f, fontWeight = FontWeight.SemiBold))
                Text(meta?.finalScore?.roundToInt()?.toString() ?: " ", style = MaterialTheme.typography.titleLarge)
                Text(`class`.name, style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center)
            }
        }
    }
    val modifier = Modifier.fillMaxWidth().aspectRatio(1f)
    val themeModifier = darkModeColorModifier()
    val kvault = LocalKVault.current
    val isGeorge = remember { kvault?.string(USERNAME_KEY) == "1gdschneider" }
    val colorsList = if (isGeorge) {
        georgeGradeColors
    } else {
        gradeColors
    }
    val colors = meta?.grade?.first()?.toString()?.let {colorsList[it]?.let {CardDefaults.cardColors(containerColor = it*themeModifier) } } ?: CardDefaults.cardColors()
    BadgedBox(badge = {
        if (updates) {
            Badge(Modifier.size(15.dp))
        }
    }) {
        if (onClick == null) {
            Card(modifier, colors = colors) {
                inner()
            }
        } else {
            Card(onClick, modifier = modifier, colors = colors) {
                inner()
            }
        }
    }
}

operator fun Color.times(x: Float): Color {
    return copy(red = this.red*x, green = this.green*x, blue = this.blue*x)
}