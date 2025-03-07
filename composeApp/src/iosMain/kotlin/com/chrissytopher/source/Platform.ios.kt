package com.chrissytopher.source

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.interop.LocalUIViewController
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import io.github.aakira.napier.Napier
import kotlinx.serialization.json.Json
import platform.UIKit.UIActivityViewController
import platform.UIKit.UIDevice
import platform.UIKit.UIViewController
import platform.UIKit.popoverPresentationController
import platform.UIKit.presentationController
import platform.posix.exit
import source2.composeapp.generated.resources.Res
import source2.composeapp.generated.resources.icon_apple
import source2.composeapp.generated.resources.snowflake_apple

class IOSPlatform(private val uiViewController: UIViewController?, private var filesDir: String, private var openLinkSwift: (String) -> Unit): Platform() {
    override val name: String = UIDevice.currentDevice.systemName() + " " + UIDevice.currentDevice.systemVersion

    override fun closeApp() {
        exit(0)
    }

    override fun filesDir(): String {
        return filesDir
    }

    override fun livingInFearOfBackGestures(): Boolean = false

    override fun appIcon() = Res.drawable.icon_apple
    override fun snowFlake() = Res.drawable.snowflake_apple
    override fun iconRounding() = RoundedCornerShape(20)

    override fun openLink(link: String) {
        openLinkSwift(link)
    }

    @Composable
    override fun BackHandler(enabled: Boolean, onBack: () -> Unit) {
        //😔
    }

    override fun shareText(text: String) {
        val shareViewController = UIActivityViewController(listOf(text), null)
        shareViewController.popoverPresentationController?.sourceView = uiViewController?.view
        uiViewController?.presentViewController(shareViewController, true, null)
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
actual fun getScreenSize(): IntSize {
    return LocalWindowInfo.current.containerSize
}

