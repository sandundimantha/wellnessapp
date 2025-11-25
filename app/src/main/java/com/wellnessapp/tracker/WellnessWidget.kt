package com.wellnessapp.tracker

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews

class WellnessWidget : AppWidgetProvider() {
    
    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
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
    
    companion object {
        fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
            val dataManager = DataManager(context)
            val habits = dataManager.getHabits()
            val completedToday = habits.count { it.isCompletedToday() }
            val totalHabits = habits.size
            val completionPercentage = if (totalHabits > 0) {
                (completedToday * 100) / totalHabits
            } else {
                0
            }
            
            val currentWater = dataManager.getWaterIntake()
            val waterGoal = dataManager.getWaterGoal()
            
            // Construct the RemoteViews object
            val views = RemoteViews(context.packageName, R.layout.widget_wellness)
            
            // Update text views
            views.setTextViewText(R.id.habits_progress_text, "$completedToday/$totalHabits")
            views.setTextViewText(R.id.completion_percentage, "$completionPercentage%")
            views.setTextViewText(R.id.water_intake_widget, "Water: ${currentWater}ml / ${waterGoal}ml")
            
            // Update progress bar
            views.setProgressBar(R.id.habits_progress_bar, 100, completionPercentage, false)
            
            // Set click intent to open the app
            val intent = Intent(context, MainActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(
                context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_container, pendingIntent)
            
            // Instruct the widget manager to update the widget
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}
