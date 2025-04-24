package com.au.module_androidui.widget

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.FrameLayout
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.core.view.children
import androidx.core.view.size
import com.au.module_android.utils.dp
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlin.math.cos
import kotlin.math.sin

/**
 * Created by asd on 2/20/2017.
 */
class UnfoldButton @JvmOverloads constructor(context: Context,
                                             attrs: AttributeSet? = null,
                                             defStyleAttr: Int = 0)
        : FloatingActionButton(context, attrs, defStyleAttr) {

    /**
     * 通过tag，来寻找是它。
     */
    class Element(val tag:String,
                  @DrawableRes val initImage:Int,
                  @ColorInt val color:Int?) {
        var listener: OnClickListener? = null
    }

    var flag: Int = FOLDING
    private var mRotatable = true //图标是否应该旋转
    private lateinit var mRootView: ViewGroup //父view
    private lateinit var mBackground: FrameLayout //菜单背景幕布

    private var mAlpha = 1f //透明度
    private var length = 200 //子view展开的距离
    private var mScale = 0.8f //展开之后的缩放比例
    private var mDuration = 400 //动画时长
    var elementList: MutableList<Element> = ArrayList<Element>() //保存添加的button
    private var totalAngle = 90

    init {
        //绘制完成后再调用
        viewTreeObserver.addOnGlobalLayoutListener(object : OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                init()
                freshElement()
                viewTreeObserver.removeOnGlobalLayoutListener(this) //取消监听
            }
        })

        //点击展开菜单
        setOnClickListener { v: View? -> expendMenu() }
    }

    /**
     * 初始化  要在view绘制完成后调用
     */
    private fun init() {
        val rootView = parent as ViewGroup
        mRootView = rootView
        mBackground = FrameLayout(context).apply {
            layoutParams = ViewGroup.LayoutParams(
                rootView.width,
                rootView.height
            )
            setBackgroundColor(resources.getColor(com.au.module_androidcolor.R.color.color_floating_btn_bg, null))
            setOnClickListener { v: View? -> expendMenu() }
            alpha = 0f
            visibility = INVISIBLE

            rootView.addView(this)
        }
    }

    /**
     * 通过addElement添加的button，在这里才是真正的添加到mBackground中
     */
    private fun freshElement() {
        for (element in elementList) {
            val b = FloatingActionButton(context)
            b.tag = element.tag
            b.setImageResource(element.initImage) //图标
            val lp = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT
            )
            //使添加的button偏移到unfoldButton
            lp.leftMargin = left
            lp.topMargin = top
            b.layoutParams = lp
            b.setOnClickListener { v ->
                element.listener?.onClick(v)
                expendMenu() //缩回菜单
            }
            b.visibility = INVISIBLE
            val color = element.color
            if(color != null) b.backgroundTintList = ColorStateList.valueOf(color) //背景颜色
            b.imageTintList = ColorStateList.valueOf(Color.WHITE)
            b.setMaxImageSize(36.dp)
            mBackground.addView(b) //添加
        }
    }

    /**
     * 改变mBackground的状态  显示或者是隐藏
     */
    private fun changeBackgroundStatus() {
        val alpha: ObjectAnimator
        if (flag == FOLDING) { //处于折叠状态
            alpha = ObjectAnimator.ofFloat(mBackground, "alpha", mAlpha)
            mBackground.visibility = VISIBLE
        } else {
            alpha = ObjectAnimator.ofFloat(mBackground, "alpha", 0f)
            alpha.addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    mBackground.visibility = INVISIBLE
                }
            })
        }
        alpha.duration = mDuration.toLong()
        alpha.start()
    }

    /**
     * 折叠与展开菜单
     * 如果button展开角度不对  要修改这个
     */
    private fun expendMenu() {
        val count = mBackground.size
        changeBackgroundStatus() //改变mBackground的状态
        if (flag == FOLDING) { //折叠状态  要展开
            for (i in 0..<count) {
                val view = mBackground.getChildAt(i)
                view.visibility = VISIBLE
                //开始平移  第一个参数是view 第二个是角度
                setTranslation(view, totalAngle / (count - 1) * (i - 0))
            }
            //开始旋转
            if (mRotatable) setRotateAnimation(this, DO_ROTATE)
            flag = UNFOLDING
        } else {
            setBackTranslation()
            flag = FOLDING
            //开始反向旋转 恢复原来的样子
            if (mRotatable) setRotateAnimation(this, RECOVER_ROTATE)
        }
    }

    /**
     * 添加button
     * 由于调用的时候一般都是在onCreate里面调用，所以直接添加到mBackground会有空指针异常
     * 所以先加入到一个链表，然后等绘制完成后再调用freshElement()添加
     *
     * @param tag           用于标记名字寻找回来重新设置图片
     * @param initImageId   菜单的图标
     * @param listener      菜单的点击事件
     * @param color         菜单按钮的背景颜色
     */
    fun addElementOnCreate(tag:String, initImageId: Int, color: Int?, listener: OnClickListener?) {
        elementList.add(Element(tag, initImageId, color).also { it.listener = listener })
    }

    fun findElement(tag:String): FloatingActionButton? {
        return mBackground.children.find { it.tag == tag } as? FloatingActionButton
    }

    /**
     * 设置旋转动画
     *
     * @param view
     * @param flag
     */
    fun setRotateAnimation(view: View?, flag: Int) {
        val rotate = if (flag == DO_ROTATE) ObjectAnimator.ofFloat(view, "rotation", 135f)
            else ObjectAnimator.ofFloat(view, "rotation", 0f)
        rotate.duration = mDuration.toLong()
        rotate.start()
    }

    /**
     * 菜单展开动画  缩放+透明度+平移
     *
     * @param view
     * @param angle
     */
    fun setTranslation(view: View?, angle: Int) {
        val x = (length * sin(Math.toRadians(angle.toDouble()))).toInt()
        val y = (length * cos(Math.toRadians(angle.toDouble()))).toInt()
        Log.d("ICE", "angle" + angle + "y:" + y)
        val tX = ObjectAnimator.ofFloat(view, "translationX", -x.toFloat())
        val tY = ObjectAnimator.ofFloat(view, "translationY", -y.toFloat())
        val alpha = ObjectAnimator.ofFloat(view, "alpha", 1f)
        val scaleX = ObjectAnimator.ofFloat(view, "scaleX", mScale)
        val scaleY = ObjectAnimator.ofFloat(view, "scaleY", mScale)

        val set = AnimatorSet()
        set.play(tX).with(tY).with(alpha)
        set.play(scaleX).with(scaleY).with(tX)
        set.duration = mDuration.toLong()
        set.interpolator = AccelerateDecelerateInterpolator()
        set.start()
    }

    /**
     * 菜单缩回动画  与上面相反
     */
    private fun setBackTranslation() {
        val count = mBackground.size
        for (i in 0..<count) {
            val view = mBackground.getChildAt(i)
            val tX = ObjectAnimator.ofFloat(view, "translationX", 0f)
            val tY = ObjectAnimator.ofFloat(view, "translationY", 0f)
            val alpha = ObjectAnimator.ofFloat(view, "alpha", 0f) //透明度 0为完全透明
            val set = AnimatorSet() //动画集合
            set.play(tX).with(tY).with(alpha)
            set.duration = mDuration.toLong() //持续时间
            set.interpolator = AccelerateDecelerateInterpolator()
            set.start()
            //动画完成后 设置为不可见
            val finalI = i
            set.addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    view.visibility = INVISIBLE
                }
            })
        }
    }


    //设置方法
    fun setMyAlpha(mAlpha: Float) {
        this.mAlpha = mAlpha
    }

    fun setMyLength(length: Int) {
        this.length = length
    }

    fun setMyScale(mScale: Float) {
        this.mScale = mScale
    }

    fun setMyDuration(mDuration: Int) {
        this.mDuration = mDuration
    }

    fun setMyRotatable(mRotatable: Boolean) {
        this.mRotatable = mRotatable
    }

    fun setMyAngle(angle: Int) {
        this.totalAngle = angle
    }

    companion object {
        private const val RECOVER_ROTATE = -1 //恢复旋转之前的状态
        private const val UNFOLDING = 2 //菜单展开状态
        private const val FOLDING = 1 //菜单折叠状态
        private const val DO_ROTATE = 1 //旋转动画
    }
}
