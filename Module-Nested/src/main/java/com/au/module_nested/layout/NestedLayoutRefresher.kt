package com.au.module_nested.layout

import android.animation.ValueAnimator
import android.os.SystemClock
import android.util.Log
import android.view.View
import android.view.ViewGroup
import androidx.core.animation.doOnEnd
import androidx.core.animation.doOnStart
import androidx.recyclerview.widget.RecyclerView
import com.au.module_nested.mgr.INestedPullManager
import com.au.module_nested.mgr.NestedPullFakeManager
import com.au.module_nested.mgr.NestedPullSmoothManager
import com.au.module_nested.mgr.SmoothParams
import com.au.module_nested.widget.NoTopEffectRecyclerView
import com.au.module_android.utils.dp
import com.google.android.material.progressindicator.CircularProgressIndicator
import java.lang.Long.max

/**
 * @author au
 * Date: 2023/1/8
 *
 * 辅助一个XXXLayout, 让他成为1个可以下拉刷新的控件。比如辅助NestedCoordinatorLayout。
 * 你也可以参考NestedCoordinatorLayout，实现NestedXXXLayout。
 *
 * todo：实现stick模式。
 */
class NestedLayoutRefresher(private val layout:ViewGroup) : INestedPullManager {
    companion object {
        internal const val DEBUG = true
        internal const val TAG = "allan_nested"
    }

    init {
        layout.addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {
            override fun onViewAttachedToWindow(v: View) {
            }

            override fun onViewDetachedFromWindow(v: View) {
                resetRefreshCompleted() //恢复状态
            }
        })

