package com.wellnessapp.tracker

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.children
import androidx.core.content.ContextCompat
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.wellnessapp.tracker.models.Mood
import com.wellnessapp.tracker.utils.ActivityTransitionUtils
import android.text.format.DateUtils
import java.text.SimpleDateFormat
import java.util.*
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import android.graphics.Color

class MoodActivity : AppCompatActivity() {
    
    private lateinit var moodsContainer: LinearLayout
    private lateinit var moodDevastated: Button
    private lateinit var moodSad: Button
    private lateinit var moodDown: Button
    private lateinit var moodNeutral: Button
    private lateinit var moodContent: Button
    private lateinit var moodHappy: Button
    private lateinit var moodExcited: Button
    private lateinit var moodEcstatic: Button
    private lateinit var moodGrateful: Button
    private lateinit var moodPeaceful: Button
    private lateinit var detailedMoodBtn: Button
    private lateinit var viewMoodJournalBtn: Button
    private lateinit var bottomNavigation: BottomNavigationView
    private lateinit var dataManager: DataManager
    private lateinit var notificationIcon: ImageView
    private lateinit var notificationBadge: TextView
    private lateinit var moodChart: LineChart
    private lateinit var chartDescription: TextView
    private var selectedMood: String = ""
    private var unreadCount: Int = 0
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize data manager first
        dataManager = DataManager(this)
        
        
        setContentView(R.layout.activity_mood)
        
