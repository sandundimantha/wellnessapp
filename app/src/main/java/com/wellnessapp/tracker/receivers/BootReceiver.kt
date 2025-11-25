package com.wellnessapp.tracker.receivers

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.wellnessapp.tracker.DataManager

class BootReceiver : BroadcastReceiver() {
    
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            // Re-schedule hydration reminder after device reboot
            val dataManager = DataManager(context)
            if (dataManager.isHydrationReminderEnabled()) {
                val interval = dataManager.getHydrationReminderInterval()
                scheduleHydrationReminder(context, interval)
            }
        }
    }
    
    private fun scheduleHydrationReminder(context: Context, intervalMinutes: Double) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, HydrationReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val intervalMillis = (intervalMinutes * 60 * 1000).toLong()
        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,
            System.currentTimeMillis() + intervalMillis,
            intervalMillis,
            pendingIntent
        )
    }
}