        val childCount = layout.childCount
        for (childIndex in 0 until childCount) {
            val view = layout.getChildAt(childIndex)
            if (view is RecyclerView) {
                if (DEBUG) Log.d(TAG, "set no edge effect for $view")
                //研究许久，如果想要下拉刷新的嵌套RecyclerView的Layout，则禁用内部的RecyclerView的edge效果即可。
                //rcv1.3.2的库，额外处理了edge consume掉了nest的距离。因此，我们这里搞一个假的空Effect进去，也符合我们
                //下拉刷新的本质。要求
                view.edgeEffectFactory = NoTopEffectRecyclerView.NoEdgeTopEffectFactory(view.context)
            }
        }
    }

    //内部使用：用做于下拉状态的标志。表示我们是否接收本次为触发类型。
    private enum class PullDownState {
        Stopped,
        Accept,
        NotAccept
    }

    /**
     * 内外部使用：用做于下拉后回弹的状态。
     */
    enum class PullDownShrinkState {
        START,
        MOVING,
        END
    }

    /**
     * 松手后的回弹时间
     */
    var looseFingerAnimDuration = 360L

    /**
     * 至少显示加载进度的时间
     */
    var atLeastShowLoadingTime = 1200L

    /**
     * 当手松开的时候，会调用它，判断我们的目标子View是否位移过。
     */
    override fun pullDownIsTargetTranslated() = pullManager.pullDownIsTargetTranslated()

    private lateinit var pullManager: INestedPullManager

    /**
     * 早一点调用。必须调用，否则，出错。
     *
     * 定义为虚拟刷新模式
     *
     * @param bePullView 你希望被拉动的布局，不一定需要一定是recyclerView，也可以是任意的布局，比如预设界面，整个滑动View作为被拉动的部分比如host。
     */
    fun initEarlyAsFake(
        bePullView: View,
        params: SmoothParams = SmoothParams(80.dp, 0.39f)
    ) {
        pullManager = NestedPullFakeManager(this, bePullView, params)
    }

    /**
     * 早一点调用。必须调用，否则，出错。
     *
     * 定义为直接回弹型的刷新弹簧模式
     *
     * @param bePullView 你希望被拉动的布局，不一定需要一定是recyclerView，也可以是任意的布局，比如预设界面，整个滑动View作为被拉动的部分比如host。
     * @param progressIndicator 顶部的刷新圈。可以没有indicator，也会刷新
     * @param isIndicatorChildOfBePullView 顾名思义。内部则不做indicator的translation
     */
    fun initEarlyAsSmooth(
        bePullView: View,
        progressIndicator: CircularProgressIndicator?,
        isIndicatorChildOfBePullView: Boolean,
        params: SmoothParams = SmoothParams(80.dp, 0.39f)
    ) {
        pullManager = NestedPullSmoothManager(this, bePullView, progressIndicator, isIndicatorChildOfBePullView, params)
    }

    private fun resetRefreshCompleted() {
        abortTouch = false
        pullManager.refreshCompleted()
    }

    override fun setIndicatorDeltaHoldY(delta: Float) {
        pullManager.setIndicatorDeltaHoldY(delta)
    }

    override fun refreshCompleted() {
        if (DEBUG) Log.d(TAG, "refreshCompleted")
        abortTouch = false
        val delay = max(0L, atLeastShowLoadingTime - (SystemClock.elapsedRealtime() - abortTouchTimestamp))
        layout.postDelayed({
            resetRefreshCompleted()
        }, delay)
    }

    override fun setOnRefreshAction(onRefreshAction: (() -> Unit)?) {
        pullManager.setOnRefreshAction(onRefreshAction)
    }

    override fun loadingData() = pullManager.loadingData()

    ///////////////////////////////////

    //本轮是否接受成为是下拉刷新的状态。
    @Volatile
    private var mIsATurnScrollAccept = PullDownState.Stopped

    //当我回弹的时候，拦截掉触摸事件
    @Volatile
    internal var abortTouch = false
        private set

    private var abortTouchTimestamp = 0L

    private val looseFingerAnim by lazy(LazyThreadSafetyMode.NONE) {
        ValueAnimator
            .ofInt(0, 0)
            .apply {
                duration = looseFingerAnimDuration
                doOnStart {
                    abortTouch = true
                    abortTouchTimestamp = SystemClock.elapsedRealtime()
                    if (DEBUG) Log.d(TAG, "looseFinger Anim start")
                    pullDownLooseFingerCallback?.invoke(1f, PullDownShrinkState.START)
                }
                addUpdateListener {
                    val f = 1 - it.animatedFraction
                    pullDownLooseFingerCallback?.invoke(f, PullDownShrinkState.MOVING)
                }
                doOnEnd {
                    if (!loadingData()) {
                        abortTouch = false
                    }
                    if (DEBUG) Log.d(TAG, "looseFinger Anim end")
                    pullDownLooseFingerCallback?.invoke(0f, PullDownShrinkState.END)
                }
            }
    }

    internal fun onHostNestedPreScroll(target: View, dx: Int, dy: Int, consumed: IntArray, tag:String? = null) {
        if(DEBUG) Log.d(TAG, "$tag onHostNestedPreScroll $dy  ${consumed[0]}  ${consumed[1]}")
        //如果是接收了本次的refresh滑动模式；我们则将全部消费行为给到onPullDownMoving去处理。
        if (mIsATurnScrollAccept == PullDownState.Accept) {
            if (true) { //其实可以做点事情
                consumed[0] = dx
                consumed[1] = dy //这里我们进行全部消费，later: 虽然我认为没必要，子View也滑不动，因为我们只在顶部的时候往下拉的场景。
                onPullDownMoving(target, dy.toFloat(), consumed)
            }
        }
    }

    internal fun onHostNestedScroll(
        target: View,
        dxConsumed: Int,
        dyConsumed: Int,
        dxUnconsumed: Int,
        dyUnconsumed: Int, tag:String? = null
    ) {
        if(DEBUG) Log.d(TAG, "onHostNestedScroll $dyConsumed  $dyUnconsumed")
        //只有当子View不做刷新(dyConsumed == 0)；而且方向是往下滑(即dyUnconsumed<0) 我们认为进入了下拉场景。
        //但是，还得考虑必须是从没有动作的场景进入的。即之前是stopNested的。
        if (mIsATurnScrollAccept == PullDownState.Stopped) {
            mIsATurnScrollAccept = if (dyConsumed == 0 && dyUnconsumed != 0) {
                PullDownState.Accept
            } else {
                PullDownState.NotAccept
            }
        }
    }

    /**
     * 当我们拉动下滑，还没有松手的时候，一直回调这个。
     * 可以考虑放开，继承干点别的事情。
     */
    private fun onPullDownMoving(target: View, dy: Float, consumed: IntArray) {
        if(looseFingerAnim.isRunning) looseFingerAnim.cancel()
        pullDownScrollingCallback?.invoke(dy)
    }

    internal fun onHostPullDownReleased(tag:String? = null) {
        if(DEBUG) Log.d(TAG, "$tag on HostPullDownReleased")
        if (mIsATurnScrollAccept != PullDownState.Stopped) {
            mIsATurnScrollAccept = PullDownState.Stopped
            if (pullDownIsTargetTranslated()) {
                looseFingerAnim.start()
            }
        }
    }

    /**
     * 布局逻辑已经接受此次事情为refresh动作，随着拉动不断的回调offsetY。你将跟随
     *
     */
    internal var pullDownScrollingCallback:((offY:Float)->Unit)? = null

    /**
     * 当拉动动作结束，开始回弹。
     * 包含三类动作：开始，停止，回弹中。
     */
    internal var pullDownLooseFingerCallback:((offY:Float, state: PullDownShrinkState)->Unit)? = null
}