package com.wellnessapp.tracker.utils

import android.app.Activity
import android.content.Intent
import android.os.Bundle

object ActivityTransitionUtils {
    
    /**
     * Navigate to another activity with smooth slide transition
     */
    fun navigateWithSlideTransition(
        fromActivity: Activity,
        toActivity: Class<*>,
        finishCurrent: Boolean = true,
        extras: Bundle? = null
    ) {
        val intent = Intent(fromActivity, toActivity)
        extras?.let { intent.putExtras(it) }
        
        fromActivity.startActivity(intent)
        
        if (finishCurrent) {
            fromActivity.finish()
        }
        
        // Apply slide transition
        fromActivity.overridePendingTransition(
            com.wellnessapp.tracker.R.anim.slide_in_right,
            com.wellnessapp.tracker.R.anim.slide_out_left
        )
    }
    
    /**
     * Navigate back with reverse slide transition
     */
    fun navigateBackWithTransition(fromActivity: Activity) {
        fromActivity.finish()
        fromActivity.overridePendingTransition(
            com.wellnessapp.tracker.R.anim.slide_in_left,
            com.wellnessapp.tracker.R.anim.slide_out_right
        )
    }
}
