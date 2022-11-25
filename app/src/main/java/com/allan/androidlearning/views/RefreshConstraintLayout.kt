package com.allan.androidlearning.views

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.NestedScrollingParent3

abstract class RefreshConstraintLayout : ConstraintLayout, NestedScrollingParent3 {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    private val TAG = "alland"
    /**
     * 【重要】
     *
     * 当NSChild onTouchEvent(e)|onInterceptTouchEvent() Action.Down -> startNestedScroll()时, 会调用到该方法。
     * 通过返回值告诉系统是否需要对后续的滚动进行处理。
     *
     * child和target的区别：
     *
     * 如果是嵌套两层如:NSParent包含一个LinearLayout，LinearLayout里面才是NSChild类型的View。这个时候，
     * child指向LinearLayout，target指向NSChild；如果Parent直接就包含了NSChild，这个时候target和child都指向NSChild
     *
     * 【适合做什么？】
     * 适合做一些停止动画的动作。
     *
     * @param child：该ViewParen的包含NestedScrollingChild的直接子View，如果只有一层嵌套，和target是同一个View
     * @param target：本次嵌套滚动的NestedScrollingChild
     * @param axes：滚动方向
     * @param type: type of input
     * @return true:表示我需要进行处理，后续的滚动会触发相应的回调。false: NSParent则不需要处理，后面也就不会进行相应的回调.
     * 所以我们既然要做这个嵌套逻辑，就必须return true了。
     */
    override fun onStartNestedScroll(child: View, target: View, axes: Int, type: Int): Boolean {
        return isEnabled && isNestedScrollingEnabled
    }

    /**
     * 【不重要】
     * 如果onStartNestedScroll()方法返回的是true的话,那么紧接着就会调用该方法.它是让嵌套滚动在开始滚动之前,
     * 让布局容器(viewGroup)或者它的父类执行一些配置的初始化的。
     *
     * 【适合做什么？】
     * 适合做一些停止动画的动作。
     */
    override fun onNestedScrollAccepted(child: View, target: View, axes: Int, type: Int) {
    }

    /**
     * 【重要】 预处理，子view处理前回调
     *
     * 【适合】：对于recyclerView做为NSChild，而我们写父控件来嵌套rcv。就在这里决策我们需要滑动consumed多少距离。
     *
     * 这个是NSParent2的接口。是需要我们自行配合recyclerView等实现了NSChild2的类来分析。
     * 基本与上一致；onStartNestedScroll之后，在滑动之前。
     *
     * 当子view onTouchEvent(e) Action.MOVE -> dispatchNestedPreScroll() 会调用该方法。
     * 每次都调用；而且是NSChild必定先丢过来给NSParent。
     *
     * 也就是在NestedScrollingChild在处理滑动之前，
     * 会先将机会给NSParent处理。如果NSParent想先消费部分滚动距离，将消费的距离放入consumed
     *
     * @param dx：水平滑动距离
     * @param dy：处置滑动距离
     * @param consumed：表示Parent要消费的滚动距离,consumed[0]和consumed[1]分别表示父布局在x和y方向上消费的距离, 剩下的再给子view
     */
    override fun onNestedPreScroll(target: View, dx: Int, dy: Int, consumed: IntArray, type: Int) {
        Log.d(TAG, "preScroll>>> dx$dx, dy$dy")
    }

    override fun onNestedScroll(
        target: View,
        dxConsumed: Int,
        dyConsumed: Int,
        dxUnconsumed: Int,
        dyUnconsumed: Int,
        type: Int
    ) {
        onNestedScroll(
            target,
            dxConsumed,
            dyConsumed,
            dxUnconsumed,
            dyUnconsumed,
            type,
            intArrayOf(0, 0)
        )
    }

    /**
     * 【重要】
     *
     * 回顾前面的：
     * 第一，NSParent处理onNestedPreScroll消费一波；
     * 第二，还有剩余的，则NSChild消费；
     * 第三到了这里，是因为后续的MOVE持续的触发，然后，调用dispatchNestedScroll()方法时,会调用该方法。
     * 也就是开始分发处理嵌套滑动了
     *
     * @param dxConsumed：已经被target消费掉的水平方向的滑动距离
     * @param dyConsumed：已经被target消费掉的垂直方向的滑动距离
     * @param dxUnconsumed：未被tagert消费掉的水平方向的滑动距离
     * @param dyUnconsumed：未被tagert消费掉的垂直方向的滑动距离
     * @param consumed 当前view消耗的值，剩下的再回调给父view
     */
    override fun onNestedScroll(
        target: View/*滚动的子view*/,
        dxConsumed: Int/*子view已经消耗*/,
        dyConsumed: Int/*子view已经消耗*/,
        dxUnconsumed: Int/*子view未消耗*/,
        dyUnconsumed: Int/*子view未消耗*/,
        type: Int,
        consumed: IntArray
    ) {
        Log.d(TAG, "preScroll>>> Consumed dx$dxConsumed, dy$dyConsumed Unconsumed dx$dxUnconsumed dy$dyUnconsumed")
    }

    /**
     * 停止滚动了,当NSChild，停止滑动，Action.UP，
     * 调用stopNestedScroll()时会调用该方法。
     *
     * 适合做一些动画收尾工作。
     */
    override fun onStopNestedScroll(target: View, type: Int) {
    }

}