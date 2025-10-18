package com.chrissytopher.source

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.chrissytopher.source.navigation.NavigationStack
import io.github.aakira.napier.Napier
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

const val PAGE_CONTENT_URL = "https://congraduation-seattlegradebook.s3.us-west-2.amazonaws.com/page-content.json"
const val PLACEHOLDER_CONTENT = "{\n" +
        "    \"default\": {\n" +
        "        \"title\": \"Congratulations!\",\n" +
        "        \"authors\": null,\n" +
        "        \"message\": \"Loading Content...\"\n" +
        "    }\n" +
        "}"

@Composable
fun CongraduationPage(viewModel: AppViewModel, navHost: NavigationStack<NavScreen>, outerPadding: PaddingValues) {
    val allPageContentString by viewModel.congraduationsPageContent.collectAsStateWithLifecycle()
    val username by viewModel.username()
    val sourceData by viewModel.sourceData()
    val schoolId = key(sourceData) { remember { sourceData?.get(getCurrentQuarter())?.let { schoolFromClasses(it) }?.firstOrNull() } }
    val gradeLevel = sourceData?.get(getCurrentQuarter())?.grade_level
    val screenSize = getScreenSize()
    LaunchedEffect(username) { launch {
        viewModel.loadCongraduationsContent()
    } }
    val allPageContent = viewModel.json.decodeFromString<HashMap<String, NamedPageContent>>(allPageContentString ?: PLACEHOLDER_CONTENT)
    val pageContent = allPageContent.getMyContent(username ?: "default", schoolId ?: "default", gradeLevel ?: "default") ?: return
    Column(Modifier.verticalScroll(rememberScrollState()).padding(outerPadding)) {
        Box(Modifier.fillMaxWidth().aspectRatio(2f)) {
            FilledIconButton(onClick = { navHost.popStack(screenSize.width.toFloat()) }) {
                Icon(Icons.AutoMirrored.Rounded.ArrowBack, "Back")
            }
            Text(pageContent.title, style = MaterialTheme.typography.titleLarge, modifier = Modifier.align(Alignment.Center))
        }
        Box(Modifier.fillMaxWidth().padding(horizontal = 12.dp)) {
            Text(pageContent.message, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.align(Alignment.TopCenter))
        }
        if (pageContent.authors != null) {
            Box(Modifier.padding(20.dp).fillMaxWidth()) {
                Text("Written by ${pageContent.authors}", style = MaterialTheme.typography.titleMedium, modifier = Modifier.align(Alignment.Center))
            }
        }
    }
}

private fun HashMap<String, NamedPageContent>.getMyContent(username: String, schoolId: String, gradeLevel: String): NamedPageContent? {
    get(username)?.let { return it }
    get("${schoolId}_${gradeLevel}")?.let { return it }
    get("default_${gradeLevel}")?.let { return it }
    get("default_default")?.let { return it }
    return null
}

@Serializable
data class NamedPageContent(
    val title: String,
    val authors: String?,
    val message: String,
)