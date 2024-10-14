package com.chrissytopher.source

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.tooling.preview.Preview
import com.liftric.kvault.KVault
import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier

lateinit var filesDirectory: String

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        Napier.base(DebugAntilog())
        val kvault = KVault(this)
        super.onCreate(savedInstanceState)
        filesDirectory = this.filesDir.path
        setContent {
            CompositionLocalProvider(LocalKVault provides kvault) {
                App()
            }
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}