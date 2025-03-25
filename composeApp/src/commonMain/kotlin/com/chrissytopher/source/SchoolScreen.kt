package com.chrissytopher.source

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage

enum class School(val id: String, val displayName: String, val titleImageUrl: String, val links: List<Pair<String, String>>) {
    Lincoln(
        "15",
        "Lincoln High School",
        "https://static.hudl.com/users/temp/12919184_9833547dea2e49fe85b5969f5ebcd697.png",
        listOf(
            Pair("https://lhsconnect.com/_app/immutable/assets/sps.LqeIuqzq.avif", "https://lincolnhs.seattleschools.org/"),
            Pair("https://lhsconnect.com/_app/immutable/assets/source.C9GDibEI.avif", "https://ps.seattleschools.org/"),
        )
    )
}

@Composable
fun SchoolScreen(viewModel: AppViewModel) {
    val platform = LocalPlatform.current
    val sourceData by viewModel.sourceData()
    val school = key(sourceData == null) { remember { sourceData?.getSchool() } }
    Column {
        val titleImagePainter by viewModel.schoolTitleImage
        titleImagePainter?.let { Image(it, null, modifier = Modifier.fillMaxWidth().aspectRatio(2f), contentScale = ContentScale.FillHeight) }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
            Text(school?.displayName ?: "No school", style = MaterialTheme.typography.displaySmall)
        }
        if (school == null) return
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
            Text("Official Links", style = MaterialTheme.typography.headlineSmall)
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            for ((imageUrl, url) in school.links) {
                Box(Modifier.padding(10.dp).clickable { platform.openLink(url) }) {
                    AsyncImage(imageUrl, null, modifier = Modifier.size(50.dp))
                    Icon(Icons.AutoMirrored.Outlined.OpenInNew, null, Modifier.size(20.dp).offset(5.dp, 5.dp).align(Alignment.BottomEnd))
                }
            }
        }
    }
}