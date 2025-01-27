package com.chrissytopher.source

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ClassWidgetPicker(pickedClass: (Class) -> Unit) {
    val json = LocalJson.current
    val kvault = LocalKVault.current
    val quarter = getCurrentQuarter()
    val sourceData = remember { kvault?.string(SOURCE_DATA_KEY)?.let { json.decodeFromString<HashMap<String, SourceData>>(it) } }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        Text("Select a Class", style = MaterialTheme.typography.titleLarge, modifier = Modifier.align(Alignment.CenterHorizontally))
        sourceData?.get(quarter)?.classes?.chunked(2)?.forEach { row ->
            Row {
                row.forEach {
                    val meta = remember { ClassMeta(it) }
                    Box (modifier = Modifier.weight(1f).padding(10.dp)) {
                        ClassCard(it, meta, false, false) {
                            pickedClass(it)
                        }
                    }
                }

                if (row.size == 1) {
                    Box(modifier = Modifier.fillMaxSize().weight(1f).padding(10.dp)) {}
                }
            }
        }
    }
}