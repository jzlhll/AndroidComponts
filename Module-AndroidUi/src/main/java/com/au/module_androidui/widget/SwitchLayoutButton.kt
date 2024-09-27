package com.au.module_androidui.widget

import android.animation.ObjectAnimator
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import com.au.module_android.click.onClick
import com.au.module_android.utils.invisible
import com.au.module_android.utils.visible
import com.au.module_androidui.R
import com.au.module_androidui.databinding.LayoutSwitchButtonsBinding

/**
 * 自定义SwitchView 全新设计。 滑块。
 * 请注意：width必须设置为wrap_content。代码内部会让2个文字一样宽，与preview不同。请注意。
 */
class SwitchLayoutButton @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) :
    FrameLayout(
        context, attrs, defStyleAttr
    ) {
    private var mViewBinding: LayoutSwitchButtonsBinding
    var isLeft = true
        private set

    private var isInit = false
    fun isInited() = isInit

    /**
     * 点击切换的回调函数
     */
    var valueCallback : ((Boolean)->Unit)? = null

    private var isPost = false

    private val textColor:Int
    private val textSelectColor:Int
    private val textColorDisable:Int
    private val textSelectColorDisable:Int

    private var isDisabled = false

    init {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.SwitchLayoutButton)

        textColor = typedArray.getColor(R.styleable.SwitchLayoutButton_text_color,
            context.getColor(com.au.module_androidcolor.R.color.color_text_normal))
        textSelectColor = typedArray.getColor(R.styleable.SwitchLayoutButton_select_text_color,
            context.getColor(com.au.module_androidcolor.R.color.color_text_normal))

        textColorDisable = typedArray.getColor(R.styleable.SwitchLayoutButton_text_color_disable,
            context.getColor(com.au.module_androidcolor.R.color.color_switch_block_text_dis))
        textSelectColorDisable = typedArray.getColor(R.styleable.SwitchLayoutButton_select_text_color_disable,
            context.getColor(com.au.module_androidcolor.R.color.color_switch_block_text_sel_dis))

        val textPaddingHorz = typedArray.getDimension(R.styleable.SwitchLayoutButton_text_padding_horz, -1f).toInt()

        val leftStr = typedArray.getString(R.styleable.SwitchLayoutButton_first_str)
        val rightStr = typedArray.getString(R.styleable.SwitchLayoutButton_second_str)

        val paddingInner = typedArray.getDimension(R.styleable.SwitchLayoutButton_padding_inner, -1f).toInt()

        typedArray.recycle()

        mViewBinding = LayoutSwitchButtonsBinding.inflate(LayoutInflater.from(context), this, true)
        mViewBinding.leftTv.setPadding(textPaddingHorz, 0, textPaddingHorz, 0)
        mViewBinding.rightTv.setPadding(textPaddingHorz, 0, textPaddingHorz, 0)
        mViewBinding.leftTv.text = leftStr
        mViewBinding.rightTv.text = rightStr

        mViewBinding.root.onClick {
            if(isDisabled) return@onClick

            val newIsLeft = !isLeft
            setValue(newIsLeft)
            valueCallback?.invoke(newIsLeft)
        }

        post {
            if (paddingInner == 0) {
                mViewBinding.padding.visibility = View.GONE
            } else if (paddingInner > 0) {
                mViewBinding.padding.layoutParams = mViewBinding.padding.layoutParams.apply {
                    width = paddingInner
                }
            }

            val leftWidth = mViewBinding.leftTv.width
            val rightWidth = mViewBinding.rightTv.width
            if (leftWidth > rightWidth) {
                mViewBinding.rightTv.layoutParams = mViewBinding.rightTv.layoutParams.apply {
                    width = leftWidth
                }
            } else if (leftWidth < rightWidth) {
                mViewBinding.leftTv.layoutParams = mViewBinding.leftTv.layoutParams.apply {
                    width = rightWidth
                }
            }
            if (!isLeft) { //如果initValue比我们post要早。在这里初始化。
                mViewBinding.padding.post { //需要2层post才能确定位置。第一次post设置后，第二post才能得到selectBgView的变化
                    mViewBinding.selectBgView.translationX = (mViewBinding.selectBgView.width + mViewBinding.padding.width).toFloat()
                    mViewBinding.selectBgViewDisable.translationX = (mViewBinding.selectBgView.width + mViewBinding.padding.width).toFloat()
                    isPost = true
                }
            } else {
                isPost = true
            }
            changeTextColor()
        }
    }

    private fun changeTextColor() {
        if (isDisabled) {
            if(isLeft) {
                mViewBinding.leftTv.setTextColor(textSelectColorDisable)
                mViewBinding.rightTv.setTextColor(textColorDisable)
            } else {
                mViewBinding.rightTv.setTextColor(textSelectColorDisable)
                mViewBinding.leftTv.setTextColor(textColorDisable)
            }
        } else {
            if (isLeft) {
                mViewBinding.leftTv.setTextColor(textSelectColor)
                mViewBinding.rightTv.setTextColor(textColor)
            } else {
                mViewBinding.leftTv.setTextColor(textColor)
                mViewBinding.rightTv.setTextColor(textSelectColor)
            }
        }

        if (isDisabled) {
            mViewBinding.selectBgViewDisable.visible()
            mViewBinding.selectBgView.invisible()
        }
    }

    fun initValue(isLeft:Boolean, disable:Boolean, leftRightStrs:Pair<String, String>? = null) {
        isInit = true
        this.isLeft = isLeft

        isDisabled = disable

        if (leftRightStrs != null) {
            mViewBinding.leftTv.text = leftRightStrs.first
            mViewBinding.rightTv.text = leftRightStrs.second
        }

        if (isPost) {
            if (!isLeft) { //我们默认true。初始化为false。则需要特殊处理移动下block向右。
                mViewBinding.selectBgView.translationX = (mViewBinding.selectBgView.width + mViewBinding.padding.width).toFloat()
                mViewBinding.selectBgViewDisable.translationX = (mViewBinding.selectBgView.width + mViewBinding.padding.width).toFloat()
            }
            changeTextColor()
        }
    }

    fun setValue(isLeft: Boolean) {
        if (!isInit) throw RuntimeException()
        if (isDisabled) return
        //后续也可能后台改动，进而触发notifyItemChange bindData，则动画
        this.isLeft = isLeft
        if (isPost) {
            handleAnimal()
            changeTextColor()
        }
    }

    private fun handleAnimal() {
        val bgAnimator: ObjectAnimator
        val newIsLeftOn = isLeft
        bgAnimator = if (newIsLeftOn) {  //从 右边 -> 左边
            ObjectAnimator.ofFloat(mViewBinding.selectBgView, "translationX", 0f)
        } else { //从 true - false
            ObjectAnimator.ofFloat(
                mViewBinding.selectBgView, "translationX",
                0f, (mViewBinding.selectBgView.width + mViewBinding.padding.width).toFloat()
            )
        }
        bgAnimator.duration = 160
        bgAnimator.start()
    }
}