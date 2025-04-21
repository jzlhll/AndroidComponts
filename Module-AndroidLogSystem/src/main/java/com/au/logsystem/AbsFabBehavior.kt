package com.au.logsystem

import android.content.Context
import android.os.SystemClock
import android.util.AttributeSet
import android.view.View
import androidx.annotation.Keep
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.ViewCompat
import com.au.module_android.utils.logdNoFile
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.Runnable

@Keep
abstract class AbsFabBehavior : FloatingActionButton.Behavior {
    constructor() :super()
    constructor(context: Context, attrs: AttributeSet):super(context, attrs)

    private val startTypes = mutableListOf<Int>()
    private var isHide = false

    private val triggerDistance = 5

    private fun logDebug(s:String) {
        if(true) logdNoFile { s }
    }

    override fun onStartNestedScroll(coordinatorLayout: CoordinatorLayout, child: FloatingActionButton, directTargetChild: View, target: View, axes: Int, type: Int): Boolean {
        val isVertical = axes == ViewCompat.SCROLL_AXIS_VERTICAL
        logDebug("nested on start $isVertical axes $axes type $type")
        return isVertical
    }

    override fun onNestedScroll(
        coordinatorLayout: CoordinatorLayout,
        child: FloatingActionButton,
        target: View,
        dxConsumed: Int,
        dyConsumed: Int,
        dxUnconsumed: Int,
        dyUnconsumed: Int,
        type: Int,
        consumed: IntArray
    ) {
        super.onNestedScroll(coordinatorLayout, child, target, dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, type, consumed)
        //child -> Floating Action Button
        if (!isHide && (dyConsumed > triggerDistance || dyConsumed < -triggerDistance)) {
            logDebug("nested scroll dyConsumed $dyConsumed dyUnconsumed $dyUnconsumed type $type")
            child.removeCallbacks(showRunnable)
            hide(child)
            mLastHideTs = SystemClock.elapsedRealtime()
            isHide = true
        }
    }

    override fun onNestedScrollAccepted(coordinatorLayout: CoordinatorLayout, child: FloatingActionButton, directTargetChild: View, target: View, axes: Int, type: Int) {
        super.onNestedScrollAccepted(coordinatorLayout, child, directTargetChild, target, axes, type)
        logDebug("nested accept dyConsumed axes $axes type $type")
        val isVertical = axes == ViewCompat.SCROLL_AXIS_VERTICAL
        if (isVertical) {
            startTypes.add(type)
        }
    }

    override fun onStopNestedScroll(coordinatorLayout: CoordinatorLayout, child: FloatingActionButton, target: View, type: Int) {
        super.onStopNestedScroll(coordinatorLayout, child, target, type)
        startTypes.remove(type)
        logDebug("nested stop type $type")
        if (startTypes.isEmpty()) {
            showChild(child)
            isHide = false
        }
    }

    /**
     * 最短隐藏时间
     */
    abstract val minTimeForHide:Int

    private var mLastHideTs = 0L

    private var child: FloatingActionButton? = null
    private val showRunnable = Runnable{
        if(child?.isAttachedToWindow == true) show(child!!)
    }

    private fun showChild(child: FloatingActionButton) {
        val ts = SystemClock.elapsedRealtime()
        val deltaTime = ts - mLastHideTs
        if (deltaTime < minTimeForHide) {
            val leftTime = minTimeForHide - deltaTime
            this.child = child
            child.postDelayed(showRunnable, leftTime)
        } else {
            show(child)
        }
    }

    abstract fun show(child: FloatingActionButton)
    abstract fun hide(child: FloatingActionButton)
}