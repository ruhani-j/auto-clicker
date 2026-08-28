package com.autoclicker.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.graphics.Path
import android.view.accessibility.AccessibilityEvent
import com.autoclicker.data.ClickType
import kotlinx.coroutines.*

class AutoClickerAccessibilityService : AccessibilityService() {

    companion object {
        var instance: AutoClickerAccessibilityService? = null
            private set
    }

    override fun onServiceConnected() {
        instance = this
    }

    override fun onDestroy() {
        instance = null
        super.onDestroy()
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) = Unit
    override fun onInterrupt() = Unit

    fun performClick(
        x: Int,
        y: Int,
        clickType: ClickType,
        holdDurationMs: Long,
        jitterPx: Int
    ) {
        val jx = if (jitterPx > 0) (-jitterPx..jitterPx).random() else 0
        val jy = if (jitterPx > 0) (-jitterPx..jitterPx).random() else 0
        val fx = (x + jx).toFloat()
        val fy = (y + jy).toFloat()

        val duration = when (clickType) {
            ClickType.SINGLE_TAP -> 50L
            ClickType.PRESS_AND_HOLD -> holdDurationMs
        }

        val path = Path().apply { moveTo(fx, fy) }
        val stroke = GestureDescription.StrokeDescription(path, 0, duration)
        val gesture = GestureDescription.Builder().addStroke(stroke).build()
        dispatchGesture(gesture, null, null)
    }
}
