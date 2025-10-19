package com.chrissytopher.source

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.IntSize
import io.github.aakira.napier.Napier
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.refTo
import kotlinx.cinterop.usePinned
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.io.Buffer
import kotlinx.io.Source
import okio.BufferedSource
import platform.AVFoundation.AVPlayer
import platform.AVFoundation.AVPlayerItem
import platform.AVFoundation.play
import platform.AVFoundation.rate
import platform.UIKit.*
import platform.Foundation.*
import platform.darwin.NSObject
import platform.posix.exit
import platform.posix.memcpy
import source2.composeapp.generated.resources.Res
import source2.composeapp.generated.resources.icon_apple
import source2.composeapp.generated.resources.snowflake_apple

class IOSPlatform(private val uiViewController: UIViewController?, private var filesDir: String, private var openLinkSwift: (String) -> Unit, private var implementPlueySwift: (Boolean) -> Unit): Platform() {
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

    @OptIn(ExperimentalForeignApi::class)
    override suspend fun pickFile(mimeType: String): Source? {

        val documentPicker = UIDocumentPickerViewController(
            documentTypes = listOf(mimeType),
            inMode = UIDocumentPickerMode.UIDocumentPickerModeOpen
        )
        val channel = Channel<NSData?>()
        val documentDelegate =
            object : NSObject(), UIDocumentPickerDelegateProtocol {
                override fun documentPicker(controller: UIDocumentPickerViewController, didPickDocumentAtURL: NSURL) {
                    didPickDocumentAtURL.startAccessingSecurityScopedResource()
                    val data = NSData.dataWithContentsOfURL(didPickDocumentAtURL)
                    didPickDocumentAtURL.stopAccessingSecurityScopedResource()
                    CoroutineScope(Dispatchers.Main).launch { channel.send(data) }
                    controller.dismissViewControllerAnimated(true, null)
                }
            }
        documentPicker.setDelegate(documentDelegate)
        UIApplication.sharedApplication.keyWindow?.rootViewController?.presentViewController(documentPicker, true, null)
        val data = channel.receive() ?: return null
        val bytes = ByteArray(data.length.toInt()).apply {
            usePinned {
                memcpy(it.addressOf(0), data.bytes, data.length)
            }
        }
        return Buffer().apply { write(bytes) }
    }

    override fun imageTypeDescriptor() = "public.image"
    override fun jsonTypeDescriptor() = "public.json"
    override fun successVibration() {
        vibration(UINotificationFeedbackType.UINotificationFeedbackTypeSuccess)
    }

    override fun failureVibration() {
        vibration(UINotificationFeedbackType.UINotificationFeedbackTypeError)
    }

    override fun implementPluey(reverse: Boolean) {
        implementPlueySwift(reverse)
    }
}

fun vibration(type: UINotificationFeedbackType) {
    val generator = UINotificationFeedbackGenerator()
    generator.notificationOccurred(type)
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
actual fun getScreenSize(): IntSize {
    return LocalWindowInfo.current.containerSize
}

