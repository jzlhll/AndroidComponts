package com.allan.nested.widget

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import com.au.module_android.utils.dp
import kotlin.math.abs

/**
 * @author allan
 * @date :2024/8/5 18:01
 * @description:  放在Viewpager里面的rcv，需要处理下左右滑动的容易触发viewPager的切换问题
 */
class InViewPageRecyclerView : NoTopEffectRecyclerView {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) :super(context, attrs, defStyleAttr)

    private var startX = 0  //手指碰到屏幕时的 X坐标
    private var startY = 0

    private var mTriggerViewPagerHorzDistance:Int = 0
    /**
     * 设置允许父控件左右滑动的距离
     */
    fun setTriggerViewPagerHorzDistance(distance:Int) {
        mTriggerViewPagerHorzDistance = distance
    }

    private fun getTriggerViewPagerHorzDistance() : Int{
        if (mTriggerViewPagerHorzDistance == 0) {
            mTriggerViewPagerHorzDistance = 33.dp
        }
        return mTriggerViewPagerHorzDistance
    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        ev ?: return super.dispatchTouchEvent(null)

        when (ev.action) {
            MotionEvent.ACTION_DOWN -> {
                startX = ev.x.toInt()
                startY = ev.y.toInt()
                //按下去我们就不允许父控件接受事件
                parent.requestDisallowInterceptTouchEvent(true)
            }
            MotionEvent.ACTION_MOVE -> {
                val endX = ev.x.toInt()
                val disX = abs(endX - startX)
                val endY = ev.y.toInt()
                val disY = abs(endY - startY)

                /**
                 * 只有当x滑动的距离超过1个单位。而且小于3个竖向单位的时候，认为是横向滑动，才允许父控件接受事件
                 */
                if (disX > getTriggerViewPagerHorzDistance() && disY < getTriggerViewPagerHorzDistance() * 2) {
                    parent.requestDisallowInterceptTouchEvent(false)
                }
            }
            else ->
                //抬起手后恢复
                parent.requestDisallowInterceptTouchEvent(false)
        }

        return super.dispatchTouchEvent(ev)
    }
}