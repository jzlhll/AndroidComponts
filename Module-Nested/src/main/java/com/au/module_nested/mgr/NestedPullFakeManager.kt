package com.au.module_nested.mgr

import android.util.Log
import android.view.View
import com.au.module_nested.layout.NestedLayoutRefresher
import com.au.module_android.utils.dp

/**
 * author: allan
 * Time: 2022/11/25
 * Desc: coordinatorLayout+RecyclerView+AppTabLayout形成滑动嵌套
 * 下拉刷新的帮助类。
 *
 * 虚假拉动弹簧模式
 *
 * @param refresher 传入NestedXXXXLayout的refresher
 * @param bePullView 你希望被拉动的布局，不一定需要一定是recyclerView，也可以是任意的布局，比如预设界面，整个滑动View作为被拉动的部分比如host。
 */
internal class NestedPullFakeManager
(
    private val refresher: NestedLayoutRefresher,
    private val bePullView: View,
    private val params: SmoothParams = SmoothParams(80.dp, 0.35f)
)  : INestedPullManager {
    init {
        initial()
    }

    override fun loadingData() = false

    override fun pullDownIsTargetTranslated() = bePullView.translationY != 0f

    override fun refreshCompleted() {}

    override fun setOnRefreshAction(onRefreshAction: (() -> Unit)?) {}

    private fun initial() {
        refresher.pullDownLooseFingerCallback = { fra, state ->
            when (state) {
                NestedLayoutRefresher.PullDownShrinkState.START -> {
                    if(NestedLayoutRefresher.DEBUG) Log.d(NestedLayoutRefresher.TAG, ">shrink START $fra ${bePullView.translationY}")
                }
                NestedLayoutRefresher.PullDownShrinkState.MOVING -> {
                    bePullView.translationY = bePullView.translationY * fra
                    if(NestedLayoutRefresher.DEBUG) Log.d(NestedLayoutRefresher.TAG, ">shrink2MOVING $fra ${bePullView.translationY}")
                }
                NestedLayoutRefresher.PullDownShrinkState.END -> {
                    if(NestedLayoutRefresher.DEBUG) Log.d(NestedLayoutRefresher.TAG, ">shrink END $fra ${bePullView.translationY}")
                    bePullView.translationY = 0f
                }
            }
        }

        refresher.pullDownScrollingCallback = { dy ->
            val ny = -dy * params.realMoveRatio
            val translation1 = ny + bePullView.translationY

            bePullView.translationY = translation1
            if(NestedLayoutRefresher.DEBUG) Log.d(NestedLayoutRefresher.TAG, ">PullDown $translation1")
        }
    }
}