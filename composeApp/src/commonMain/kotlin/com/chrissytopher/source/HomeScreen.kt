package com.chrissytopher.source

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Card
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Alignment

@Composable
fun HomeScreen() {
    val sourceData: List<Class>? by LocalSourceData.current
    Column {
        Text("Home")
        Text(sourceData?.get(0)?.assignments_parsed?.get(0)?._assignmentsections?.get(0)?.name.toString())


        var classes = sourceData

        Column (
            modifier = Modifier
                .fillMaxSize()
        ) {
            classes?.chunked(2)?.forEach {
                Row (
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    it.forEach {
                        Box (modifier = Modifier.fillMaxSize().weight(1f).padding(10.dp)) {
                            Card (modifier = Modifier.fillMaxWidth().aspectRatio(1f)) {
                                Text(it.name, modifier = Modifier.align(Alignment.center))
                            }
                        }
                        
                    }

                    if (it.size == 1)
                    {
                        Box (modifier = Modifier.fillMaxSize().weight(1f).padding(10.dp)) {
                        }
                    }
                }
            }
        }
    }
}