        initializeViews()
        setupDataManager()
        setupClickListeners()
        setupBottomNavigation()
        setupNotificationIcon()
        setupChart()
        loadMoods()
    }
    
    private fun initializeViews() {
        moodsContainer = findViewById(R.id.moods_container)
        moodDevastated = findViewById(R.id.moodDevastated)
        moodSad = findViewById(R.id.moodSad)
        moodDown = findViewById(R.id.moodDown)
        moodNeutral = findViewById(R.id.moodNeutral)
        moodContent = findViewById(R.id.moodContent)
        moodHappy = findViewById(R.id.moodHappy)
        moodExcited = findViewById(R.id.moodExcited)
        moodEcstatic = findViewById(R.id.moodEcstatic)
        moodGrateful = findViewById(R.id.moodGrateful)
        moodPeaceful = findViewById(R.id.moodPeaceful)
        detailedMoodBtn = findViewById(R.id.detailedMoodBtn)
        viewMoodJournalBtn = findViewById(R.id.viewMoodJournalBtn)
        bottomNavigation = findViewById(R.id.bottom_navigation)
        notificationIcon = findViewById(R.id.notification_icon)
        notificationBadge = findViewById(R.id.notification_badge)
        moodChart = findViewById(R.id.moodChart)
        chartDescription = findViewById(R.id.chartDescription)
    }
    
    private fun setupDataManager() {
        // DataManager already initialized in onCreate
    }
    
    private fun setupBottomNavigation() {
        // Set the correct selected item for bottom navigation
        bottomNavigation.selectedItemId = R.id.nav_mood
        
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
        // Mood button click listeners
        moodDevastated.setOnClickListener { selectMood("😭") }
        moodSad.setOnClickListener { selectMood("😢") }
        moodDown.setOnClickListener { selectMood("😔") }
        moodNeutral.setOnClickListener { selectMood("😐") }
        moodContent.setOnClickListener { selectMood("🙂") }
        moodHappy.setOnClickListener { selectMood("😊") }
        moodExcited.setOnClickListener { selectMood("😄") }
        moodEcstatic.setOnClickListener { selectMood("🤩") }
        moodGrateful.setOnClickListener { selectMood("🥰") }
        moodPeaceful.setOnClickListener { selectMood("😌") }
        
        detailedMoodBtn.setOnClickListener {
            showDetailedMoodDialog()
        }
        
        viewMoodJournalBtn.setOnClickListener {
            // Already in mood journal, could scroll to top or show all entries
            Toast.makeText(this, "Viewing all entries", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun selectMood(emoji: String) {
        selectedMood = emoji
        
        // Reset all button backgrounds
        val buttons = listOf(moodDevastated, moodSad, moodDown, moodNeutral, moodContent, moodHappy, moodExcited, moodEcstatic, moodGrateful, moodPeaceful)
        buttons.forEach { button ->
            button.alpha = 0.5f
        }
        
        // Highlight selected button
        when (emoji) {
            "😢" -> moodSad.alpha = 1.0f
            "😔" -> moodDown.alpha = 1.0f
            "😐" -> moodNeutral.alpha = 1.0f
            "😊" -> moodHappy.alpha = 1.0f
            "😄" -> moodExcited.alpha = 1.0f
        }
        
        // Show confirmation dialog for mood selection
        MaterialAlertDialogBuilder(this)
            .setTitle("Log Mood")
            .setMessage("Log \"${getMoodTitle(emoji)}\" as your current mood?")
            .setPositiveButton("Yes") { _, _ ->
                val mood = Mood(selectedMood, "")
                dataManager.addMood(mood)
                loadMoods()
                Toast.makeText(this, "Mood logged!", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel") { _, _ ->
                // Reset selection
                selectedMood = ""
                buttons.forEach { button ->
                    button.alpha = 0.5f
                }
            }
            .show()
    }
    
    private fun setupChart() {
        // Configure chart appearance
        moodChart.setBackgroundColor(Color.WHITE)
        moodChart.description.isEnabled = false
        moodChart.setTouchEnabled(true)
        moodChart.isDragEnabled = true
        moodChart.setScaleEnabled(true)
        moodChart.setPinchZoom(true)
        
        // Configure X-axis
        val xAxis = moodChart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.setDrawGridLines(false)
        xAxis.setDrawAxisLine(true)
        xAxis.textColor = Color.parseColor("#6B7280")
        xAxis.textSize = 12f
        
        // Configure Y-axis
        val leftAxis = moodChart.axisLeft
        leftAxis.setDrawGridLines(true)
        leftAxis.gridColor = Color.parseColor("#E5E7EB")
        leftAxis.textColor = Color.parseColor("#6B7280")
        leftAxis.textSize = 12f
        leftAxis.axisMinimum = 0f
        leftAxis.axisMaximum = 5f
        leftAxis.setLabelCount(6, true)
        
        val rightAxis = moodChart.axisRight
        rightAxis.isEnabled = false
        
        // Configure legend
        val legend = moodChart.legend
        legend.isEnabled = false
        
        // Load chart data
        loadChartData()
    }
    
    private fun loadChartData() {
        val moods = dataManager.getMoodTrend(7) // Last 7 days
        
        if (moods.isEmpty()) {
            chartDescription.text = "Start logging your mood to see trends"
            moodChart.clear()
            return
        }
        
        val entries = mutableListOf<Entry>()
        val labels = mutableListOf<String>()
        
        // Create entries for the last 7 days
        val calendar = Calendar.getInstance()
        for (i in 6 downTo 0) {
            calendar.timeInMillis = System.currentTimeMillis()
            calendar.add(Calendar.DAY_OF_MONTH, -i)
            
            val dayStart = calendar.apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis
            
            val dayEnd = dayStart + (24 * 60 * 60 * 1000) - 1
            
            // Find mood for this day
            val dayMood = moods.find { it.timestamp in dayStart..dayEnd }
            val moodValue = dayMood?.getMoodValue()?.toFloat() ?: 3f // Default neutral
            
            entries.add(Entry((6 - i).toFloat(), moodValue))
            
            // Create label for this day
            val dateFormat = SimpleDateFormat("MMM dd", Locale.getDefault())
            labels.add(dateFormat.format(Date(dayStart)))
        }
        
        // Create dataset
        val dataSet = LineDataSet(entries, "Mood Trend")
        dataSet.color = Color.parseColor("#6B46C1")
        dataSet.setCircleColor(Color.parseColor("#6B46C1"))
        dataSet.lineWidth = 3f
        dataSet.circleRadius = 6f
        dataSet.setDrawCircleHole(true)
        dataSet.circleHoleColor = Color.WHITE
        dataSet.setDrawValues(false)
        dataSet.setDrawFilled(true)
        dataSet.fillColor = Color.parseColor("#6B46C1")
        dataSet.fillAlpha = 50
        
        // Create line data
        val lineData = LineData(dataSet)
        moodChart.data = lineData
        
        // Set X-axis labels
        val xAxis = moodChart.xAxis
        xAxis.valueFormatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                val index = value.toInt()
                return if (index >= 0 && index < labels.size) {
                    labels[index]
                } else {
                    ""
                }
            }
        }
        
        // Refresh chart
        moodChart.invalidate()
        
        // Update description
        val averageMood = entries.map { it.y }.average()
        chartDescription.text = when {
            averageMood >= 4.5 -> "You've been feeling great lately! 😊"
            averageMood >= 3.5 -> "Your mood has been mostly positive 👍"
            averageMood >= 2.5 -> "Your mood has been neutral lately"
            else -> "Consider taking some time for self-care 💙"
        }
    }
    
    private fun loadMoods() {
        moodsContainer.removeAllViews()
        val moods = dataManager.getMoods().sortedByDescending { it.timestamp }
        
        if (moods.isEmpty()) {
            showEmptyState()
        } else {
            moods.forEach { mood ->
                val moodView = createMoodView(mood)
                moodsContainer.addView(moodView)
            }
        }
        
        // Refresh chart data
        loadChartData()
    }
    
    private fun showEmptyState() {
        val emptyView = layoutInflater.inflate(R.layout.empty_moods_state, moodsContainer, false)
        val addMoodButton = emptyView.findViewById<Button>(R.id.btn_add_first_mood)
        
        addMoodButton.setOnClickListener {
            showDetailedMoodDialog()
        }
        
        moodsContainer.addView(emptyView)
    }
    
    private fun createMoodView(mood: Mood): View {
        val moodView = layoutInflater.inflate(R.layout.item_mood_history, moodsContainer, false)
        
        val emojiText = moodView.findViewById<TextView>(R.id.mood_emoji)
        val titleText = moodView.findViewById<TextView>(R.id.mood_title)
        val noteText = moodView.findViewById<TextView>(R.id.mood_note)
        val timestampText = moodView.findViewById<TextView>(R.id.mood_timestamp)
        val moreButton = moodView.findViewById<ImageView>(R.id.mood_more)
        
        emojiText.text = mood.emoji
        
        // Generate title based on mood
        titleText.text = getMoodTitle(mood.emoji)
        
        if (mood.note.isNotEmpty()) {
            noteText.text = mood.note
            noteText.visibility = View.VISIBLE
        } else {
            noteText.text = getDefaultMoodDescription(mood.emoji)
            noteText.visibility = View.VISIBLE
        }
        
        // Format timestamp
        val dateFormat = SimpleDateFormat("MMM dd, h:mm a", Locale.getDefault())
        val calendar = Calendar.getInstance()
        val today = Calendar.getInstance()
        val yesterday = Calendar.getInstance().apply { add(Calendar.DAY_OF_MONTH, -1) }
        
        calendar.timeInMillis = mood.timestamp
        
        timestampText.text = when {
            isSameDay(calendar, today) -> "Today, ${SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(mood.timestamp))}"
            isSameDay(calendar, yesterday) -> "Yesterday, ${SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(mood.timestamp))}"
            else -> dateFormat.format(Date(mood.timestamp))
        }
        
        // Set up more button click listener
        moreButton.setOnClickListener {
            showMoodOptionsDialog(mood)
        }
        
        return moodView
    }
    
    private fun showMoodOptionsDialog(mood: Mood) {
        val options = arrayOf(getString(R.string.edit), getString(R.string.delete))
        
        MaterialAlertDialogBuilder(this)
            .setTitle("Mood Entry")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> showEditMoodDialog(mood)
                    1 -> showDeleteMoodDialog(mood)
                }
            }
            .show()
    }
    
    private fun showEditMoodDialog(mood: Mood) {
        val emojis = listOf("😊", "😄", "😐", "😔", "😢", "😴", "😍", "🤔", "😤", "😌")
        
        val dialogView = layoutInflater.inflate(R.layout.dialog_mood, null)
        val emojiGrid = dialogView.findViewById<GridLayout>(R.id.emoji_grid)
        val noteInput = dialogView.findViewById<EditText>(R.id.mood_note_input)
        val selectedEmoji = mutableListOf<String>()
        
        // Set current values
        noteInput.setText(mood.note)
        selectedEmoji.add(mood.emoji)
        
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
                alpha = if (emoji == mood.emoji) 1.0f else 0.5f
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
            .setTitle("Edit Mood")
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                if (selectedEmoji.isNotEmpty()) {
                    val updatedMood = mood.copy(
                        emoji = selectedEmoji.first(),
                        note = noteInput.text.toString().trim()
                    )
                    
                    // Update the mood in the list
                    val moods = dataManager.getMoods().toMutableList()
                    val moodIndex = moods.indexOfFirst { it.timestamp == mood.timestamp }
                    if (moodIndex != -1) {
                        moods[moodIndex] = updatedMood
                        dataManager.saveMoods(moods)
                        loadMoods()
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun showDeleteMoodDialog(mood: Mood) {
        MaterialAlertDialogBuilder(this)
            .setTitle("Delete Mood Entry")
            .setMessage("Are you sure you want to delete this mood entry?")
            .setPositiveButton("Yes") { _, _ ->
                val moods = dataManager.getMoods().toMutableList()
                moods.removeAll { it.timestamp == mood.timestamp }
                dataManager.saveMoods(moods)
                loadMoods()
            }
            .setNegativeButton("No", null)
            .show()
    }
    
    private fun getMoodTitle(emoji: String): String {
        return when (emoji) {
            "😢" -> "Feeling sad"
            "😔" -> "Feeling down"
            "😐" -> "Neutral mood"
            "😊" -> "Feeling great today!"
            "😄" -> "Excited and happy"
            "😴" -> "Tired but content"
            "🤗" -> "Grateful and happy"
            "😌" -> "Peaceful evening"
            else -> "Mood logged"
        }
    }
    
    private fun getDefaultMoodDescription(emoji: String): String {
        return when (emoji) {
            "😢" -> "Having a tough day. It's okay to feel this way."
            "😔" -> "Feeling a bit low today. Tomorrow will be better."
            "😐" -> "Just a regular day, nothing too exciting."
            "😊" -> "Had a productive morning and enjoyed my coffee. Looking forward to the evening walk."
            "😄" -> "Everything is going great! Feeling very positive."
            "😴" -> "Long day at work but accomplished a lot. Need to get better sleep tonight."
            "🤗" -> "Spent quality time with family. These moments are precious."
            "😌" -> "Meditation session went well. Feeling centered and calm."
            else -> "Mood entry recorded."
        }
    }
    
    private fun isSameDay(cal1: Calendar, cal2: Calendar): Boolean {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }
    
    
    private fun showDetailedMoodDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_mood, null)
        val emojiGrid = dialogView.findViewById<GridLayout>(R.id.emoji_grid)
        val noteInput = dialogView.findViewById<EditText>(R.id.mood_note_input)
        val selectedEmoji = mutableListOf<String>()
        
        val emojis = listOf("😢", "😔", "😐", "😊", "😄", "😴", "🤗", "😌", "🤔", "😤")
        
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
            .setTitle("Add Detailed Mood Entry")
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                if (selectedEmoji.isNotEmpty()) {
                    val mood = Mood(selectedEmoji.first(), noteInput.text.toString().trim())
                    dataManager.addMood(mood)
                    loadMoods()
                    Toast.makeText(this, "Mood entry saved!", Toast.LENGTH_SHORT).show()
                }
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
                startActivity(Intent(this, HabitActivity::class.java))
            }
            com.wellnessapp.tracker.models.NotificationType.HYDRATION -> {
                startActivity(Intent(this, MainActivity::class.java))
            }
            com.wellnessapp.tracker.models.NotificationType.MOOD -> {
                // Already in mood activity
                Toast.makeText(this, "You're already viewing mood journal", Toast.LENGTH_SHORT).show()
            }
            com.wellnessapp.tracker.models.NotificationType.PROGRESS, 
            com.wellnessapp.tracker.models.NotificationType.ACHIEVEMENT -> {
                // Already in mood activity, show progress
                Toast.makeText(this, "Great progress! Keep it up!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        loadMoods()
        updateNotificationBadge()
    }
}
