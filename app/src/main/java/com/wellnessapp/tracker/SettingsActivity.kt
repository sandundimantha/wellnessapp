package com.wellnessapp.tracker

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import android.view.View
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import android.text.format.DateUtils
import com.wellnessapp.tracker.utils.ActivityTransitionUtils
import android.os.Environment
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.*
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import android.Manifest
import android.net.Uri
import androidx.core.content.FileProvider
import com.google.gson.Gson
import com.google.gson.GsonBuilder

class SettingsActivity : AppCompatActivity() {
    
    private lateinit var userName: TextView
    private lateinit var memberSince: TextView
    private lateinit var habitRemindersSwitch: Switch
    private lateinit var hydrationRemindersSwitch: Switch
    private lateinit var moodCheckinsSwitch: Switch
    private lateinit var waterGoalDisplay: TextView
    private lateinit var reminderIntervalDisplay: TextView
    private lateinit var editWaterGoalButton: Button
    private lateinit var editIntervalButton: Button
    private lateinit var stepGoalDisplay: TextView
    private lateinit var editStepGoalButton: Button
    private lateinit var bottomNavigation: BottomNavigationView
    private lateinit var editProfileIcon: ImageView
    private lateinit var userAvatar: TextView
    private lateinit var dataManager: DataManager
    private lateinit var notificationIcon: ImageView
    private lateinit var notificationBadge: TextView
    private lateinit var exportDataLayout: LinearLayout
    private var unreadCount: Int = 0
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize data manager first
        dataManager = DataManager(this)
        
        
        setContentView(R.layout.activity_settings)
        
