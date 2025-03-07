package com.chrissytopher.source

import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import com.liftric.kvault.KVault
import dev.icerock.moko.permissions.PermissionsController
import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier
import kotlinx.serialization.json.Json

class ClassWidgetPickerActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        Napier.base(DebugAntilog())
        val kvault = KVault(this)
        super.onCreate(savedInstanceState)
        val platform = AndroidPlatform(this)
        val permissionsController = PermissionsController(this)
        permissionsController.bind(this)
        val notificationSender = AndroidNotificationSender(this)
        val appWidgetId = intent?.extras?.getInt(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        ) ?: AppWidgetManager.INVALID_APPWIDGET_ID
        val viewModel: AndroidAppViewModel by viewModels { AndroidAppViewModel.factory(this.applicationContext) }
        setContent {
//            CompositionLocalProvider(LocalPermissionsController provides permissionsController) {
//                CompositionLocalProvider(LocalKVault provides kvault) {
//                    CompositionLocalProvider(LocalNotificationSender provides notificationSender) {
                        CompositionLocalProvider(LocalPlatform provides platform) {
                            Scaffold { paddingValues ->
                                Box(Modifier.padding(paddingValues)) {
                                    ClassWidgetPicker(viewModel) {
                                        kvault.set(WIDGET_CLASS_KEY+appWidgetId, it.frn)
                                        val appWidgetManager = AppWidgetManager.getInstance(this@ClassWidgetPickerActivity)
                                        val meta = ClassMeta(it)
                                        val gradeColors = kvault.string(GRADE_COLORS_KEY)?.let { runCatching { Json.decodeFromString<GradeColors>(it) }.getOrNull() } ?: GradeColors.default()
                                        updateWidgetContent(appWidgetId, it, meta, gradeColors, appWidgetManager, this@ClassWidgetPickerActivity)
                                        val resultValue = Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                                        setResult(RESULT_OK, resultValue)
                                        finish()
                                    }
                                }
                            }
                        }
//                    }
//                }
//            }
        }
    }
}