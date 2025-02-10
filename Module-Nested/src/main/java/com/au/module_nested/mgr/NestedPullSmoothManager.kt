package com.au.module_nested.mgr

import android.util.Log
import android.view.View
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.au.module_nested.layout.NestedLayoutRefresher
import com.au.module_android.utils.dp
import kotlin.math.max

/**
 * author: allan
 * Time: 2022/11/25
 * Desc: coordinatorLayout+RecyclerView+AppTabLayout形成滑动嵌套
 * 下拉刷新的帮助类。
 *
 * 真实拉动弹簧刷新模式
 * 采取的bePullView直接回弹到原来为止，等待外部调用[refreshCompleted] 才隐藏indicator。
 *
 * @param refresher 传入NestedXXXXLayout的refresher
 * @param bePullView 你希望被拉动的布局，不一定需要一定是recyclerView，也可以是任意的布局，比如预设界面，整个滑动View作为被拉动的部分比如host。
 * @param isIndicatorChildOfBePullView 顾名思义。内部则不做indicator的translation
 * @param progressIndicator 顶部的刷新圈，可以没有刷新indicator，并不影响刷新逻辑
 */
internal class NestedPullSmoothManager
(
    private val refresher: NestedLayoutRefresher,
    private val bePullView: View,
    progressIndicator: CircularProgressIndicator?,
    private val isIndicatorChildOfBePullView: Boolean,
    private val params: SmoothParams = SmoothParams(80f.dp.toInt(), 0.35f)
): INestedPullManager {
    private val indicator:IndicatorBase

    private var onRefreshAction:(() -> Unit)? = null

    /**
     * 触发加载数据函数，当下拉动作达成(即达到了一定的距离)以后，触发回调。
     */
    override fun setOnRefreshAction(onRefreshAction: (() -> Unit)?) {
        this.onRefreshAction = onRefreshAction
    }

    override fun setIndicatorDeltaHoldY(delta: Float) {
        indicator.holdTranslateDeltaY = delta
    }

    /**
     * 是否可以加载数据的标记
     */
    @Volatile
    var isLoadingData = false

    override fun loadingData() = isLoadingData

    override fun pullDownIsTargetTranslated() = bePullView.translationY != 0f

    init {
        indicator = if (progressIndicator == null) NoneIndicator() else RealIndicator(progressIndicator)

        initial()
    }

    private fun hideIndicator() {
        if(!isIndicatorChildOfBePullView) indicator.translationY = 0f
        if (onRefreshAction != null) {
            indicator.hide()
        }
    }

    private fun showIndicator() {
        if (onRefreshAction != null) {
            indicator.show()
        }
    }

    private fun initial() {
        refresher.pullDownLooseFingerCallback = { fra, state ->
            when (state) {
                NestedLayoutRefresher.PullDownShrinkState.START -> {
                    if(NestedLayoutRefresher.DEBUG) Log.d(NestedLayoutRefresher.TAG, "#shrink START $fra ${bePullView.translationY}")
                    if (!isLoadingData) {
                        val shouldRefresh = indicator.progress >= indicator.max //如果进度条超过，则可以加载。
                        val refreshAction = onRefreshAction
                        if (shouldRefresh && refreshAction != null) {
                            indicator.isIndeterminate = true
                            isLoadingData = true
                            refreshAction.invoke()
                        }
                    }
                }

                NestedLayoutRefresher.PullDownShrinkState.MOVING -> {
                    //不管是不是触发了loadingData，位移都还原。
                    if (NestedLayoutRefresher.DEBUG) Log.d(NestedLayoutRefresher.TAG, "#shrink0MOVING $fra ${bePullView.translationY}")
                    bePullView.translationY = bePullView.translationY * fra
                    //如果不是加载数据则退进度
                    if (!isLoadingData) {
                        if (!isIndicatorChildOfBePullView) {
                            indicator.translationY = indicator.translationY * fra
                        }
                        indicator.progress = (indicator.translationY * indicator.max / params.pullDownTriggerValue).toInt()
                    } else {
                        if (!isIndicatorChildOfBePullView) {
                            indicator.translationY = max(indicator.translationY * fra, indicator.holdTranslateY)
                        }
                    }
                }
                NestedLayoutRefresher.PullDownShrinkState.END -> {
                    if(NestedLayoutRefresher.DEBUG) Log.d(NestedLayoutRefresher.TAG, "#shrink END $fra ${bePullView.translationY}")
                    //不是一直转圈，则恢复; 或者是ResetForce，则代表我们已经加载了数据，也应该恢复
                    if(!isLoadingData) hideIndicator()
                    bePullView.translationY = 0f
                }
            }
        }

        refresher.pullDownScrollingCallback = { dy ->
            val ny = -dy * params.realMoveRatio
            val translation1 = ny + bePullView.translationY

            bePullView.translationY = translation1
            if (!isIndicatorChildOfBePullView) {
                val translation2 = ny + indicator.translationY
                indicator.translationY = translation2

                if(NestedLayoutRefresher.DEBUG) Log.d(NestedLayoutRefresher.TAG, "#PullDown $translation1 $translation2")
            } else {
                if(NestedLayoutRefresher.DEBUG) Log.d(NestedLayoutRefresher.TAG, "#PullDown $translation1")
            }

            if (translation1 < 0) { //代表上拉，不做显示
                hideIndicator()
            } else { //代表下拉
                indicator.progress = (indicator.max * translation1 / params.pullDownTriggerValue).toInt()
                indicator.isIndeterminate = false
                showIndicator()
            }
        }
    }

    override fun refreshCompleted() {
        isLoadingData = false
        indicator.isIndeterminate = false
        indicator.progress = 0
        if (NestedLayoutRefresher.DEBUG) Log.d(NestedLayoutRefresher.TAG, "refreshCompleted in nestedPullSmooth")
        hideIndicator()
    }
}