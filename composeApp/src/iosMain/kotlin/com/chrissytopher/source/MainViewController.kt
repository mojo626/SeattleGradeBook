package com.chrissytopher.source

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.window.ComposeUIViewController
import com.liftric.kvault.KVault
import platform.UIKit.UIViewController

fun MainViewController(getSourceData: (String, String) -> String, filesDir: String): UIViewController {
    getSourceDataSwift = getSourceData
    globalFilesDir = filesDir
    val kvault = KVault()
    return ComposeUIViewController {
        CompositionLocalProvider(LocalKVault provides kvault) {
            App()
        }
    }
}