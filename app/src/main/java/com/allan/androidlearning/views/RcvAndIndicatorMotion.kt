package com.allan.androidlearning.views

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.progressindicator.CircularProgressIndicator

/**
 * author: allan.jiang
 * Time: 2022/11/25
 * Desc:
 */
class RcvAndIndicatorMotion(private val host:NestedCoordinatorLayout,
                            private val rcv: RecyclerView,
                            private val indicator: CircularProgressIndicator) {
    private val pullDownTriggerValue = 160f

    //{{ 设置参数
    /**
     * 触发加载数据。
     */
     var loadingDataCallback:(()->Unit)? = null

    /**
     * 真实的移动比例偏差。一般不用设置。
     */
    val realMoveRatio = 0.35f

    /**
     * 外部调用，重置为普通状态
     */
    fun reset() {
        indicator.visibility = View.GONE
        host.forceResetAnim()
        isLoadingData = false
    }

    /**
     * 是否可以加载数据的标记
     */
    var isLoadingData = false
    //}}

    private val pullDownEndY:Float by lazy {
        indicator.height.toFloat()
    }

    init {
        host.pullDownForceShrinkCallback = {
                y, state ->
            val offY = y * realMoveRatio
            rcv.translationY = offY

            if (state == NestedCoordinatorLayout.PullDownShrinkState.END
                || state == NestedCoordinatorLayout.PullDownShrinkState.START) {
                indicator.visibility = View.GONE
            }
        }

        host.pullDownShrinkCallback = {
                y, state ->
            val offY = y * realMoveRatio
            when (state) {
                NestedCoordinatorLayout.PullDownShrinkState.START -> {
                    //如果进度条超过，则可以加载。
                    val shouldRefresh:Boolean = indicator.progress >= indicator.max
                    indicator.isIndeterminate = shouldRefresh
                    rcv.translationY = offY
                    indicator.translationY = offY

                    if (shouldRefresh && !isLoadingData) {
                        isLoadingData = true
                        loadingDataCallback?.invoke()
                    }
                }
                NestedCoordinatorLayout.PullDownShrinkState.MOVING -> {
                    if (indicator.isIndeterminate) {
                        indicator.translationY = offY
                        rcv.translationY = if (offY <= pullDownEndY) pullDownEndY else offY
                    } else {
                        //如果不是一直转圈，即表示现在是不能loading，则退进度即可
                        indicator.progress = (offY * indicator.max / pullDownTriggerValue).toInt()
                        rcv.translationY = offY
                        indicator.translationY = offY
                    }
                }
                NestedCoordinatorLayout.PullDownShrinkState.END -> {
                    if (indicator.isIndeterminate) {
                        //如果是一直转圈的情况，即加载的情况，那么，我们将
                        indicator.visibility = View.VISIBLE
                        indicator.translationY = offY
                        rcv.translationY = if (offY <= pullDownEndY) pullDownEndY  else offY
                    } else {
                        indicator.visibility = View.GONE
                        rcv.translationY = offY
                        indicator.translationY = offY
                        isLoadingData = false
                    }
                }
            }
        }

        host.pullDownScrollingCallback = { i ->
            val it = i * realMoveRatio
            rcv.translationY = it
            indicator.translationY = it

            indicator.visibility = View.VISIBLE
            indicator.isIndeterminate = false
            indicator.progress = (it * indicator.max / pullDownTriggerValue).toInt()
        }
    }
}