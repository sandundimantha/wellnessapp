package com.wellnessapp.tracker

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.wellnessapp.tracker.models.Habit
import com.wellnessapp.tracker.models.Mood
import com.wellnessapp.tracker.models.Notification
import com.wellnessapp.tracker.models.NotificationType
import java.text.SimpleDateFormat
import java.util.*

class DataManager(context: Context) {
    
    private val prefs: SharedPreferences = context.getSharedPreferences("wellness_tracker", Context.MODE_PRIVATE)
    private val gson = Gson()
    
    // User Settings
    fun getUserName(): String = prefs.getString("user_name", "User") ?: "User"
    fun setUserName(name: String) = prefs.edit().putString("user_name", name).apply()
    
    fun getUserEmail(): String = prefs.getString("user_email", "") ?: ""
    fun setUserEmail(email: String) = prefs.edit().putString("user_email", email).apply()
    
    fun getUserPhone(): String = prefs.getString("user_phone", "") ?: ""
    fun setUserPhone(phone: String) = prefs.edit().putString("user_phone", phone).apply()
    
    fun getUserDOB(): String = prefs.getString("user_dob", "") ?: ""
    fun setUserDOB(dob: String) = prefs.edit().putString("user_dob", dob).apply()
    
    fun getUserHeight(): String = prefs.getString("user_height", "") ?: ""
    fun setUserHeight(height: String) = prefs.edit().putString("user_height", height).apply()
    
    fun getUserWeight(): String = prefs.getString("user_weight", "") ?: ""
    fun setUserWeight(weight: String) = prefs.edit().putString("user_weight", weight).apply()
    
    fun getUserGender(): String = prefs.getString("user_gender", "") ?: ""
    fun setUserGender(gender: String) = prefs.edit().putString("user_gender", gender).apply()
    
    fun getUserPrimaryGoal(): String = prefs.getString("user_primary_goal", "") ?: ""
    fun setUserPrimaryGoal(goal: String) = prefs.edit().putString("user_primary_goal", goal).apply()
    
    fun getUserTargetWeight(): String = prefs.getString("user_target_weight", "") ?: ""
    fun setUserTargetWeight(weight: String) = prefs.edit().putString("user_target_weight", weight).apply()
    
    fun getWaterGoal(): Int = prefs.getInt("water_goal", 2500) // Default 2.5L
    fun setWaterGoal(goal: Int) = prefs.edit().putInt("water_goal", goal).apply()
    
    fun getWaterIntake(): Int = prefs.getInt("water_intake_${getTodayString()}", 0)
    fun setWaterIntake(amount: Int) = prefs.edit().putInt("water_intake_${getTodayString()}", amount).apply()
    fun clearWaterIntake() = prefs.edit().putInt("water_intake_${getTodayString()}", 0).apply()
    
    // Step counting
    fun getTodayStepCount(): Int = prefs.getInt("step_count_${getTodayString()}", 0)
    fun setTodayStepCount(count: Int) = prefs.edit().putInt("step_count_${getTodayString()}", count).apply()
    fun getStepGoal(): Int = prefs.getInt("step_goal", 10000) // Default 10,000 steps
    fun setStepGoal(goal: Int) = prefs.edit().putInt("step_goal", goal).apply()
    
    // Hydration Reminder Settings
    fun isHydrationReminderEnabled(): Boolean = prefs.getBoolean("hydration_reminder_enabled", false)
    fun setHydrationReminderEnabled(enabled: Boolean) = prefs.edit().putBoolean("hydration_reminder_enabled", enabled).apply()
    
    fun getHydrationReminderInterval(): Double = prefs.getFloat("hydration_reminder_interval", 60f).toDouble() // Default 60 minutes
    fun setHydrationReminderInterval(interval: Double) = prefs.edit().putFloat("hydration_reminder_interval", interval.toFloat()).apply()
    
