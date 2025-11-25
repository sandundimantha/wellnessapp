package com.wellnessapp.tracker.workers

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.wellnessapp.tracker.MainActivity
import com.wellnessapp.tracker.R
import com.wellnessapp.tracker.DataManager

class HydrationReminderWorker(
    context: Context,
    workerParams: WorkerParameters
) : Worker(context, workerParams) {

    override fun doWork(): Result {
        val dataManager = DataManager(applicationContext)
        
        // Only show notification if hydration reminders are enabled
        if (!dataManager.isHydrationReminderEnabled()) {
            return Result.success()
        }
        
        createNotificationChannel()
        showNotification()
        
        return Result.success()
    }
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "hydration_reminder",
                "Hydration Reminder",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Reminds you to drink water"
                enableVibration(true)
                setShowBadge(true)
            }
            
            val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    private fun showNotification() {
        val dataManager = DataManager(applicationContext)
        val currentWater = dataManager.getWaterIntake()
        val goal = dataManager.getWaterGoal()
        val remaining = goal - currentWater
        
        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        
        val pendingIntent = PendingIntent.getActivity(
            applicationContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        // Create action buttons
        val drinkWaterIntent = Intent(applicationContext, MainActivity::class.java).apply {
            putExtra("action", "add_water")
            putExtra("amount", 250)
        }
        val drinkWaterPendingIntent = PendingIntent.getActivity(
            applicationContext, 1, drinkWaterIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val customAmountIntent = Intent(applicationContext, MainActivity::class.java).apply {
            putExtra("action", "custom_water")
        }
        val customAmountPendingIntent = PendingIntent.getActivity(
            applicationContext, 2, customAmountIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val title = if (remaining > 0) {
            "Stay Hydrated! 💧"
        } else {
            "Great job! You've reached your goal! 🎉"
        }
        
        val message = if (remaining > 0) {
            "You have ${remaining}ml left to reach your daily goal. Time for a drink!"
        } else {
            "You've reached your daily water goal! Keep up the great work!"
        }
        
        val builder = NotificationCompat.Builder(applicationContext, "hydration_reminder")
            .setSmallIcon(R.drawable.ic_drop)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setVibrate(longArrayOf(0, 300, 200, 300))
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
        
        // Add action buttons only if goal not reached
        if (remaining > 0) {
            builder.addAction(
                R.drawable.ic_drop,
                "Add 250ml",
                drinkWaterPendingIntent
            )
            builder.addAction(
                R.drawable.ic_add,
                "Custom Amount",
                customAmountPendingIntent
            )
        }
        
        with(NotificationManagerCompat.from(applicationContext)) {
            notify(System.currentTimeMillis().toInt(), builder.build())
        }
    }
}
