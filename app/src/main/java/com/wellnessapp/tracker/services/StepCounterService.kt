package com.wellnessapp.tracker.services

import android.app.Service
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.IBinder
import android.os.PowerManager
import com.wellnessapp.tracker.DataManager

class StepCounterService : Service(), SensorEventListener {
    
    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null
    private lateinit var dataManager: DataManager
    private lateinit var wakeLock: PowerManager.WakeLock
    
    private var lastStepTime = 0L
    private var stepThreshold = 0.8f // Threshold for step detection
    private var stepCount = 0
    private var lastX = 0f
    private var lastY = 0f
    private var lastZ = 0f
    
    override fun onCreate() {
        super.onCreate()
        
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        dataManager = DataManager(this)
        
        // Acquire wake lock to keep service running
        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "StepCounter::WakeLock")
        wakeLock.acquire(10*60*1000L /*10 minutes*/)
        
        // Load today's step count
        stepCount = dataManager.getTodayStepCount()
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        accelerometer?.let { sensor ->
            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL)
        }
        return START_STICKY
    }
    
    override fun onBind(intent: Intent?): IBinder? = null
    
    override fun onDestroy() {
        super.onDestroy()
        sensorManager.unregisterListener(this)
        wakeLock.release()
    }
    
    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_ACCELEROMETER) {
            val x = event.values[0]
            val y = event.values[1]
            val z = event.values[2]
            
            // Calculate acceleration magnitude
            val acceleration = Math.sqrt((x * x + y * y + z * z).toDouble()).toFloat()
            
            // Simple step detection algorithm
            if (isStepDetected(acceleration)) {
                val currentTime = System.currentTimeMillis()
                
                // Prevent multiple steps within 200ms
                if (currentTime - lastStepTime > 200) {
                    stepCount++
                    lastStepTime = currentTime
                    
                    // Save step count
                    dataManager.setTodayStepCount(stepCount)
                    
                    // Send broadcast to update UI
                    val intent = Intent("STEP_COUNT_UPDATED")
                    intent.putExtra("step_count", stepCount)
                    sendBroadcast(intent)
                }
            }
            
            lastX = x
            lastY = y
            lastZ = z
        }
    }
    
    private fun isStepDetected(acceleration: Float): Boolean {
        // Simple step detection based on acceleration changes
        val deltaAcceleration = Math.abs(acceleration - Math.sqrt((lastX * lastX + lastY * lastY + lastZ * lastZ).toDouble()).toFloat())
        return deltaAcceleration > stepThreshold
    }
    
    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Not needed for this implementation
    }
}
