package com.wellnessapp.tracker

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.wellnessapp.tracker.models.Notification
import com.wellnessapp.tracker.models.NotificationType
import java.text.SimpleDateFormat
import java.util.*

class EditProfileActivity : AppCompatActivity() {

    private lateinit var dataManager: DataManager
    private lateinit var fullNameInput: TextInputEditText
    private lateinit var emailInput: TextInputEditText
    private lateinit var phoneInput: TextInputEditText
    private lateinit var dobInput: TextInputEditText
    private lateinit var heightInput: TextInputEditText
    private lateinit var weightInput: TextInputEditText
    private lateinit var genderInput: TextInputEditText
    private lateinit var primaryGoalInput: TextInputEditText
    private lateinit var targetWeightInput: TextInputEditText
    private lateinit var profileAvatar: TextView
    private lateinit var changePhotoText: TextView
    private lateinit var saveButton: TextView

    private val dateFormat = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault())
    private val calendar = Calendar.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        initializeViews()
        setupDataManager()
        setupClickListeners()
        loadUserData()
    }

    private fun initializeViews() {
        fullNameInput = findViewById(R.id.full_name_input)
        emailInput = findViewById(R.id.email_input)
        phoneInput = findViewById(R.id.phone_input)
        dobInput = findViewById(R.id.dob_input)
        heightInput = findViewById(R.id.height_input)
        weightInput = findViewById(R.id.weight_input)
        genderInput = findViewById(R.id.gender_input)
        primaryGoalInput = findViewById(R.id.primary_goal_input)
        targetWeightInput = findViewById(R.id.target_weight_input)
        profileAvatar = findViewById(R.id.profile_avatar)
        changePhotoText = findViewById(R.id.change_photo_text)
        saveButton = findViewById(R.id.save_button)
    }

    private fun setupDataManager() {
        dataManager = DataManager(this)
    }

    private fun setupClickListeners() {
        // Date of Birth picker
        dobInput.setOnClickListener {
            showDatePicker()
        }

        // Gender picker
        genderInput.setOnClickListener {
            showGenderPicker()
        }

        // Change photo
        changePhotoText.setOnClickListener {
            showPhotoOptions()
        }

        // Save button
        saveButton.setOnClickListener {
            saveProfile()
        }
    }

    private fun loadUserData() {
        // Load user data from DataManager
        val userName = dataManager.getUserName()
        val userEmail = dataManager.getUserEmail()
        val userPhone = dataManager.getUserPhone()
        val userDOB = dataManager.getUserDOB()
        val userHeight = dataManager.getUserHeight()
        val userWeight = dataManager.getUserWeight()
        val userGender = dataManager.getUserGender()
        val userPrimaryGoal = dataManager.getUserPrimaryGoal()
        val userTargetWeight = dataManager.getUserTargetWeight()
        
        // Set the loaded data
        fullNameInput.setText(userName)
        emailInput.setText(userEmail)
        phoneInput.setText(userPhone)
        dobInput.setText(userDOB)
        heightInput.setText(userHeight)
        weightInput.setText(userWeight)
        genderInput.setText(userGender)
        primaryGoalInput.setText(userPrimaryGoal)
        targetWeightInput.setText(userTargetWeight)
        
        // Set avatar initial
        profileAvatar.text = userName.take(1).uppercase()
    }

    private fun showDatePicker() {
        val datePickerDialog = DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                calendar.set(year, month, dayOfMonth)
                dobInput.setText(dateFormat.format(calendar.time))
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.show()
    }

    private fun showGenderPicker() {
        val genders = arrayOf("Male", "Female", "Other", "Prefer not to say")
        val currentGender = genderInput.text.toString()
        val selectedIndex = genders.indexOf(currentGender).takeIf { it >= 0 } ?: 0

        MaterialAlertDialogBuilder(this)
            .setTitle("Select Gender")
            .setSingleChoiceItems(genders, selectedIndex) { dialog, which ->
                genderInput.setText(genders[which])
                dialog.dismiss()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showPhotoOptions() {
        val options = arrayOf("Take Photo", "Choose from Gallery", "Remove Photo")
        
        MaterialAlertDialogBuilder(this)
            .setTitle("Change Profile Photo")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> {
                        // Take photo
                        showToast("Camera functionality would be implemented here")
                    }
                    1 -> {
                        // Choose from gallery
                        showToast("Gallery functionality would be implemented here")
                    }
                    2 -> {
                        // Remove photo - set default avatar
                        profileAvatar.text = fullNameInput.text.toString().take(1).uppercase()
                        showToast("Photo removed")
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun saveProfile() {
        val fullName = fullNameInput.text.toString().trim()
        val email = emailInput.text.toString().trim()
        val phone = phoneInput.text.toString().trim()
        val dob = dobInput.text.toString().trim()
        val height = heightInput.text.toString().trim()
        val weight = weightInput.text.toString().trim()
        val gender = genderInput.text.toString().trim()
        val primaryGoal = primaryGoalInput.text.toString().trim()
        val targetWeight = targetWeightInput.text.toString().trim()

        // Validation
        if (fullName.isEmpty()) {
            fullNameInput.error = "Full name is required"
            return
        }

        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailInput.error = "Valid email is required"
            return
        }

        // Save to DataManager
        dataManager.setUserName(fullName)
        dataManager.setUserEmail(email)
        dataManager.setUserPhone(phone)
        dataManager.setUserDOB(dob)
        dataManager.setUserHeight(height)
        dataManager.setUserWeight(weight)
        dataManager.setUserGender(gender)
        dataManager.setUserPrimaryGoal(primaryGoal)
        dataManager.setUserTargetWeight(targetWeight)
        
        // Update avatar
        profileAvatar.text = fullName.take(1).uppercase()

        // Show success message
        showToast("Profile updated successfully!")
        
        // Add notification
        val notification = Notification(
            id = UUID.randomUUID().toString(),
            type = NotificationType.PROGRESS,
            title = "Profile Updated",
            message = "Your profile information has been successfully updated.",
            timestamp = System.currentTimeMillis()
        )
        dataManager.addNotification(notification)
        
        // Go back to settings
        finish()
    }

    private fun showToast(message: String) {
        android.widget.Toast.makeText(this, message, android.widget.Toast.LENGTH_SHORT).show()
    }

    fun onBackPressed(view: View) {
        onBackPressed()
    }

    override fun onBackPressed() {
        // Check if there are unsaved changes
        val currentName = dataManager.getUserName()
        val newName = fullNameInput.text.toString().trim()
        
        if (currentName != newName) {
            MaterialAlertDialogBuilder(this)
                .setTitle("Unsaved Changes")
                .setMessage("You have unsaved changes. Do you want to save before leaving?")
                .setPositiveButton("Save") { _, _ ->
                    saveProfile()
                }
                .setNegativeButton("Discard") { _, _ ->
                    super.onBackPressed()
                }
                .setNeutralButton("Cancel", null)
                .show()
        } else {
            super.onBackPressed()
        }
    }
}
