package com.au.module_android.utils

import android.view.MotionEvent
import android.view.View
import androidx.annotation.Keep

/**
 * block ， 1表示单击。2表示按压中，会自动每隔20ms回调一次直到结束。
 */
@Keep
class ContinuousTouchListener(private val view: View,
                              private val block:(Int)->Unit) : View.OnTouchListener{
    private var startTime = 0L
    private var onceStart = false
    private val triggerPressRun = Runnable {
        block(2)
        continuousRunDelay()
    }

    private fun continuousRunDelay() {
        view.postDelayed(triggerPressRun, 50)
    }

    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        event?: return false
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                startTime = System.currentTimeMillis()
                onceStart = true
                view.removeCallbacks(triggerPressRun)
                view.postDelayed(triggerPressRun, 330)
                return true
            }
            MotionEvent.ACTION_UP -> {
                onceStart = false
                view.removeCallbacks(triggerPressRun)
                // 当抬起时计算按压时间
                val duration = System.currentTimeMillis() - startTime
                if (duration < 300) {
                    block(1) //单击
                    v?.performClick()
                }
            }
        }
        return false
    }
}