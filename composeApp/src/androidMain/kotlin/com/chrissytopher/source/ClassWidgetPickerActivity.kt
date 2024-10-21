package com.chrissytopher.source

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import android.widget.RemoteViews
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Surface
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import com.liftric.kvault.KVault
import dev.icerock.moko.permissions.PermissionsController
import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier

class ClassWidgetPickerActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        Napier.base(DebugAntilog())
        val kvault = KVault(this)
        super.onCreate(savedInstanceState)
        filesDirectory = this.filesDir.path
        val permissionsController = PermissionsController(this)
        permissionsController.bind(this)
        val notificationSender = AndroidNotificationSender(this)
        val appWidgetId = intent?.extras?.getInt(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        ) ?: AppWidgetManager.INVALID_APPWIDGET_ID
        setContent {
            CompositionLocalProvider(LocalPermissionsController provides permissionsController) {
                CompositionLocalProvider(LocalKVault provides kvault) {
                    CompositionLocalProvider(LocalNotificationSender provides notificationSender) {
                        Scaffold { paddingValues ->
                            Box(Modifier.padding(paddingValues)) {
                                ClassWidgetPicker {
                                    kvault.set(WIDGET_CLASS_KEY+appWidgetId, it.frn)
                                    val appWidgetManager = AppWidgetManager.getInstance(this@ClassWidgetPickerActivity)
                                    val meta = ClassMeta(it)
                                    updateWidgetContent(appWidgetId, it, meta, appWidgetManager, this@ClassWidgetPickerActivity)
                                    val resultValue = Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                                    setResult(RESULT_OK, resultValue)
                                    finish()
                                }
                            }
                        }

                    }
                }
            }
        }
    }
}