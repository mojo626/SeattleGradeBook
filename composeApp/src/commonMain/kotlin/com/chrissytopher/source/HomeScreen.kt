package com.chrissytopher.source

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue

@Composable
fun HomeScreen() {
    val sourceData: List<Class>? by LocalSourceData.current
    Column {
        Text("Home")
        Text(sourceData?.get(0)?.assignments_parsed?.get(0)?._assignmentsections?.get(0)?.name.toString())
    }
}