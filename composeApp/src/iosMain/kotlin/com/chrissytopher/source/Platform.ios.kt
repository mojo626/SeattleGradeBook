package com.chrissytopher.source

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import io.github.aakira.napier.Napier
import kotlinx.serialization.json.Json
import platform.UIKit.UIDevice
import platform.posix.exit
import source2.composeapp.generated.resources.Res
import source2.composeapp.generated.resources.icon_apple

class IOSPlatform(private var filesDir: String, private var getSourceDataSwift: (String, String) -> String, private var openLinkSwift: (String) -> Unit): Platform {
    override val name: String = UIDevice.currentDevice.systemName() + " " + UIDevice.currentDevice.systemVersion

    private val json = Json { ignoreUnknownKeys = true }

    override fun getSourceData(username: String, password: String): Result<SourceData> = runCatching {
        json.decodeFromString(getSourceDataSwift(username, password))
    }

    override fun closeApp() {
        exit(0)
    }

    override fun filesDir(): String {
        return filesDir
    }

    override fun livingInFearOfBackGestures(): Boolean = false

    override fun appIcon() = Res.drawable.icon_apple
    override fun iconRounding() = RoundedCornerShape(20)

    override fun openLink(link: String) {
        openLinkSwift(link)
    }
}