    // New Settings Methods
    fun saveReminderEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("reminder_enabled", enabled).apply()
    }
    
    fun isReminderEnabled(): Boolean {
        return prefs.getBoolean("reminder_enabled", true)
    }
    
    fun saveReminderInterval(interval: String) {
        prefs.edit().putString("reminder_interval", interval).apply()
    }
    
    fun getReminderInterval(): String {
        return prefs.getString("reminder_interval", "Every 2 hours") ?: "Every 2 hours"
    }
    
    fun saveHabitRemindersEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("habit_reminders_enabled", enabled).apply()
    }
    
    fun isHabitRemindersEnabled(): Boolean {
        return prefs.getBoolean("habit_reminders_enabled", true)
    }
    
    fun saveMoodRemindersEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("mood_reminders_enabled", enabled).apply()
    }
    
    fun isMoodRemindersEnabled(): Boolean {
        return prefs.getBoolean("mood_reminders_enabled", false)
    }
    
    
    fun saveWaterGoal(goal: Int) {
        prefs.edit().putInt("water_goal", goal).apply()
    }
    
    // Habits Management
    fun getHabits(): List<Habit> {
        val habitsJson = prefs.getString("habits", "[]")
        val type = object : TypeToken<List<Habit>>() {}.type
        return gson.fromJson(habitsJson, type) ?: emptyList()
    }
    
    fun saveHabits() {
        val habits = getHabits()
        val habitsJson = gson.toJson(habits)
        prefs.edit().putString("habits", habitsJson).apply()
    }
    
    fun saveHabits(habits: List<Habit>) {
        val habitsJson = gson.toJson(habits)
        prefs.edit().putString("habits", habitsJson).apply()
    }
    
    fun addHabit(habit: Habit) {
        val habits = getHabits().toMutableList()
        habits.add(habit)
        val habitsJson = gson.toJson(habits)
        prefs.edit().putString("habits", habitsJson).apply()
    }
    
    fun deleteHabit(habit: Habit) {
        val habits = getHabits().toMutableList()
        habits.removeAll { it.name == habit.name }
        val habitsJson = gson.toJson(habits)
        prefs.edit().putString("habits", habitsJson).apply()
    }
    
    // Mood Management
    fun getMoods(): List<Mood> {
        val moodsJson = prefs.getString("moods", "[]")
        val type = object : TypeToken<List<Mood>>() {}.type
        return gson.fromJson(moodsJson, type) ?: emptyList()
    }
    
    fun saveMoods() {
        val moods = getMoods()
        val moodsJson = gson.toJson(moods)
        prefs.edit().putString("moods", moodsJson).apply()
    }
    
    fun saveMoods(moods: List<Mood>) {
        val moodsJson = gson.toJson(moods)
        prefs.edit().putString("moods", moodsJson).apply()
    }
    
    fun addMood(mood: Mood) {
        val moods = getMoods().toMutableList()
        
        // Check if there's already a mood for today and replace it
        val today = Calendar.getInstance()
        val existingMoodIndex = moods.indexOfFirst { existingMood ->
            val moodDate = Calendar.getInstance().apply { timeInMillis = existingMood.timestamp }
            isSameDay(today, moodDate)
        }
        
        if (existingMoodIndex != -1) {
            // Replace existing mood for today
            moods[existingMoodIndex] = mood
        } else {
            // Add new mood
            moods.add(mood)
        }
        
        // Keep only last 30 days of moods
        val thirtyDaysAgo = System.currentTimeMillis() - (30 * 24 * 60 * 60 * 1000L)
        val filteredMoods = moods.filter { it.timestamp > thirtyDaysAgo }
        val moodsJson = gson.toJson(filteredMoods)
        prefs.edit().putString("moods", moodsJson).apply()
    }
    
    fun getRecentMoods(count: Int): List<Mood> {
        return getMoods().sortedByDescending { it.timestamp }.take(count)
    }
    
    fun getTodayMood(): Mood? {
        val today = Calendar.getInstance()
        val moods = getMoods()
        
        // Find the most recent mood for today
        return moods
            .filter { mood ->
                val moodDate = Calendar.getInstance().apply { timeInMillis = mood.timestamp }
                isSameDay(today, moodDate)
            }
            .maxByOrNull { it.timestamp }
    }
    
    fun getMoodTrend(days: Int): List<Mood> {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_MONTH, -days)
        val startTime = calendar.timeInMillis
        
        return getMoods()
            .filter { it.timestamp >= startTime }
            .sortedBy { it.timestamp }
    }
    
    // Helper Methods
    private fun getTodayString(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return dateFormat.format(Date())
    }
    
    private fun isSameDay(cal1: Calendar, cal2: Calendar): Boolean {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }
    
    // Statistics
    fun getHabitsCompletionRate(): Float {
        val habits = getHabits()
        if (habits.isEmpty()) return 0f
        
        val totalHabits = habits.size
        val completedHabits = habits.count { it.isCompletedToday() }
        
        return (completedHabits.toFloat() / totalHabits) * 100
    }
    
    fun getAverageMood(days: Int): Float {
        val moods = getMoodTrend(days)
        if (moods.isEmpty()) return 3f // Neutral
        
        val totalMoodValue = moods.sumOf { it.getMoodValue() }
        return totalMoodValue.toFloat() / moods.size
    }
    
    fun getWeeklyWaterAverage(): Float {
        val calendar = Calendar.getInstance()
        var totalWater = 0
        var daysWithData = 0
        
        for (i in 0..6) {
            val dateString = getDateString(calendar)
            val waterIntake = prefs.getInt("water_intake_$dateString", 0)
            if (waterIntake > 0) {
                totalWater += waterIntake
                daysWithData++
            }
            calendar.add(Calendar.DAY_OF_MONTH, -1)
        }
        
        return if (daysWithData > 0) totalWater.toFloat() / daysWithData else 0f
    }
    
    private fun getDateString(calendar: Calendar): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return dateFormat.format(calendar.time)
    }
    
    // Notification Management
    fun getNotifications(): List<Notification> {
        val notificationsJson = prefs.getString("notifications", "[]")
        val type = object : TypeToken<List<Notification>>() {}.type
        return gson.fromJson(notificationsJson, type) ?: emptyList()
    }
    
    fun addNotification(notification: Notification) {
        val notifications = getNotifications().toMutableList()
        notifications.add(0, notification) // Add to beginning
        // Keep only last 50 notifications
        val limitedNotifications = notifications.take(50)
        val notificationsJson = gson.toJson(limitedNotifications)
        prefs.edit().putString("notifications", notificationsJson).apply()
    }
    
    fun markNotificationAsRead(notificationId: String) {
        val notifications = getNotifications().toMutableList()
        val index = notifications.indexOfFirst { it.id == notificationId }
        if (index != -1) {
            notifications[index] = notifications[index].copy(isRead = true)
            val notificationsJson = gson.toJson(notifications)
            prefs.edit().putString("notifications", notificationsJson).apply()
        }
    }
    
    fun markAllNotificationsAsRead() {
        val notifications = getNotifications().map { it.copy(isRead = true) }
        val notificationsJson = gson.toJson(notifications)
        prefs.edit().putString("notifications", notificationsJson).apply()
    }
    
    fun getUnreadNotificationCount(): Int {
        return getNotifications().count { !it.isRead }
    }
    
    // Generate sample notifications for demo
    fun generateSampleNotifications() {
        val sampleNotifications = listOf(
            Notification(
                id = "1",
                type = NotificationType.HYDRATION,
                title = "Hydration Reminder",
                message = "Time to drink some water! You're 250ml behind your goal.",
                timestamp = System.currentTimeMillis() - (5 * 60 * 1000), // 5 minutes ago
                isRead = false
            ),
            Notification(
                id = "2",
                type = NotificationType.HABIT,
                title = "Habit Reminder",
                message = "Don't forget your evening walk! Keep your streak going.",
                timestamp = System.currentTimeMillis() - (2 * 60 * 60 * 1000), // 2 hours ago
                isRead = false
            ),
            Notification(
                id = "3",
                type = NotificationType.MOOD,
                title = "Mood Check-in",
                message = "How are you feeling today? Take a moment to log your mood.",
                timestamp = System.currentTimeMillis() - (4 * 60 * 60 * 1000), // 4 hours ago
                isRead = false
            ),
            Notification(
                id = "4",
                type = NotificationType.PROGRESS,
                title = "Great Progress!",
                message = "You've completed 6 out of 8 habits today. Keep it up!",
                timestamp = System.currentTimeMillis() - (24 * 60 * 60 * 1000), // Yesterday
                isRead = true
            ),
            Notification(
                id = "5",
                type = NotificationType.ACHIEVEMENT,
                title = "Streak Achievement",
                message = "Congratulations! You've reached a 7-day streak for meditation.",
                timestamp = System.currentTimeMillis() - (2 * 24 * 60 * 60 * 1000), // 2 days ago
                isRead = true
            )
        )
        
        val notificationsJson = gson.toJson(sampleNotifications)
        prefs.edit().putString("notifications", notificationsJson).apply()
    }
    
    // Reset daily data (for testing or new day)
    fun resetDailyData() {
        val editor = prefs.edit()
        val keys = prefs.all.keys
        keys.forEach { key ->
            if (key.startsWith("water_intake_")) {
                editor.remove(key)
            }
        }
        editor.apply()
    }
}
