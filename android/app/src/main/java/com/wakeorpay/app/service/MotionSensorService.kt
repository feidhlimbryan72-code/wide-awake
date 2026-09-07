package com.wakeorpay.app.service

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.sqrt

/**
 * Tracks physical device shaking using Android SensorManager.
 * Filters out minor jitter or tilting by requiring acceleration magnitude reversals above 18 m/s².
 */
class MotionSensorService(context: Context) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    private val _shakeCount = MutableStateFlow(0)
    val shakeCount: StateFlow<Int> = _shakeCount.asStateFlow()

    private val _isCompleted = MutableStateFlow(false)
    val isCompleted: StateFlow<Boolean> = _isCompleted.asStateFlow()

    private var targetCount = 100
    private var lastDirectionPositive = true
    private val accelerationThreshold = 18.0f // m/s^2 (approx 1.84g)

    fun startTracking(target: Int) {
        stopTracking()
        this.targetCount = target
        _shakeCount.value = 0
        _isCompleted.value = false

        accelerometer?.let { sensor ->
            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_GAME)
        }
    }

    fun stopTracking() {
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null || event.sensor.type != Sensor.TYPE_ACCELEROMETER) return

        val x = event.values[0]
        val y = event.values[1]
        val z = event.values[2]

        val magnitude = sqrt((x * x + y * y + z * z).toDouble()).toFloat()

        if (magnitude > accelerationThreshold) {
            val currentDirectionPositive = y > 0
            if (currentDirectionPositive != lastDirectionPositive) {
                lastDirectionPositive = currentDirectionPositive
                val nextCount = _shakeCount.value + 1
                _shakeCount.value = nextCount

                if (nextCount >= targetCount) {
                    _isCompleted.value = true
                    stopTracking()
                }
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}
