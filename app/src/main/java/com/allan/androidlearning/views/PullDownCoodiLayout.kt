package com.allan.androidlearning.views

import android.animation.ObjectAnimator
import android.content.Context
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.GestureDetectorCompat
import androidx.core.view.forEach
import com.allan.androidlearning.utils.callDensity
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.progressindicator.CircularProgressIndicator
import kotlin.math.abs
import kotlin.math.max

/**
 * author: allan.jiang
 * Time: 2022/11/25
 * Desc: 比较适合。内部并没有滑动的子控件。
 */
class PullDownCoodiLayout : ConstraintLayout, GestureDetector.OnGestureListener {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    val progressIndicator by lazy(LazyThreadSafetyMode.NONE) { CircularProgressIndicator(context) }

    private val density:Float

    init {
        addRingView()
        post {
            forEachChild {
                if (it is AppBarLayout) {
                    setAppBarLayout(it)
                }
            }
        }
        density = callDensity(context)
    }

    private fun View?.forEachChild(action: ((View) -> Unit)) {
        if (this == null) {
            return
        }
        action.invoke(this)
        if (this is ViewGroup) {
            this.forEach {
                it.forEachChild(action)
            }
        }
    }

    private val gestureDetectorCompat by lazy(LazyThreadSafetyMode.NONE)  { GestureDetectorCompat(context, this) }

    /**
     *是否支持下拉刷新
     */
    var enableRefresh = false

    /**
     * 添加下拉指示器
     */
    private fun addRingView() {
        val lp = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
        lp.bottomToTop = LayoutParams.PARENT_ID
        lp.startToStart = LayoutParams.PARENT_ID
        lp.endToEnd = LayoutParams.PARENT_ID
        lp.bottomMargin = 16.dp
        progressIndicator.indicatorSize = 20.dp
        progressIndicator.trackThickness = 2.dp
        addView(progressIndicator, lp)
    }

    /**
     * dp2px
     */
    fun dip(value: Float): Float {
        return value * density
    }

    fun dip(value: Int): Int {
        return (value * density).toInt()
    }

    val Int.dp: Int
        get() = dip(this)

    val Float.dp: Float
        get() = dip(this)

    private var isScrollTop = true

    /**
     * 触发下拉刷新的值
     */
    var pullRefreshValue = 80f.dp

    /**
     * 实际滑动距离比例
     */
    var offsetRatio = 0.4f

    private fun setAppBarLayout(appBarLayout: AppBarLayout) {
        appBarLayout.addOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener { _, verticalOffset ->
            isScrollTop = verticalOffset == 0
        })
    }

    private var needIntercept: Boolean? = null
    private var currentX: Float = 0f
    private var currentY: Float = 0f
    private var isRefreshing = false

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        if (isRefreshing) {
            return super.onInterceptTouchEvent(ev)
        }
        if (stopAnim.isRunning) {
            return true
        }
        when (ev?.action) {
            MotionEvent.ACTION_DOWN -> {
                needIntercept = null
                currentX = ev.x
                currentY = ev.y
                progressIndicator.isIndeterminate = false
                gestureDetectorCompat.onTouchEvent(ev)
            }
            MotionEvent.ACTION_MOVE -> {
                if (needIntercept == null) {
                    val offY = ev.y - currentY
                    val offX = ev.x - currentX
                    if (abs(offX) > abs(offY) * 2) {
                        //当前为横向滑动
                        needIntercept = false
                    } else {
                        if (offY != 0f) {
                            needIntercept = offY > 0 && isScrollTop
                        }
                    }
                }
                if (needIntercept == true) {
                    return true
                }
            }
            MotionEvent.ACTION_UP,
            MotionEvent.ACTION_CANCEL -> {
                stopTouchEvent()
            }
        }
        return super.onInterceptTouchEvent(ev)
    }


    override fun onTouchEvent(ev: MotionEvent): Boolean {
        ev ?: return super.onTouchEvent(ev)
        if (isRefreshing) {
            return super.onInterceptTouchEvent(ev)
        }
        if (stopAnim.isRunning) {
            return super.onTouchEvent(ev)
        }
        when (ev.action) {
            MotionEvent.ACTION_UP,
            MotionEvent.ACTION_CANCEL -> {
                stopTouchEvent()
            }
        }
        return gestureDetectorCompat.onTouchEvent(ev)
//        val offY = ev.y - currentY
//        startPull(offY)
//        when (ev.action) {
//            MotionEvent.ACTION_UP,
//            MotionEvent.ACTION_CANCEL -> {
//                stopTouchEvent()
//            }
//            else -> {}
//        }
//        return true
    }


    private fun startPull(offY: Float) {
        progressIndicator.visibility = if(enableRefresh) VISIBLE else GONE
        val tY = offY * offsetRatio
        val finalY = max(0f, progressIndicator.translationY + tY)
        progressIndicator.progress = (100 * finalY / pullRefreshValue).toInt()
        forEach {
            it.translationY = finalY
        }
    }

    /**
     * 通知滑动
     */
    private val stopAnim = ObjectAnimator.ofFloat().apply {
        duration = 320
        addUpdateListener {
            val v = it.animatedValue as Float
            forEach { view ->
                view.translationY = v
            }
        }
    }

    /**
     * 设置刷新时候的监听
     */
    var onRefreshListener: Function0<Unit>? = null

    private fun stopTouchEvent() {
        isRefreshing = enableRefresh && progressIndicator.translationY > pullRefreshValue
        if (isRefreshing) {
            progressIndicator.isIndeterminate = true
            stopAnim.setFloatValues(progressIndicator.translationY, pullRefreshValue)
            onRefreshListener?.invoke()
        } else {
            stopAnim.setFloatValues(progressIndicator.translationY, 0f)
        }
        if (stopAnim.isRunning) {
            stopAnim.cancel()
        }
        stopAnim.start()
    }

    /**
     * 停止刷新
     */
    fun stopRefresh() {
        isRefreshing = false
        progressIndicator.isIndeterminate = false
        if (progressIndicator.translationY == 0f) {
            return
        }
        if (stopAnim.isRunning) {
            stopAnim.cancel()
        }
        stopAnim.setFloatValues(progressIndicator.translationY, 0f)
        stopAnim.start()
    }

    override fun onDown(p0: MotionEvent): Boolean {
        return true
    }

    override fun onShowPress(p0: MotionEvent) {
    }

    override fun onSingleTapUp(p0: MotionEvent): Boolean {
        return true
    }

    override fun onScroll(e1: MotionEvent?, e2: MotionEvent, distanceX: Float, distanceY: Float): Boolean {
        startPull(-y)
        return true
    }

    override fun onLongPress(p0: MotionEvent) {
    }

    override fun onFling(e1: MotionEvent?, e2: MotionEvent, velocityX: Float, velocityY: Float): Boolean {
        return true
    }

}