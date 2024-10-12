package com.chrissytopher.source

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Card
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import coil3.compose.AsyncImage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlin.math.roundToInt

@Composable
fun HomeScreen() {
    var refreshingInProgress by remember { mutableStateOf(false) }

    val sourceData by LocalSourceData.current
    var classMetas: List<ClassMeta>? by remember { mutableStateOf(null) }
    LaunchedEffect(sourceData) {
        classMetas = sourceData?.classes?.map { ClassMeta(it) }
    }
    Column (
        modifier = Modifier
            .fillMaxSize()
    ) {
        val kvault = LocalKVault.current
        Row(Modifier.fillMaxWidth().padding(5.dp), verticalAlignment = Alignment.CenterVertically) {
            val pfpImage = remember { "file://${filesDir()}/pfp.jpeg" }
            AsyncImage(
                pfpImage,
                "Content",
                Modifier.size(50.dp).clip(CircleShape),
                alignment = Alignment.Center,
                contentScale = ContentScale.FillWidth
            )
            Text(text = "${sourceData?.student_name}", style = MaterialTheme.typography.titleLarge, textAlign = TextAlign.Center, modifier = Modifier.weight(1f))
            if (!refreshingInProgress) {
                val sourceDataState = LocalSourceData.current
                val json = LocalJson.current
                Icon(Icons.Outlined.Refresh, "Refresh grades", modifier = Modifier.size(50.dp).clickable {
                    kvault?.string("USERNAME")?.let { username ->
                        kvault.string("PASSWORD")?.let { password ->
                            CoroutineScope(Dispatchers.IO).launch {
                                refreshingInProgress = true
                                val sourceData = getSourceData(username, password).getOrNull()
                                kvault.set("SOURCE_DATA", json.encodeToString(sourceData))
                                sourceDataState.value = sourceData
                                refreshingInProgress = false
                            }
                        }
                    }
                }.clip(CircleShape))
            } else {
                CircularProgressIndicator(modifier = Modifier.size(50.dp))
            }
        }
        val hideMentorship = remember { mutableStateOf(kvault?.bool("HIDE_MENTORSHIP") ?: false) }
        val filteredClasses = if (hideMentorship.value) {
            sourceData?.classes?.filter {
                it.name != "MENTORSHIP"
            }
        } else {
            sourceData?.classes
        }
        filteredClasses?.chunked(2)?.forEachIndexed {row, it ->
            Row (modifier = Modifier.fillMaxWidth()) {
                it.forEachIndexed { column, it ->
                    val index = row*2 + column
                    val meta = classMetas?.getOrNull(index)
                    Box (modifier = Modifier.fillMaxSize().weight(1f).padding(10.dp)) {
                        Card(modifier = Modifier.fillMaxWidth().aspectRatio(1f), colors = meta?.grade?.first()?.toString()?.let {gradeColors[it]?.let {CardDefaults.cardColors(containerColor = it) } } ?: CardDefaults.cardColors()) {
                            Box(Modifier.fillMaxSize()) {
                                Column(Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(meta?.grade ?: "-")
                                    Text(meta?.finalScore?.roundToInt()?.toString() ?: " ")
                                    Text(it.name)
                                }
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

val gradeColors = mapOf(
    "A" to Color(0xFF64ed72),
    "B" to Color(0xFF69d0f5),
    "C" to Color(0xFFf0e269),
    "D" to Color(0xFFf09151),
    "E" to Color(0xFFf24646),
)
