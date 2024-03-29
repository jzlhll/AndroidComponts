package com.allan.nongyaofloat

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent

/**
 * @author allan
 * @date :2024/3/19 10:04
 * @description:
 */
class NongYaoAutoClickService : AccessibilityService() {
    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        performGlobalAction(GLOBAL_ACTION_DPAD_DOWN)
        performGlobalAction(GLOBAL_ACTION_DPAD_UP)
    }

    override fun onInterrupt() {
        TODO("Not yet implemented")
    }
}