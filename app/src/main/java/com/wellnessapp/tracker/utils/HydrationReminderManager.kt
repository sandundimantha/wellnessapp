package com.wellnessapp.tracker.utils

import android.content.Context
import androidx.work.*
import com.wellnessapp.tracker.workers.HydrationReminderWorker
import java.util.concurrent.TimeUnit

object HydrationReminderManager {
    
    private const val WORK_NAME = "hydration_reminder_work"
    
    fun scheduleHydrationReminder(context: Context, intervalMinutes: Double) {
        // Cancel existing work
        cancelHydrationReminder(context)
        
        // Create constraints
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
            .setRequiresBatteryNotLow(true)
            .build()
        
        // Handle fractional minutes (like 0.5 for 30 seconds)
        val actualInterval = if (intervalMinutes < 1) {
            // For intervals less than 1 minute, use seconds
            (intervalMinutes * 60).toLong()
        } else {
            intervalMinutes.toLong()
        }
        
        val timeUnit = if (intervalMinutes < 1) TimeUnit.SECONDS else TimeUnit.MINUTES
        
        // Create periodic work request
        val hydrationWork = PeriodicWorkRequestBuilder<HydrationReminderWorker>(
            actualInterval, timeUnit
        )
            .setConstraints(constraints)
            .setInitialDelay(actualInterval, timeUnit)
            .build()
        
        // Enqueue the work
        WorkManager.getInstance(context)
            .enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.UPDATE,
                hydrationWork
            )
    }
    
    fun cancelHydrationReminder(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
    }
    
    fun scheduleImmediateReminder(context: Context) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
            .build()
        
        val immediateWork = OneTimeWorkRequestBuilder<HydrationReminderWorker>()
            .setConstraints(constraints)
            .build()
        
        WorkManager.getInstance(context).enqueue(immediateWork)
    }
    
    fun scheduleTestReminder(context: Context) {
        // Schedule a test reminder in 5 seconds
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
            .build()
        
        val testWork = OneTimeWorkRequestBuilder<HydrationReminderWorker>()
            .setConstraints(constraints)
            .setInitialDelay(5, TimeUnit.SECONDS)
            .build()
        
        WorkManager.getInstance(context).enqueue(testWork)
    }
}