        initializeViews()
        setupDataManager()
        setupClickListeners()
        setupNotificationIcon()
        loadSettings()
    }
    
    private fun initializeViews() {
        userName = findViewById(R.id.user_name)
        memberSince = findViewById(R.id.member_since)
        habitRemindersSwitch = findViewById(R.id.habit_reminders_switch)
        hydrationRemindersSwitch = findViewById(R.id.hydration_reminders_switch)
        moodCheckinsSwitch = findViewById(R.id.mood_checkins_switch)
        waterGoalDisplay = findViewById(R.id.water_goal_display)
        reminderIntervalDisplay = findViewById(R.id.reminder_interval_display)
        editWaterGoalButton = findViewById(R.id.edit_water_goal_button)
        editIntervalButton = findViewById(R.id.edit_interval_button)
        stepGoalDisplay = findViewById(R.id.step_goal_display)
        editStepGoalButton = findViewById(R.id.edit_step_goal_button)
        bottomNavigation = findViewById(R.id.bottom_navigation)
        editProfileIcon = findViewById<ImageView>(R.id.edit_profile_icon)
        userAvatar = findViewById<TextView>(R.id.user_avatar)
        notificationIcon = findViewById(R.id.notification_icon)
        notificationBadge = findViewById(R.id.notification_badge)
        exportDataLayout = findViewById(R.id.export_data_layout)
    }
    
    private fun setupDataManager() {
        // DataManager already initialized in onCreate
    }
    
    private fun setupClickListeners() {
        // Set the correct selected item for bottom navigation
        bottomNavigation.selectedItemId = R.id.nav_settings
        
        editProfileIcon.setOnClickListener {
            startActivity(Intent(this, EditProfileActivity::class.java))
        }
        
        editWaterGoalButton.setOnClickListener {
            showWaterGoalDialog()
        }
        
        editIntervalButton.setOnClickListener {
            showIntervalDialog()
        }
        
        editStepGoalButton.setOnClickListener {
            showStepGoalDialog()
        }
        
        exportDataLayout.setOnClickListener {
            exportData()
        }
        
        habitRemindersSwitch.setOnCheckedChangeListener { _, isChecked ->
            dataManager.saveHabitRemindersEnabled(isChecked)
            Toast.makeText(this, "Habit reminders ${if (isChecked) "enabled" else "disabled"}", Toast.LENGTH_SHORT).show()
        }
        
        hydrationRemindersSwitch.setOnCheckedChangeListener { _, isChecked ->
            dataManager.saveReminderEnabled(isChecked)
            dataManager.setHydrationReminderEnabled(isChecked)
            
            if (isChecked) {
                // Enable hydration reminders with current interval
                val interval = dataManager.getHydrationReminderInterval()
                com.wellnessapp.tracker.utils.HydrationReminderManager.scheduleHydrationReminder(this, interval)
            } else {
                // Disable hydration reminders
                com.wellnessapp.tracker.utils.HydrationReminderManager.cancelHydrationReminder(this)
            }
            
            Toast.makeText(this, "Hydration reminders ${if (isChecked) "enabled" else "disabled"}", Toast.LENGTH_SHORT).show()
        }
        
        moodCheckinsSwitch.setOnCheckedChangeListener { _, isChecked ->
            dataManager.saveMoodRemindersEnabled(isChecked)
            Toast.makeText(this, "Mood check-ins ${if (isChecked) "enabled" else "disabled"}", Toast.LENGTH_SHORT).show()
        }
        
        
        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_dashboard -> {
                    ActivityTransitionUtils.navigateWithSlideTransition(this, MainActivity::class.java)
                    true
                }
                R.id.nav_habits -> {
                    ActivityTransitionUtils.navigateWithSlideTransition(this, HabitActivity::class.java)
                    true
                }
                R.id.nav_mood -> {
                    ActivityTransitionUtils.navigateWithSlideTransition(this, MoodActivity::class.java)
                    true
                }
                R.id.nav_settings -> {
                    true
                }
                else -> false
            }
        }
    }
    
    
    private fun loadSettings() {
        // Load user profile
        val userNameValue = dataManager.getUserName().ifEmpty { "Sarah Johnson" }
        userName.text = userNameValue
        userAvatar.text = userNameValue.take(1).uppercase()
        memberSince.text = "Member since March 2024"
        
        // Load notification settings
        habitRemindersSwitch.isChecked = dataManager.isHabitRemindersEnabled()
        hydrationRemindersSwitch.isChecked = dataManager.isHydrationReminderEnabled()
        moodCheckinsSwitch.isChecked = dataManager.isMoodRemindersEnabled()
        
        // Load goals and targets
        val waterGoal = dataManager.getWaterGoal()
        waterGoalDisplay.text = "Current: ${waterGoal / 1000.0}L"
        
        val interval = dataManager.getReminderInterval()
        reminderIntervalDisplay.text = interval
        
        val stepGoal = dataManager.getStepGoal()
        stepGoalDisplay.text = "Current: ${stepGoal} steps"
        
    }
    
    private fun showWaterGoalDialog() {
        val goals = arrayOf("2.0L", "2.5L", "3.0L", "3.5L", "4.0L")
        val currentGoal = dataManager.getWaterGoal()
        val currentGoalLiters = currentGoal / 1000.0
        val currentIndex = goals.indexOf("${currentGoalLiters}L")
        
        MaterialAlertDialogBuilder(this)
            .setTitle("Select Daily Water Goal")
            .setSingleChoiceItems(goals, if (currentIndex >= 0) currentIndex else 1) { dialog, which ->
                val selectedGoal = goals[which].replace("L", "").toDouble()
                dataManager.saveWaterGoal((selectedGoal * 1000).toInt()) // Convert to milliliters
                waterGoalDisplay.text = "Current: ${goals[which]}"
                Toast.makeText(this, "Water goal updated to ${goals[which]}", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun showIntervalDialog() {
        val intervals = arrayOf("Every 30 seconds", "Every 1 minute", "Every 5 minutes", "Every 30 minutes", "Every 1 hour", "Every 2 hours", "Every 3 hours", "Every 4 hours")
        val currentInterval = dataManager.getReminderInterval()
        val currentIndex = intervals.indexOf(currentInterval)
        
        MaterialAlertDialogBuilder(this)
            .setTitle("Select Reminder Interval")
            .setSingleChoiceItems(intervals, currentIndex) { dialog, which ->
                dataManager.saveReminderInterval(intervals[which])
                reminderIntervalDisplay.text = intervals[which]
                
                // Update the actual hydration reminder interval based on selection
                val intervalMinutes = when (which) {
                    0 -> 0.5 // 30 seconds = 0.5 minutes
                    1 -> 1.0 // 1 minute
                    2 -> 5.0 // 5 minutes
                    3 -> 30.0 // 30 minutes
                    4 -> 60.0 // 1 hour
                    5 -> 120.0 // 2 hours
                    6 -> 180.0 // 3 hours
                    7 -> 240.0 // 4 hours
                    else -> 60.0
                }
                
                dataManager.setHydrationReminderInterval(intervalMinutes)
                dataManager.setHydrationReminderEnabled(true)
                
                // Restart the hydration reminder with new interval
                com.wellnessapp.tracker.utils.HydrationReminderManager.scheduleHydrationReminder(this, intervalMinutes)
                
                Toast.makeText(this, "Reminder interval updated to ${intervals[which]}", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun showStepGoalDialog() {
        val goals = arrayOf("5,000", "7,500", "10,000", "12,500", "15,000", "20,000")
        val currentGoal = dataManager.getStepGoal()
        val currentIndex = goals.indexOf(currentGoal.toString())
        
        MaterialAlertDialogBuilder(this)
            .setTitle("Select Daily Step Goal")
            .setSingleChoiceItems(goals, if (currentIndex >= 0) currentIndex else 2) { dialog, which ->
                val selectedGoal = goals[which].replace(",", "").toInt()
                dataManager.setStepGoal(selectedGoal)
                stepGoalDisplay.text = "Current: ${goals[which]} steps"
                Toast.makeText(this, "Step goal updated to ${goals[which]} steps", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun setupNotificationIcon() {
        notificationIcon.setOnClickListener {
            showNotificationsDialog()
        }
        updateNotificationBadge()
    }
    
    private fun updateNotificationBadge() {
        unreadCount = dataManager.getNotifications().count { !it.isRead }
        if (unreadCount > 0) {
            notificationBadge.visibility = View.VISIBLE
            notificationBadge.text = if (unreadCount > 99) "99+" else unreadCount.toString()
        } else {
            notificationBadge.visibility = View.GONE
        }
    }
    
    private fun showNotificationsDialog() {
        val notifications = dataManager.getNotifications()
        
        if (notifications.isEmpty()) {
            Toast.makeText(this, "No notifications", Toast.LENGTH_SHORT).show()
            return
        }
        
        // Create a custom dialog with notification list
        val dialogView = layoutInflater.inflate(R.layout.dialog_notifications, null)
        val notificationsContainer = dialogView.findViewById<LinearLayout>(R.id.notifications_container)
        val markAllReadText = dialogView.findViewById<TextView>(R.id.mark_all_read_text)
        val notificationSettingsText = dialogView.findViewById<TextView>(R.id.notification_settings_text)
        
        // Populate notifications
        notifications.forEach { notification ->
            val notificationView = createNotificationItemView(notification)
            notificationsContainer.addView(notificationView)
        }
        
        val dialog = MaterialAlertDialogBuilder(this)
            .setView(dialogView)
            .setCancelable(true)
            .create()
        
        // Set up click listeners
        markAllReadText.setOnClickListener {
            dataManager.markAllNotificationsAsRead()
            updateNotificationBadge()
            dialog.dismiss()
            Toast.makeText(this, "All notifications marked as read", Toast.LENGTH_SHORT).show()
        }
        
        notificationSettingsText.setOnClickListener {
            dialog.dismiss()
            // Already in settings activity
            Toast.makeText(this, "You're already in notification settings", Toast.LENGTH_SHORT).show()
        }
        
        dialog.show()
    }
    
    private fun createNotificationItemView(notification: com.wellnessapp.tracker.models.Notification): View {
        val notificationView = layoutInflater.inflate(R.layout.item_notification_dialog, null)
        
        val iconBackground = notificationView.findViewById<ImageView>(R.id.notification_icon_background)
        val icon = notificationView.findViewById<ImageView>(R.id.notification_icon)
        val title = notificationView.findViewById<TextView>(R.id.notification_title)
        val message = notificationView.findViewById<TextView>(R.id.notification_message)
        val timestamp = notificationView.findViewById<TextView>(R.id.notification_timestamp)
        val unreadIndicator = notificationView.findViewById<View>(R.id.unread_indicator)
        
        // Set icon and background based on notification type
        icon.setImageResource(getNotificationIcon(notification.type))
        iconBackground.background = ContextCompat.getDrawable(this, getNotificationIconBackground(notification.type))
        
        title.text = notification.title
        message.text = notification.message
        timestamp.text = getRelativeTimeSpanString(notification.timestamp)
        
        // Show unread indicator if not read
        unreadIndicator.visibility = if (!notification.isRead) View.VISIBLE else View.GONE
        
        // Set click listener
        notificationView.setOnClickListener {
            if (!notification.isRead) {
                dataManager.markNotificationAsRead(notification.id)
                updateNotificationBadge()
            }
            handleNotificationClick(notification)
        }
        
        return notificationView
    }
    
    private fun getNotificationIcon(type: com.wellnessapp.tracker.models.NotificationType): Int {
        return when (type) {
            com.wellnessapp.tracker.models.NotificationType.HABIT -> R.drawable.ic_notification_check_circle
            com.wellnessapp.tracker.models.NotificationType.HYDRATION -> R.drawable.ic_notification_water_drop
            com.wellnessapp.tracker.models.NotificationType.MOOD -> R.drawable.ic_notification_smile
            com.wellnessapp.tracker.models.NotificationType.PROGRESS -> R.drawable.ic_notification_heart
            com.wellnessapp.tracker.models.NotificationType.ACHIEVEMENT -> R.drawable.ic_notification_trophy
        }
    }
    
    private fun getNotificationIconBackground(type: com.wellnessapp.tracker.models.NotificationType): Int {
        return when (type) {
            com.wellnessapp.tracker.models.NotificationType.HABIT -> R.drawable.bg_notification_icon_green_gradient
            com.wellnessapp.tracker.models.NotificationType.HYDRATION -> R.drawable.bg_notification_icon_blue_gradient
            com.wellnessapp.tracker.models.NotificationType.MOOD -> R.drawable.bg_notification_icon_yellow_gradient
            com.wellnessapp.tracker.models.NotificationType.PROGRESS -> R.drawable.bg_notification_icon_purple_gradient
            com.wellnessapp.tracker.models.NotificationType.ACHIEVEMENT -> R.drawable.bg_notification_icon_orange_gradient
        }
    }
    
    private fun getRelativeTimeSpanString(timestamp: Long): String {
        return DateUtils.getRelativeTimeSpanString(
            timestamp,
            System.currentTimeMillis(),
            DateUtils.MINUTE_IN_MILLIS,
            DateUtils.FORMAT_ABBREV_RELATIVE
        ).toString()
    }
    
    private fun handleNotificationClick(notification: com.wellnessapp.tracker.models.Notification) {
        when (notification.type) {
            com.wellnessapp.tracker.models.NotificationType.HABIT -> {
                ActivityTransitionUtils.navigateWithSlideTransition(this, HabitActivity::class.java)
            }
            com.wellnessapp.tracker.models.NotificationType.HYDRATION -> {
                ActivityTransitionUtils.navigateWithSlideTransition(this, MainActivity::class.java)
            }
            com.wellnessapp.tracker.models.NotificationType.MOOD -> {
                ActivityTransitionUtils.navigateWithSlideTransition(this, MoodActivity::class.java)
            }
            com.wellnessapp.tracker.models.NotificationType.PROGRESS, 
            com.wellnessapp.tracker.models.NotificationType.ACHIEVEMENT -> {
                // Already in settings activity, show progress
                Toast.makeText(this, "Great progress! Keep it up!", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun exportData() {
        // Check for storage permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) 
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, 
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 1001)
            return
        }
        
        try {
            // Create export data structure
            val exportData = createExportData()
            
            // Convert to JSON
            val gson = GsonBuilder().setPrettyPrinting().create()
            val jsonData = gson.toJson(exportData)
            
            // Create file
            val fileName = "wellness_data_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())}.json"
            val file = File(getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), fileName)
            
            // Write data to file
            val writer = FileWriter(file)
            writer.write(jsonData)
            writer.close()
            
            // Share the file
            shareFile(file)
            
            Toast.makeText(this, "Data exported successfully!", Toast.LENGTH_SHORT).show()
            
        } catch (e: Exception) {
            Toast.makeText(this, "Error exporting data: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
    
    private fun createExportData(): Map<String, Any> {
        val habits = dataManager.getHabits()
        val moods = dataManager.getMoods()
        val waterIntake = dataManager.getWaterIntake()
        val waterGoal = dataManager.getWaterGoal()
        val stepCount = dataManager.getTodayStepCount()
        val stepGoal = dataManager.getStepGoal()
        val userName = dataManager.getUserName()
        
        return mapOf(
            "export_info" to mapOf(
                "export_date" to SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date()),
                "app_version" to "1.2.0",
                "user_name" to userName
            ),
            "habits" to habits.map { habit ->
                mapOf(
                    "name" to habit.name,
                    "description" to habit.description,
                    "streak" to habit.streak,
                    "completion_dates" to habit.completionDates.toList(),
                    "created_at" to habit.createdAt
                )
            },
            "moods" to moods.map { mood ->
                mapOf(
                    "emoji" to mood.emoji,
                    "note" to mood.note,
                    "timestamp" to mood.timestamp,
                    "formatted_date" to mood.getFormattedDate(),
                    "mood_value" to mood.getMoodValue()
                )
            },
            "water_data" to mapOf(
                "current_intake" to waterIntake,
                "goal" to waterGoal
            ),
            "step_data" to mapOf(
                "current_count" to stepCount,
                "goal" to stepGoal
            ),
            "settings" to mapOf(
                "habit_reminders_enabled" to dataManager.isHabitRemindersEnabled(),
                "hydration_reminders_enabled" to dataManager.isReminderEnabled(),
                "mood_reminders_enabled" to dataManager.isMoodRemindersEnabled(),
                "reminder_interval" to dataManager.getReminderInterval()
            )
        )
    }
    
    private fun shareFile(file: File) {
        val uri = FileProvider.getUriForFile(
            this,
            "${packageName}.fileprovider",
            file
        )
        
        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            type = "application/json"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "Wellness Data Export")
            putExtra(Intent.EXTRA_TEXT, "My wellness data export from WellnessTracker app")
        }
        
        startActivity(Intent.createChooser(shareIntent, "Share wellness data"))
    }

    override fun onResume() {
        super.onResume()
        loadSettings()
        updateNotificationBadge()
    }
}