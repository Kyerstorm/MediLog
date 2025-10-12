package com.healthcalendar.app.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.RemoteViews
import com.healthcalendar.app.MainActivity
import com.healthcalendar.app.R

/**
 * Implementation of App Widget functionality.
 * Displays today's medication reminders on the home screen.
 *
 * This provider listens for MY_PACKAGE_REPLACED so OEM launchers (including some Vivo
 * launchers) will refresh widgets after an app update. It also handles the normal
 * update lifecycle.
 */
class MedicationWidget : AppWidgetProvider() {
    private val TAG = "MedicationWidget"

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)

        val action = intent.action
        if (action == Intent.ACTION_MY_PACKAGE_REPLACED || action == Intent.ACTION_PACKAGE_REPLACED) {
            try {
                val appWidgetManager = AppWidgetManager.getInstance(context)
                val cn = ComponentName(context, MedicationWidget::class.java)
                val appWidgetIds = appWidgetManager.getAppWidgetIds(cn)
                if (appWidgetIds != null && appWidgetIds.isNotEmpty()) {
                    for (id in appWidgetIds) {
                        updateAppWidget(context, appWidgetManager, id)
                    }
                }
            } catch (t: Throwable) {
                Log.w(TAG, "Failed to refresh widgets after package replaced", t)
            }
        }
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        // There may be multiple widgets active, so update all of them
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onEnabled(context: Context) {
        // Enter relevant functionality for when the first widget is created
    }

    override fun onDisabled(context: Context) {
        // Enter relevant functionality for when the last widget is disabled
    }
}

internal fun updateAppWidget(
    context: Context,
    appWidgetManager: AppWidgetManager,
    appWidgetId: Int
) {
    // Construct the RemoteViews object
    val views = RemoteViews(context.packageName, R.layout.medication_widget)

    // Create an Intent to launch MainActivity when widget is clicked
    val intent = Intent(context, MainActivity::class.java)
    val pendingIntent = PendingIntent.getActivity(
        context,
        0,
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )
    views.setOnClickPendingIntent(R.id.widget_container, pendingIntent)

    // Instruct the widget manager to update the widget
    appWidgetManager.updateAppWidget(appWidgetId, views)
}
