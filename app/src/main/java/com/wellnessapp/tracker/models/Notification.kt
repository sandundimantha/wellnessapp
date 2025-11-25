package com.wellnessapp.tracker.models

data class Notification(
    val id: String,
    val type: NotificationType,
    val title: String,
    val message: String,
    val timestamp: Long,
    val isRead: Boolean = false
) {
    fun getFormattedTime(): String {
        val now = System.currentTimeMillis()
        val diff = now - timestamp
        
        return when {
            diff < 60 * 1000 -> "Just now"
            diff < 60 * 60 * 1000 -> "${diff / (60 * 1000)} minutes ago"
            diff < 24 * 60 * 60 * 1000 -> "${diff / (60 * 60 * 1000)} hours ago"
            diff < 7 * 24 * 60 * 60 * 1000 -> "${diff / (24 * 60 * 60 * 1000)} days ago"
            else -> "A week ago"
        }
    }
    
    fun getIcon(): String {
        return when (type) {
            NotificationType.HYDRATION -> "💧"
            NotificationType.HABIT -> "✅"
            NotificationType.MOOD -> "😊"
            NotificationType.PROGRESS -> "💜"
            NotificationType.ACHIEVEMENT -> "🔥"
        }
    }
    
    fun getIconBackground(): String {
        return when (type) {
            NotificationType.HYDRATION -> "blue"
            NotificationType.HABIT -> "green"
            NotificationType.MOOD -> "yellow"
            NotificationType.PROGRESS -> "purple"
            NotificationType.ACHIEVEMENT -> "orange"
        }
    }
}

enum class NotificationType {
    HYDRATION,
    HABIT,
    MOOD,
    PROGRESS,
    ACHIEVEMENT
}
