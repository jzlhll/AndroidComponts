package com.au.jobstudy.star

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.PointF
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.content.res.ResourcesCompat
import androidx.interpolator.view.animation.FastOutLinearInInterpolator
import com.au.jobstudy.R
import com.au.module_android.utils.dp
import com.au.module_android.utils.logd

/**
 * @author allan
 * @date :2024/7/17 11:49
 * @description:
 */
class DingView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ViewGroup(context, attrs, defStyleAttr) {
    private lateinit var mStarDrawable: Drawable
    private var mWidth = 0 //整个控件的宽度
    private var mHeight = 0 //整个控件的高度
    init {
        init(context)
    }

    private fun init(context: Context) {
        //初始化图片资源
        val dr = ResourcesCompat.getDrawable(context.resources, R.drawable.ic_ding, null)!!
        mStarDrawable = dr
        val image_heard = ImageView(context)
        image_heard.setImageDrawable(dr)
        image_heard.layoutParams = LayoutParams(24.dp, 24.dp)
        addView(image_heard)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        measureChildren(widthMeasureSpec, heightMeasureSpec)
        //获取view的宽高测量模式
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)

        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)

        //保存测量高度
        setMeasuredDimension(widthSize, heightSize)
    }
    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        logd { "onLayout l:$l,t:$t,r:$r,b:$b" }
        val child = getChildAt(0)
        val childW = child.measuredWidth
        val childH = child.measuredHeight
        child.layout((mWidth - childW) / 2, (mHeight - childH), (mWidth - childW) / 2 + childW, mHeight)
    }

    //属性动画和插补器了解
    //申明属性
    private var mStartPoint: PointF? = null  //属性动画和插补器了解

    //申明属性
    private var mEndPoint: PointF? = null

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        mStartPoint = PointF()
        mEndPoint = PointF()
        super.onSizeChanged(w, h, oldw, oldh)

        mWidth = measuredWidth
        mHeight = measuredHeight

        // 初始化各个点

        //借用子view控件中的宽高
        val child = getChildAt(0)
        val childW = child.measuredWidth
        val childH = child.measuredHeight

        mStartPoint!!.x = ((mWidth - childW) / 2).toFloat()
        mStartPoint!!.y = (mHeight - childH).toFloat()
        mEndPoint!!.x = ((mWidth - childW) / 2).toFloat()
        mEndPoint!!.y = (0 - childH).toFloat()
    }


    //向外部提供方法，用于点击事件触发动画发生
    /**
     * 开始动画
     */
    fun startRunning() {
//        val valueAnimator = ValueAnimator.ofInt(bezierTypeEvaluator, mStartPoint, mEndPoint)
//        valueAnimator.addUpdateListener { animation ->
//            val pointF = animation.animatedValue as PointF
//            getChildAt(0).x = pointF.x
//            getChildAt(0).y = pointF.y
//        }
//
//        valueAnimator.setDuration(3000)
//        valueAnimator.start()

        //指定动画的初始值和结束值，使用默认的估值器IntEvalutor
        //ofInt方法会帮我们创建ValueAnimator对象并将值设置进去
        val valueAnimator = ValueAnimator.ofInt(0, 1000)
        valueAnimator.interpolator = FastOutLinearInInterpolator()
        //开始延时时长
        valueAnimator.startDelay = 100
        //动画时长
        valueAnimator.setDuration(1000)
        //重复次数
        valueAnimator.repeatCount = 0

        //设置重复模式 ValueAnimator.RESTART正序重新开始  ValueAnimator.REVERSE逆序重新开始
        valueAnimator.addUpdateListener { animation -> //获取当前值
            val number = animation.animatedValue as Int
            logd { "number $number" }
        }

        //开启动画
        valueAnimator.start()
    }
}