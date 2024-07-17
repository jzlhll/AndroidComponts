package com.au.jobstudy.star

import android.animation.ValueAnimator
import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.core.content.res.ResourcesCompat
import androidx.interpolator.view.animation.FastOutLinearInInterpolator
import com.au.jobstudy.R
import com.au.module_android.utils.dp
import com.au.module_android.utils.gone
import com.au.module_android.utils.logd
import com.au.module_android.utils.visible

/**
 * @author allan
 * @date :2024/7/17 11:49
 * @description:
 */
class DingView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {
    private lateinit var image:ImageView

    private val startDrawableSize = 28.dp
    private val stopDrawableSize = 32.dp

    init {
        init(context)
    }

    private fun init(context: Context) {
        //初始化图片资源
        val dr = ResourcesCompat.getDrawable(context.resources, R.drawable.ic_ding, null)!!
        val image = ImageView(context)
        image.setImageDrawable(dr)
        image.layoutParams = LayoutParams(startDrawableSize, startDrawableSize)
        image.gone()
        addView(image)
        this.image = image
    }

    private val valueAnimator = ValueAnimator.ofFloat(1f, 0f).apply {
        interpolator = FastOutLinearInInterpolator()
        //开始延时时长
        startDelay = 100
        //动画时长
        setDuration(1000)
        //重复次数
        repeatCount = 0
        addUpdateListener { animation -> //获取当前值
            val f = animation.animatedValue as Float
            logd { "anim num: $f" }
            image.translationY = tranY * f
            if (f == 0f) {
                image.gone()
            }
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        valueAnimator.cancel()
    }

    var tranY = 0f

    fun yToTime(y:Int) : Long {
        return y / 200L * 100
    }

    /**
     * 开始动画
     */
    fun startRunning(x:Int, y:Int) {
        if(valueAnimator.isRunning) valueAnimator.cancel()

        image.translationX = x.toFloat() - startDrawableSize
        tranY = y.toFloat() - startDrawableSize
        image.translationY = tranY
        valueAnimator.duration = yToTime(y)

        image.visible()

        //开启动画
        valueAnimator.start()
    }
}