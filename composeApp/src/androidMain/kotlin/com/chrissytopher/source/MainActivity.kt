package com.chrissytopher.source

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.tooling.preview.Preview
import com.liftric.kvault.KVault
import dev.icerock.moko.permissions.PermissionsController
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
        val permissionsController = PermissionsController(this)
        permissionsController.bind(this)
        setContent {
            CompositionLocalProvider(LocalPermissionsController provides permissionsController) {
                CompositionLocalProvider(LocalKVault provides kvault) {
                    App()
                }
            }
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}