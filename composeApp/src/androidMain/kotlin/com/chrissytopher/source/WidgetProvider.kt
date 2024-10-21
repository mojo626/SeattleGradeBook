package com.chrissytopher.source

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.util.Log
import android.widget.RemoteViews
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.CardDefaults
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.liftric.kvault.KVault
import kotlinx.serialization.json.Json

class WidgetProvider : AppWidgetProvider() {
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        appWidgetIds.forEachIndexed{ i, appWidgetId ->


            val kvault = KVault(context)
            val sourceData = kvault.string(SOURCE_DATA_KEY)?.let { Json.decodeFromString<SourceData>(it) } ?: return@onUpdate
            val widgetClass = sourceData.classes.find { it.frn == kvault.string(WIDGET_CLASS_KEY+appWidgetId) } ?: sourceData.classes.getOrNull(i) ?: sourceData.classes.lastOrNull() ?: return@forEachIndexed
            val meta = ClassMeta(widgetClass)
            updateWidgetContent(appWidgetId, widgetClass, meta, appWidgetManager, context)
        }
    }
}

fun updateWidgetContent(widgetId: Int, `class`: Class, meta: ClassMeta, appWidgetManager: AppWidgetManager, context: Context) {
    val pendingIntent: PendingIntent = PendingIntent.getActivity(
        context,
        0,
        Intent(context, MainActivity::class.java),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    val views: RemoteViews = RemoteViews(
        context.packageName,
        R.layout.class_appwidget_loading
    ).apply {
        setOnClickPendingIntent(R.id.background, pendingIntent)
        setTextViewText(R.id.class_name, `class`.name)
        setTextViewText(R.id.letter_grade, meta.grade ?: "-")
        setTextViewText(R.id.grade_number, meta.finalScore?.toString() ?: "-")
        val backgroundColor = gradeColors[meta.grade?.firstOrNull()?.toString() ?: "-"] ?: if (darkMode(context)) Color(0xFF40493C) else Color(0xFFDCE6D3)
        setInt(R.id.background, "setBackgroundColor", backgroundColor.toArgb())
    }

    appWidgetManager.updateAppWidget(widgetId, views)
}

private fun darkMode(context: Context): Boolean {
    val darkModeFlags = context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK// Retrieve the Mode of the App.
    val isDarkModeOn = darkModeFlags == Configuration.UI_MODE_NIGHT_YES
    return isDarkModeOn
}
