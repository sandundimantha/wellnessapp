package com.wellnessapp.tracker

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.view.children
import android.text.format.DateUtils
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.wellnessapp.tracker.models.Habit
import com.wellnessapp.tracker.models.Mood
import com.wellnessapp.tracker.receivers.HydrationReminderReceiver
import com.wellnessapp.tracker.utils.ActivityTransitionUtils
import com.wellnessapp.tracker.utils.HydrationReminderManager
import com.wellnessapp.tracker.services.StepCounterService
import android.content.IntentFilter
import android.content.BroadcastReceiver
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {
    
    private lateinit var bottomNavigation: BottomNavigationView
    private lateinit var habitsContainer: LinearLayout
    private lateinit var recentMoodsContainer: LinearLayout
    private lateinit var waterAmountText: TextView
    private lateinit var waterGoalText: TextView
    private lateinit var waterProgressBar: ProgressBar
    private lateinit var habitsDoneText: TextView
    private lateinit var currentMoodEmoji: TextView
    private lateinit var welcomeText: TextView
    private lateinit var dateText: TextView
    private lateinit var notificationIcon: ImageView
    private lateinit var notificationBadge: TextView
    private lateinit var stepCountText: TextView
    private lateinit var stepGoalText: TextView
    private lateinit var stepProgressBar: ProgressBar
    
    private lateinit var dataManager: DataManager
    private lateinit var stepCountReceiver: BroadcastReceiver
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize data manager first
        dataManager = DataManager(this)
        
        
        setContentView(R.layout.activity_main)
        
        initializeViews()
        setupDataManager()
        setupBottomNavigation()
        setupClickListeners()
        setupNotificationIcon()
        createNotificationChannel()
        setupStepCounter()
        updateUI()
        loadHabits()
        loadRecentMoods()
        setupHydrationReminder()
    }
    
    private fun initializeViews() {
        bottomNavigation = findViewById(R.id.bottom_navigation)
        habitsContainer = findViewById(R.id.habits_container)
        recentMoodsContainer = findViewById(R.id.recent_moods_container)
        waterAmountText = findViewById(R.id.water_amount_text)
        waterGoalText = findViewById(R.id.water_goal_text)
        waterProgressBar = findViewById(R.id.water_progress_bar)
        habitsDoneText = findViewById(R.id.habits_done_text)
        currentMoodEmoji = findViewById(R.id.current_mood_emoji)
        welcomeText = findViewById(R.id.welcome_text)
        dateText = findViewById(R.id.date_text)
        notificationIcon = findViewById(R.id.notification_icon)
        notificationBadge = findViewById(R.id.notification_badge)
        stepCountText = findViewById(R.id.step_count_text)
        stepGoalText = findViewById(R.id.step_goal_text)
        stepProgressBar = findViewById(R.id.step_progress_bar)
    }
    
    private fun setupDataManager() {
        // Generate sample notifications for demo
        if (dataManager.getNotifications().isEmpty()) {
            dataManager.generateSampleNotifications()
        }
    }
    
    private fun setupBottomNavigation() {
        // Set the correct selected item for bottom navigation
        bottomNavigation.selectedItemId = R.id.nav_dashboard
        
        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_dashboard -> {
                    // Already on dashboard
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
                    ActivityTransitionUtils.navigateWithSlideTransition(this, SettingsActivity::class.java)
                    true
                }
                else -> false
            }
        }
    }
    
    private fun setupClickListeners() {
        findViewById<CardView>(R.id.add_habit_card).setOnClickListener {
            showAddHabitDialog()
        }
        
        findViewById<CardView>(R.id.log_mood_card).setOnClickListener {
            showMoodDialog()
        }
        
        findViewById<TextView>(R.id.view_all_habits).setOnClickListener {
            ActivityTransitionUtils.navigateWithSlideTransition(this, HabitActivity::class.java)
        }
        
        findViewById<TextView>(R.id.view_mood_journal).setOnClickListener {
            ActivityTransitionUtils.navigateWithSlideTransition(this, MoodActivity::class.java)
        }
        
        findViewById<Button>(R.id.add_water_button).setOnClickListener {
            addWater(250)
        }
        
        findViewById<Button>(R.id.custom_water_button).setOnClickListener {
            showCustomWaterDialog()
        }
        
        findViewById<Button>(R.id.clear_water_button).setOnClickListener {
            showClearWaterDialog()
        }
        
        // Add test notification button (for debugging)
        findViewById<Button>(R.id.add_water_button).setOnLongClickListener {
            // Long press to test notification
            HydrationReminderManager.scheduleTestReminder(this)
            Toast.makeText(this, "Test notification scheduled in 5 seconds", Toast.LENGTH_SHORT).show()
            true
        }
        
        // Add long press listener to water section for clear option
        findViewById<androidx.cardview.widget.CardView>(R.id.water_card).setOnLongClickListener {
            showWaterOptionsDialog()
            true
        }
    }
    
    private fun setupNotificationIcon() {
        notificationIcon.setOnClickListener {
            showNotificationsDialog()
        }
        updateNotificationBadge()
    }
    
    private fun updateNotificationBadge() {
        val unreadCount = dataManager.getUnreadNotificationCount()
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
            com.wellnessapp.tracker.models.NotificationType.HYDRATION -> R.drawable.ic_notification_water_drop
            com.wellnessapp.tracker.models.NotificationType.HABIT -> R.drawable.ic_notification_check_circle
            com.wellnessapp.tracker.models.NotificationType.MOOD -> R.drawable.ic_notification_smile
            com.wellnessapp.tracker.models.NotificationType.PROGRESS -> R.drawable.ic_notification_heart
            com.wellnessapp.tracker.models.NotificationType.ACHIEVEMENT -> R.drawable.ic_notification_trophy
        }
    }
    
    private fun getNotificationIconBackground(type: com.wellnessapp.tracker.models.NotificationType): Int {
        return when (type) {
            com.wellnessapp.tracker.models.NotificationType.HYDRATION -> R.drawable.bg_notification_icon_blue_gradient
            com.wellnessapp.tracker.models.NotificationType.HABIT -> R.drawable.bg_notification_icon_green_gradient
            com.wellnessapp.tracker.models.NotificationType.MOOD -> R.drawable.bg_notification_icon_yellow_gradient
            com.wellnessapp.tracker.models.NotificationType.PROGRESS -> R.drawable.bg_notification_icon_purple_gradient
            com.wellnessapp.tracker.models.NotificationType.ACHIEVEMENT -> R.drawable.bg_notification_icon_orange_gradient
        }
    }
    
    private fun getRelativeTimeSpanString(timestamp: Long): CharSequence {
        return DateUtils.getRelativeTimeSpanString(
            timestamp,
            System.currentTimeMillis(),
            DateUtils.MINUTE_IN_MILLIS,
            DateUtils.FORMAT_ABBREV_RELATIVE
        )
    }
    
    private fun handleNotificationClick(notification: com.wellnessapp.tracker.models.Notification) {
        when (notification.type) {
            com.wellnessapp.tracker.models.NotificationType.HYDRATION -> {
                // Already on dashboard, just scroll to hydration section
                Toast.makeText(this, "Scroll to hydration tracker", Toast.LENGTH_SHORT).show()
            }
            com.wellnessapp.tracker.models.NotificationType.HABIT -> {
                ActivityTransitionUtils.navigateWithSlideTransition(this, HabitActivity::class.java)
            }
            com.wellnessapp.tracker.models.NotificationType.MOOD -> {
                ActivityTransitionUtils.navigateWithSlideTransition(this, MoodActivity::class.java)
            }
            com.wellnessapp.tracker.models.NotificationType.PROGRESS, 
            com.wellnessapp.tracker.models.NotificationType.ACHIEVEMENT -> {
                // Already on dashboard, show progress
                Toast.makeText(this, "Great progress! Keep it up!", Toast.LENGTH_SHORT).show()
            }
        }
        
    }
    
    private fun updateUI() {
        // Update welcome message based on time of day
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val userName = dataManager.getUserName()
        val greeting = when (hour) {
            in 5..11 -> getString(R.string.good_morning, userName)
            in 12..17 -> getString(R.string.good_afternoon, userName)
            else -> getString(R.string.good_evening, userName)
        }
        welcomeText.text = greeting
        
        // Update date
        val dateFormat = SimpleDateFormat("EEEE, MMMM dd, yyyy", Locale.getDefault())
        dateText.text = dateFormat.format(Date())
        
        // Update water intake
        updateWaterDisplay()
        
        // Update habits progress
        updateHabitsProgress()
        
        // Update current mood
        updateCurrentMood()
    }
    
    private fun updateWaterDisplay() {
        val currentWater = dataManager.getWaterIntake()
        val goal = dataManager.getWaterGoal()
        val progress = ((currentWater.toFloat() / goal) * 100).toInt()
        
        waterAmountText.text = "${currentWater}ml"
        waterGoalText.text = getString(R.string.hydration_goal, "${goal}ml")
        waterProgressBar.progress = progress
    }
    
    private fun updateHabitsProgress() {
        val habits = dataManager.getHabits()
        val completedToday = habits.count { it.isCompletedToday() }
        habitsDoneText.text = "$completedToday/${habits.size}"
    }
    
    private fun updateCurrentMood() {
        val todayMood = dataManager.getTodayMood()
        currentMoodEmoji.text = todayMood?.emoji ?: "😐"
    }
    
    private fun loadHabits() {
        habitsContainer.removeAllViews()
        val habits = dataManager.getHabits()
            .sortedByDescending { it.createdAt } // Sort by creation time, newest first
            .take(3) // Show only first 3 habits
        
        habits.forEach { habit ->
            val habitView = createHabitView(habit)
            habitsContainer.addView(habitView)
        }
    }
    
    private fun createHabitView(habit: Habit): View {
        val habitView = layoutInflater.inflate(R.layout.item_habit, habitsContainer, false)
        
        val checkBox = habitView.findViewById<CheckBox>(R.id.habit_checkbox)
        val nameText = habitView.findViewById<TextView>(R.id.habit_name)
        val streakText = habitView.findViewById<TextView>(R.id.habit_streak)
        val moreButton = habitView.findViewById<ImageView>(R.id.habit_more)
        
        checkBox.isChecked = habit.isCompletedToday()
        nameText.text = habit.name
        streakText.text = if (habit.streak > 0) {
            "🔥 ${habit.streak} day streak"
        } else {
            getString(R.string.start_streak)
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
                
                // Update the UI
                updateHabitsProgress()
                
                // Update the streak text in the current view
                streakText.text = if (updatedHabit.streak > 0) {
                    "🔥 ${updatedHabit.streak} day streak"
                } else {
                    getString(R.string.start_streak)
                }
            }
        }
        
        moreButton.setOnClickListener {
            showHabitOptionsDialog(habit)
        }
        
        return habitView
    }
    
    private fun loadRecentMoods() {
        recentMoodsContainer.removeAllViews()
        val moods = dataManager.getRecentMoods(5)
        
        moods.forEach { mood ->
            val moodView = createMoodView(mood)
            recentMoodsContainer.addView(moodView)
        }
    }
    
    private fun createMoodView(mood: Mood): View {
        val moodView = layoutInflater.inflate(R.layout.item_recent_mood, recentMoodsContainer, false)
        
        val emojiText = moodView.findViewById<TextView>(R.id.mood_emoji)
        val dateText = moodView.findViewById<TextView>(R.id.mood_date)
        
        emojiText.text = mood.emoji
        dateText.text = mood.getFormattedDate()
        
        return moodView
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
                    updateHabitsProgress()
                }
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }
    
    private fun showMoodDialog() {
        val emojis = listOf("😊", "😄", "😐", "😔", "😢", "😴", "😍", "🤔", "😤", "😌")
        
        val dialogView = layoutInflater.inflate(R.layout.dialog_mood, null)
        val emojiGrid = dialogView.findViewById<GridLayout>(R.id.emoji_grid)
        val noteInput = dialogView.findViewById<EditText>(R.id.mood_note_input)
        val selectedEmoji = mutableListOf<String>()
        
        // Create emoji buttons
        emojis.forEach { emoji ->
            val button = Button(this).apply {
                text = emoji
                textSize = 24f
                layoutParams = GridLayout.LayoutParams().apply {
                    width = 0
                    height = GridLayout.LayoutParams.WRAP_CONTENT
                    columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
                    setMargins(8, 8, 8, 8)
                }
                setOnClickListener {
                    selectedEmoji.clear()
                    selectedEmoji.add(emoji)
                    // Update button appearance
                    emojiGrid.children.forEach { child ->
                        child.alpha = if (child == this) 1.0f else 0.5f
                    }
                }
            }
            emojiGrid.addView(button)
        }
        
        MaterialAlertDialogBuilder(this)
            .setTitle(getString(R.string.how_are_you_feeling))
            .setView(dialogView)
            .setPositiveButton(getString(R.string.save_mood)) { _, _ ->
                if (selectedEmoji.isNotEmpty()) {
                    val mood = Mood(selectedEmoji.first(), noteInput.text.toString().trim())
                    dataManager.addMood(mood)
                    loadRecentMoods()
                    updateCurrentMood()
                    Toast.makeText(this, "Mood logged!", Toast.LENGTH_SHORT).show()
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
                    updateHabitsProgress()
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
                updateHabitsProgress()
            }
            .setNegativeButton(getString(R.string.no), null)
            .show()
    }
    
    private fun addWater(amount: Int) {
        val currentWater = dataManager.getWaterIntake()
        dataManager.setWaterIntake(currentWater + amount)
        updateWaterDisplay()
        
        // Show notification if goal reached
        val goal = dataManager.getWaterGoal()
        if (currentWater + amount >= goal) {
            showWaterGoalNotification()
        }
    }
    
    private fun showWaterOptionsDialog() {
        val options = arrayOf("Clear Today's Intake", "Reset Goal")
        
        MaterialAlertDialogBuilder(this)
            .setTitle("Water Options")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> showClearWaterDialog()
                    1 -> showResetGoalDialog()
                }
            }
            .show()
    }
    
    private fun showClearWaterDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Clear Water Intake")
            .setMessage("Are you sure you want to clear today's water intake?")
            .setPositiveButton("Yes") { _, _ ->
                dataManager.clearWaterIntake()
                updateWaterDisplay()
                Toast.makeText(this, "Water intake cleared!", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun showResetGoalDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_custom_water, null)
        val amountInput = dialogView.findViewById<EditText>(R.id.water_amount_input)
        amountInput.hint = "Enter new goal (ml)"
        
        MaterialAlertDialogBuilder(this)
            .setTitle("Reset Water Goal")
            .setView(dialogView)
            .setPositiveButton("Set Goal") { _, _ ->
                val goal = amountInput.text.toString().toIntOrNull() ?: 2500
                if (goal > 0) {
                    dataManager.setWaterGoal(goal)
                    updateWaterDisplay()
                    Toast.makeText(this, "Water goal updated!", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun showCustomWaterDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_custom_water, null)
        val amountInput = dialogView.findViewById<EditText>(R.id.water_amount_input)
        
        MaterialAlertDialogBuilder(this)
            .setTitle("Add Water")
            .setView(dialogView)
            .setPositiveButton(getString(R.string.add_water)) { _, _ ->
                val amount = amountInput.text.toString().toIntOrNull() ?: 0
                if (amount > 0) {
                    addWater(amount)
                }
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }
    
    private fun setupStepCounter() {
        // Start step counter service
        val serviceIntent = Intent(this, StepCounterService::class.java)
        startService(serviceIntent)
        
        // Setup broadcast receiver for step count updates
        stepCountReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (intent?.action == "STEP_COUNT_UPDATED") {
                    val stepCount = intent.getIntExtra("step_count", 0)
                    updateStepDisplay(stepCount)
                }
            }
        }
        
        val filter = IntentFilter("STEP_COUNT_UPDATED")
        registerReceiver(stepCountReceiver, filter)
        
        // Update initial step count
        updateStepDisplay(dataManager.getTodayStepCount())
    }
    
    private fun updateStepDisplay(stepCount: Int) {
        val goal = dataManager.getStepGoal()
        val progress = ((stepCount.toFloat() / goal) * 100).toInt()
        
        stepCountText.text = stepCount.toString()
        stepGoalText.text = "Goal: $goal steps"
        stepProgressBar.progress = progress
    }
    
    private fun setupHydrationReminder() {
        if (dataManager.isHydrationReminderEnabled()) {
            val interval = dataManager.getHydrationReminderInterval()
            HydrationReminderManager.scheduleHydrationReminder(this, interval)
        }
    }
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "hydration_reminder",
                "Hydration Reminder",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Reminds you to drink water"
            }
            
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    private fun showWaterGoalNotification() {
        val builder = NotificationCompat.Builder(this, "hydration_reminder")
            .setSmallIcon(R.drawable.ic_drop)
            .setContentTitle("Water Goal Reached! 🎉")
            .setContentText("Great job! You've reached your daily water goal.")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        
        with(NotificationManagerCompat.from(this)) {
            notify(1, builder.build())
        }
    }
    
    override fun onResume() {
        super.onResume()
        updateUI()
        loadHabits()
        loadRecentMoods()
        updateNotificationBadge()
        updateStepDisplay(dataManager.getTodayStepCount())
    }
    
    override fun onDestroy() {
        super.onDestroy()
        try {
            unregisterReceiver(stepCountReceiver)
        } catch (e: Exception) {
            // Receiver was not registered
        }
    }
}
