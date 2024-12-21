package com.chrissytopher.source

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.IntSize
import io.github.aakira.napier.Napier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import org.jetbrains.compose.resources.DrawableResource

abstract class Platform {
    abstract val name: String
    val gradeSyncManager by lazy { GradeSyncManager(this) }
    protected abstract fun getSourceData(username: String, password: String, quarter: String, loadPfp: Boolean): Result<SourceData>

    abstract fun closeApp()

    abstract fun filesDir(): String

    abstract fun livingInFearOfBackGestures(): Boolean

    abstract fun appIcon(): DrawableResource
    abstract fun snowFlake(): DrawableResource
    abstract fun iconRounding(): RoundedCornerShape

    abstract fun openLink(link: String)

    @Composable
    abstract fun BackHandler(enabled: Boolean, onBack: () -> Unit)

    class GradeSyncManager(private val platform: Platform) {
        private var deferredResult: Deferred<Result<SourceData>>? = null

        suspend fun getSourceData(username: String, password: String, quarter: String, loadPfp: Boolean): Result<SourceData> {
            return if (deferredResult?.isActive == true) {
                Napier.d("deferring")
                deferredResult!!.await()
            } else {
                Napier.d("running new call")
                val newDeferred = CoroutineScope(Dispatchers.Default).async {
                    platform.getSourceData(username, password, quarter, loadPfp)
                }
                deferredResult = newDeferred
                newDeferred.await()
            }
        }
    }
}

@Composable
expect fun getScreenSize(): IntSize