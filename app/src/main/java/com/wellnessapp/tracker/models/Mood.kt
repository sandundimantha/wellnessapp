package com.wellnessapp.tracker.models

import java.text.SimpleDateFormat
import java.util.*

data class Mood(
    val emoji: String,
    val note: String = "",
    val timestamp: Long = System.currentTimeMillis()
) {
    
    fun getFormattedDate(): String {
        val calendar = Calendar.getInstance()
        val today = Calendar.getInstance()
        val yesterday = Calendar.getInstance().apply { add(Calendar.DAY_OF_MONTH, -1) }
        
        calendar.timeInMillis = timestamp
        
        return when {
            isSameDay(calendar, today) -> "Today"
            isSameDay(calendar, yesterday) -> "Yesterday"
            else -> {
                val dateFormat = SimpleDateFormat("MMM dd", Locale.getDefault())
                dateFormat.format(calendar.time)
            }
        }
    }
    
    private fun isSameDay(cal1: Calendar, cal2: Calendar): Boolean {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }
    
    fun getMoodValue(): Int {
        return when (emoji) {
            "😢", "😭" -> 1 // Very sad
            "😔", "😞" -> 2 // Sad
            "😐", "😑" -> 3 // Neutral
            "😊", "🙂" -> 4 // Happy
            "😄", "😁", "😍" -> 5 // Very happy
            "😴" -> 3 // Tired (neutral)
            "🤔" -> 3 // Thinking (neutral)
            "😤", "😠" -> 2 // Angry
            "😌" -> 4 // Peaceful
            else -> 3 // Default neutral
        }
    }
}
