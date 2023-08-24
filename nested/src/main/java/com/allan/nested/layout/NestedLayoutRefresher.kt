package com.allan.nested.layout

import android.animation.ValueAnimator
import android.util.Log
import android.view.View
import android.view.ViewGroup
import androidx.core.animation.doOnEnd
import androidx.core.animation.doOnStart
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.allan.nested.mgr.INestedPull
import com.allan.nested.mgr.INestedPullManager
import com.allan.nested.mgr.NestedPullFakeManager
import com.allan.nested.mgr.NestedPullSmoothManager
import com.allan.nested.mgr.SmoothParams
import com.au.module_android.utils.dpGlobal
import java.lang.Long.max

/**
 * @author allan.jiang
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

    private lateinit var pullManager: INestedPull

    private var isDisablePullUpEffect = false
    private var isDisablePullDownEffect = false

    /**
     * 早一点调用。必须调用，否则，出错。
     *
     * 定义为虚拟刷新模式
     *
     * @param bePullView 你希望被拉动的布局，不一定需要一定是recyclerView，也可以是任意的布局，比如预设界面，整个滑动View作为被拉动的部分比如host。
     */
    fun initEarlyAsFake(
        bePullView: View,
        params: SmoothParams = SmoothParams(80f.dpGlobal.toInt(), 0.35f)
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
        params: SmoothParams = SmoothParams(80f.dpGlobal.toInt(), 0.35f)
    ) {
        pullManager = NestedPullSmoothManager(this, bePullView, progressIndicator, isIndicatorChildOfBePullView, params)
    }

    override fun refreshCompleted() {
        val pm = pullManager
        abortTouch = false
        if (pm is INestedPullManager) {
            val delay = max(0L, atLeastShowLoadingTime - (System.currentTimeMillis() - abortTouchTimestamp))
            layout.postDelayed({
                if (layout.isAttachedToWindow) {
                    pm.refreshCompleted()
                }
            }, delay)
        }
    }

    override fun setOnRefreshAction(onRefreshAction: (() -> Unit)?) {
        val pm = pullManager
        if (pm is INestedPullManager) {
            pm.setOnRefreshAction(onRefreshAction)
        }
    }

    override fun loadingData() = pullManager.loadingData()

    fun disablePullUpEffect() {
        isDisablePullUpEffect = true
    }

    fun disablePullDownEffect() {
        isDisablePullDownEffect = true
    }

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
                    abortTouchTimestamp = System.currentTimeMillis()
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
            if (dy < 0) {
                if (!isDisablePullDownEffect) {
                    consumed[0] = dx
                    consumed[1] = dy //这里我们进行全部消费，later: 虽然我认为没必要，子View也滑不动，因为我们只在顶部的时候往下拉的场景。
                    onPullDownMoving(target, dy.toFloat(), consumed)
                }
            } else {
                if (!isDisablePullUpEffect) { //某些跟coordinateLayout联动的时候，需要禁用，使用默认的弹簧效果即可。
                    consumed[0] = dx
                    consumed[1] = dy
                    onPullUpMoving(target, dy.toFloat(), consumed)
                }
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

    private fun onPullUpMoving(target: View, dy: Float, consumed: IntArray) {
        if(looseFingerAnim.isRunning) looseFingerAnim.cancel()
        pullUpScrollingCallback?.invoke(dy)
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
     * 这里实现假弹簧，不做真实加载，即不触发加载动作。
     */
    internal var pullUpScrollingCallback:((offY:Float)->Unit)? = null

    /**
     * 当拉动动作结束，开始回弹。
     * 包含三类动作：开始，停止，回弹中。
     */
    internal var pullDownLooseFingerCallback:((offY:Float, state: PullDownShrinkState)->Unit)? = null
}