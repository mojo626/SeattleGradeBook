package com.chrissytopher.source

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
fun SettingsScreen() {
    Column {
        val localNavHost = LocalNavHost.current
        val kvault = LocalKVault.current
        Button(onClick = {
            kvault?.deleteObject("USERNAME")
            kvault?.deleteObject("PASSWORD")
            kvault?.deleteObject("GRADE_DATA")
            closeApp()
        }) {
            Text("Log out")
        }
    }
}