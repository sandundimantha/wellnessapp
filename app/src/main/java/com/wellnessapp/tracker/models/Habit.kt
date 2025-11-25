package com.wellnessapp.tracker.models

import java.text.SimpleDateFormat
import java.util.*

data class Habit(
    var name: String,
    var description: String = "",
    var streak: Int = 0,
    var completionDates: MutableSet<String> = mutableSetOf(),
    var createdAt: Long = System.currentTimeMillis()
) {
    
    fun isCompletedToday(): Boolean {
        val today = getTodayString()
        return completionDates.contains(today)
    }
    
    fun markCompletedToday() {
        val today = getTodayString()
        completionDates.add(today)
        updateStreak()
    }
    
    fun markIncompleteToday() {
        val today = getTodayString()
        completionDates.remove(today)
        updateStreak()
    }
    
    private fun updateStreak() {
        val calendar = Calendar.getInstance()
        var currentStreak = 0
        
        // Check consecutive days from today backwards
        for (i in 0..30) { // Check up to 30 days back
            val dateString = getDateString(calendar)
            if (completionDates.contains(dateString)) {
                currentStreak++
                calendar.add(Calendar.DAY_OF_MONTH, -1)
            } else {
                // If today is not completed, don't count it in streak
                if (i == 0 && !completionDates.contains(dateString)) {
                    calendar.add(Calendar.DAY_OF_MONTH, -1)
                    continue
                } else {
                    break
                }
            }
        }
        
        streak = currentStreak
    }
    
    private fun getTodayString(): String {
        return getDateString(Calendar.getInstance())
    }
    
    private fun getDateString(calendar: Calendar): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return dateFormat.format(calendar.time)
    }
    
    fun getCompletionPercentage(): Int {
        val calendar = Calendar.getInstance()
        var completedDays = 0
        val totalDays = 7 // Last 7 days
        
        for (i in 0 until totalDays) {
            val dateString = getDateString(calendar)
            if (completionDates.contains(dateString)) {
                completedDays++
            }
            calendar.add(Calendar.DAY_OF_MONTH, -1)
        }
        
        return (completedDays * 100) / totalDays
    }
}
