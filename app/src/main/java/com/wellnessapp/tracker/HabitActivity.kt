package com.wellnessapp.tracker

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.wellnessapp.tracker.models.Habit
import com.wellnessapp.tracker.utils.ActivityTransitionUtils
import com.wellnessapp.tracker.models.NotificationType
import android.text.format.DateUtils
import java.util.Calendar

class HabitActivity : AppCompatActivity() {
    
    private lateinit var habitsContainer: LinearLayout
    private lateinit var addHabitButton: Button
    private lateinit var dayStreakText: TextView
    private lateinit var weekProgressText: TextView
    private lateinit var bottomNavigation: BottomNavigationView
    private lateinit var dataManager: DataManager
    private lateinit var notificationIcon: ImageView
    private lateinit var notificationBadge: TextView
    private var unreadCount: Int = 0
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize data manager first
        dataManager = DataManager(this)
        
        
        setContentView(R.layout.activity_habit)
        
        initializeViews()
        setupDataManager()
        setupClickListeners()
        setupBottomNavigation()
        setupNotificationIcon()
        loadHabits()
    }
    
    private fun initializeViews() {
        habitsContainer = findViewById(R.id.habits_container)
        addHabitButton = findViewById(R.id.addHabitButton)
        dayStreakText = findViewById(R.id.day_streak_text)
        weekProgressText = findViewById(R.id.week_progress_text)
        bottomNavigation = findViewById(R.id.bottom_navigation)
        notificationIcon = findViewById(R.id.notification_icon)
        notificationBadge = findViewById(R.id.notification_badge)
    }
    
    private fun setupDataManager() {
        // DataManager already initialized in onCreate
    }
    
    private fun setupBottomNavigation() {
        // Set the correct selected item for bottom navigation
        bottomNavigation.selectedItemId = R.id.nav_habits
        
        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_dashboard -> {
                    ActivityTransitionUtils.navigateWithSlideTransition(this, MainActivity::class.java)
                    true
                }
                R.id.nav_habits -> {
                    true
                }
                R.id.nav_mood -> {
                    ActivityTransitionUtils.navigateWithSlideTransition(this, MoodActivity::class.java)
                    true
                }
                R.id.nav_settings -> {
                    ActivityTransitionUtils.navigateWithSlideTransition(this, SettingsActivity::class.java)
                    true
                }
                else -> false
            }
        }
    }
    
    private fun setupClickListeners() {
        addHabitButton.setOnClickListener {
            showAddHabitDialog()
        }
    }
    
    private fun loadHabits() {
        habitsContainer.removeAllViews()
        val habits = dataManager.getHabits()
            .sortedByDescending { it.createdAt } // Sort by creation time, newest first
        
        // Update stats
        updateStats(habits)
        
        if (habits.isEmpty()) {
            showEmptyState()
        } else {
            habits.forEach { habit ->
                val habitView = createHabitView(habit)
                habitsContainer.addView(habitView)
            }
        }
    }
    
    private fun updateStats(habits: List<Habit>) {
        // Calculate day streak (longest current streak)
        val maxStreak = habits.maxOfOrNull { it.streak } ?: 0
        dayStreakText.text = maxStreak.toString()
        
        // Calculate weekly completion percentage
        val totalHabits = habits.size
        if (totalHabits == 0) {
            weekProgressText.text = "0%"
            return
        }
        
        val calendar = Calendar.getInstance()
        var totalCompletions = 0
        val totalDays = 7 // Last 7 days
        
        // Calculate completion rate for each habit over the last 7 days
        habits.forEach { habit ->
            var habitCompletions = 0
            val habitCalendar = Calendar.getInstance()
            
            for (i in 0 until totalDays) {
                val dateString = getDateString(habitCalendar)
                if (habit.completionDates.contains(dateString)) {
                    habitCompletions++
                }
                habitCalendar.add(Calendar.DAY_OF_MONTH, -1)
            }
            
            totalCompletions += habitCompletions
        }
        
        val maxPossibleCompletions = totalHabits * totalDays
        val weeklyPercentage = if (maxPossibleCompletions > 0) {
            (totalCompletions * 100) / maxPossibleCompletions
        } else {
            0
        }
        
        weekProgressText.text = "$weeklyPercentage%"
    }
    
    private fun getDateString(calendar: Calendar): String {
        val dateFormat = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
        return dateFormat.format(calendar.time)
    }
    
    private fun showEmptyState() {
        val emptyView = layoutInflater.inflate(R.layout.empty_habits_state, habitsContainer, false)
        val addHabitButton = emptyView.findViewById<Button>(R.id.btn_add_first_habit)
        
        addHabitButton.setOnClickListener {
            showAddHabitDialog()
        }
        
        habitsContainer.addView(emptyView)
    }
    
    private fun createHabitView(habit: Habit): View {
        val habitView = layoutInflater.inflate(R.layout.item_habit, habitsContainer, false)
        
        val checkBox = habitView.findViewById<CheckBox>(R.id.habit_checkbox)
        val nameText = habitView.findViewById<TextView>(R.id.habit_name)
        val streakText = habitView.findViewById<TextView>(R.id.habit_streak)
        val moreButton = habitView.findViewById<ImageView>(R.id.habit_more)
        val progressBar = habitView.findViewById<ProgressBar>(R.id.habit_progress_bar)
        
        checkBox.isChecked = habit.isCompletedToday()
        nameText.text = habit.name
        
        // Determine category and format streak text
        val category = getHabitCategory(habit.name)
        streakText.text = if (habit.streak > 0) {
            "$category • 🔥 ${habit.streak} day streak"
        } else {
            "$category • ${getString(R.string.start_streak)}"
        }
        
        // Update progress bar
        val progress = if (habit.isCompletedToday()) 100 else 0
        progressBar.progress = progress
        progressBar.progressTintList = if (habit.isCompletedToday()) {
            android.content.res.ColorStateList.valueOf(getColor(R.color.primary_color))
        } else {
            android.content.res.ColorStateList.valueOf(getColor(R.color.gray_200))
        }
        
        checkBox.setOnCheckedChangeListener { _, isChecked ->
            // Get fresh habits list
            val habits = dataManager.getHabits().toMutableList()
            val habitIndex = habits.indexOfFirst { it.name == habit.name }
            
            if (habitIndex != -1) {
                val updatedHabit = habits[habitIndex]
                if (isChecked) {
                    updatedHabit.markCompletedToday()
                } else {
                    updatedHabit.markIncompleteToday()
                }
                
                // Save the updated habits list
                dataManager.saveHabits(habits)
                
                // Update progress bar
                val progressValue = if (isChecked) 100 else 0
                progressBar.progress = progressValue
                progressBar.progressTintList = if (isChecked) {
                    android.content.res.ColorStateList.valueOf(getColor(R.color.primary_color))
                } else {
                    android.content.res.ColorStateList.valueOf(getColor(R.color.gray_200))
                }
                
                // Update streak text
                streakText.text = if (updatedHabit.streak > 0) {
                    "$category • 🔥 ${updatedHabit.streak} day streak"
                } else {
                    "$category • ${getString(R.string.start_streak)}"
                }
                
                // Update stats
                updateStats(dataManager.getHabits())
            }
        }
        
        moreButton.setOnClickListener {
            showHabitOptionsDialog(habit)
        }
        
        return habitView
    }
    
    private fun showAddHabitDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_habit, null)
        val nameInput = dialogView.findViewById<EditText>(R.id.habit_name_input)
        val descriptionInput = dialogView.findViewById<EditText>(R.id.habit_description_input)
        
        MaterialAlertDialogBuilder(this)
            .setTitle(getString(R.string.add_new_habit))
            .setView(dialogView)
            .setPositiveButton(getString(R.string.save)) { _, _ ->
                val name = nameInput.text.toString().trim()
                if (name.isNotEmpty()) {
                    val habit = Habit(name, descriptionInput.text.toString().trim())
                    dataManager.addHabit(habit)
                    loadHabits()
                }
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }
    
    private fun showHabitOptionsDialog(habit: Habit) {
        val options = arrayOf(getString(R.string.edit), getString(R.string.delete))
        
        MaterialAlertDialogBuilder(this)
            .setTitle(habit.name)
            .setItems(options) { _, which ->
                when (which) {
                    0 -> showEditHabitDialog(habit)
                    1 -> showDeleteHabitDialog(habit)
                }
            }
            .show()
    }
    
    private fun showEditHabitDialog(habit: Habit) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_habit, null)
        val nameInput = dialogView.findViewById<EditText>(R.id.habit_name_input)
        val descriptionInput = dialogView.findViewById<EditText>(R.id.habit_description_input)
        
        nameInput.setText(habit.name)
        descriptionInput.setText(habit.description)
        
        MaterialAlertDialogBuilder(this)
            .setTitle(getString(R.string.edit_habit))
            .setView(dialogView)
            .setPositiveButton(getString(R.string.save)) { _, _ ->
                val name = nameInput.text.toString().trim()
                if (name.isNotEmpty()) {
                    val oldName = habit.name
                    habit.name = name
                    habit.description = descriptionInput.text.toString().trim()
                    
                    // Save the updated habit properly
                    val habits = dataManager.getHabits().toMutableList()
                    val habitIndex = habits.indexOfFirst { it.name == oldName }
                    if (habitIndex != -1) {
                        habits[habitIndex] = habit
                        dataManager.saveHabits(habits)
                    }
                    
                    loadHabits()
                }
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }
    
    private fun showDeleteHabitDialog(habit: Habit) {
        MaterialAlertDialogBuilder(this)
            .setTitle(getString(R.string.delete_habit))
            .setMessage("Are you sure you want to delete \"${habit.name}\"?")
            .setPositiveButton(getString(R.string.yes)) { _, _ ->
                dataManager.deleteHabit(habit)
                loadHabits()
            }
            .setNegativeButton(getString(R.string.no), null)
            .show()
    }
    
    private fun getHabitCategory(habitName: String): String {
        val name = habitName.lowercase()
        return when {
            name.contains("meditation") || name.contains("mindfulness") || name.contains("gratitude") -> "Mindfulness"
            name.contains("water") || name.contains("drink") || name.contains("hydration") -> "Health"
            name.contains("walk") || name.contains("run") || name.contains("exercise") || name.contains("fitness") -> "Fitness"
            name.contains("read") || name.contains("study") || name.contains("learn") -> "Learning"
            name.contains("sleep") || name.contains("rest") -> "Health"
            else -> "Health"
        }
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
            startActivity(Intent(this, SettingsActivity::class.java))
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
                // Already in habits activity
                Toast.makeText(this, "You're already viewing habits", Toast.LENGTH_SHORT).show()
            }
            com.wellnessapp.tracker.models.NotificationType.HYDRATION -> {
                startActivity(Intent(this, MainActivity::class.java))
            }
            com.wellnessapp.tracker.models.NotificationType.MOOD -> {
                startActivity(Intent(this, MoodActivity::class.java))
            }
            com.wellnessapp.tracker.models.NotificationType.PROGRESS, 
            com.wellnessapp.tracker.models.NotificationType.ACHIEVEMENT -> {
                // Already in habits activity, show progress
                Toast.makeText(this, "Great progress! Keep it up!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        loadHabits()
        updateNotificationBadge()
    }
}
