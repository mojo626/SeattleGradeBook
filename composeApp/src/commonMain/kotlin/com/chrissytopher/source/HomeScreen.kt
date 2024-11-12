package com.chrissytopher.source

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
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import coil3.compose.AsyncImage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
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
    var classMetas: List<ClassMeta>? by remember { mutableStateOf(sourceData?.classes?.map { ClassMeta(it) }) }
    LaunchedEffect(sourceData) {
        classMetas = sourceData?.classes?.map { ClassMeta(it) }
    }
    val kvault = LocalKVault.current
    val refresh = {
        kvault?.string(USERNAME_KEY)?.let { username ->
            kvault.string(PASSWORD_KEY)?.let { password ->
                CoroutineScope(Dispatchers.IO).launch {
                    refreshingInProgress = true
                    val newSourceData = platform.getSourceData(username, password).getOrNullAndThrow()
                    if (newSourceData != null && !(newSourceData.classes.isEmpty() && newSourceData.past_classes.isEmpty())) {
                        kvault.set(SOURCE_DATA_KEY, json.encodeToString(newSourceData))
                        val currentUpdates = kvault.string(CLASS_UPDATES_KEY)?.let { json.decodeFromString<List<String>>(it) } ?: listOf()
                        val updatedClasses = newSourceData.classes.filter { newClass ->
                            val oldClass = sourceData?.classes?.find { it.name == newClass.name} ?: return@filter false
                            (oldClass.totalSections() != newClass.totalSections())
                        }
                        kvault.set(CLASS_UPDATES_KEY, json.encodeToString(currentUpdates + updatedClasses.map { it.name }))
                        sourceData = newSourceData
                        refreshSuccess = true
                    } else {
                        refreshSuccess = false
                    }
                    refreshingInProgress = false
                    delay(500)
                    refreshSuccess = null
                }
            }
        }
    }
    LaunchedEffect(true) {
        refresh()
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
                AsyncImage(
                    pfpImage,
                    "Content",
                    Modifier.size(50.dp).clip(CircleShape),
                    alignment = Alignment.Center,
                    contentScale = ContentScale.FillWidth
                )
                Text(text = "${sourceData?.student_name}", style = MaterialTheme.typography.titleLarge, textAlign = TextAlign.Center, modifier = Modifier.weight(1f))
//                if (!refreshingInProgress) {
//                    Icon(if (!refreshError) Icons.Outlined.Refresh else Icons.Outlined.Error , "Refresh grades", modifier = Modifier.size(50.dp).clickable {
//                        refresh()
//                    }.then(if (refreshError) Modifier.background(MaterialTheme.colorScheme.error, CircleShape) else Modifier).clip(CircleShape))
//                } else {
//                    CircularProgressIndicator(modifier = Modifier.size(50.dp))
//                }
                Spacer(Modifier.size(50.dp))
            }
            Column(Modifier.verticalScroll(rememberScrollState())) {
                val hideMentorship = remember { mutableStateOf(kvault?.bool(HIDE_MENTORSHIP_KEY) ?: false) }
                val (filteredClasses, filteredClassMetas) = if (hideMentorship.value) {
                    val mentorshipIndex = sourceData?.classes?.indexOfFirst {
                        it.name == MENTORSHIP_NAME
                    }
                    Pair(sourceData?.classes?.filterIndexed { index, _ -> index != mentorshipIndex }, classMetas?.filterIndexed { index, _ -> index != mentorshipIndex })
                } else {
                    Pair(sourceData?.classes, classMetas)
                }
                //converting to a hashmap saves looping through the list for each of the ui cards below
                val updateClasses = kvault?.string(CLASS_UPDATES_KEY)?.let { json.decodeFromString<List<String>>(it) }
                val updateClassesMap = hashMapOf(
                    *(updateClasses?.map { Pair(it, true) }?.toTypedArray() ?: arrayOf())
                )
                updateClasses?.forEach { updateClassesMap[it] = true }
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
    val colors = meta?.grade?.first()?.toString()?.let {gradeColors[it]?.let {CardDefaults.cardColors(containerColor = it*themeModifier) } } ?: CardDefaults.cardColors()
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