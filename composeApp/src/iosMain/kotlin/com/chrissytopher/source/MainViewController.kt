package com.chrissytopher.source

import androidx.compose.ui.window.ComposeUIViewController
import platform.UIKit.UIViewController

fun MainViewController(getSourceData: (String, String) -> String): UIViewController {
    getSourceDataSwift = getSourceData
    return ComposeUIViewController {
        App()
    }
}