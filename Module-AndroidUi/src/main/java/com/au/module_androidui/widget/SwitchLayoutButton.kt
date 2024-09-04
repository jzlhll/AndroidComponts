package com.au.module_androidui.widget

import android.animation.ObjectAnimator
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import com.au.module_android.click.onClick
import com.au.module_android.utils.ViewBackgroundBuilder
import com.au.module_androidui.databinding.SwitchButtonsLayoutBinding

/**
 * 自定义SwitchView 全新设计。todo 通过merge减少嵌套。
 * 请注意：width必须设置为wrap_content。代码内部会让2个文字一样宽，与preview不同。请注意。
 */
class SwitchLayoutButton @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) :
    FrameLayout(
        context, attrs, defStyleAttr
    ) {
    private var mViewBinding: SwitchButtonsLayoutBinding
    var isLeft = true
        private set

    private var isInit = false

    /**
     * 点击切换的回调函数
     */
    var valueCallback : ((Boolean)->Unit)? = null

    private var isPost = false

    private val textColor:Int
    private val textSelectColor:Int

    init {
        val res = context.resources
        val typedArray = context.obtainStyledAttributes(attrs, com.au.module_androidui.R.styleable.SwitchLayoutButton)

        val bgColor = typedArray.getColor(com.au.module_androidui.R.styleable.SwitchLayoutButton_bg_color,
            context.getColor(com.au.module_androidcolor.R.color.color_switch_block_bg))
        val selectColor = typedArray.getColor(com.au.module_androidui.R.styleable.SwitchLayoutButton_select_color,
            context.getColor(com.au.module_androidcolor.R.color.color_switch_block_sel_bg))

        textColor = typedArray.getColor(com.au.module_androidui.R.styleable.SwitchLayoutButton_text_color,
            context.getColor(com.au.module_androidcolor.R.color.color_switch_block_text))
        textSelectColor = typedArray.getColor(com.au.module_androidui.R.styleable.SwitchLayoutButton_select_text_color,
            context.getColor(com.au.module_androidcolor.R.color.color_switch_block_text_sel))

        val bgCorner = typedArray.getDimension(com.au.module_androidui.R.styleable.SwitchLayoutButton_bg_corner,
            res.getDimension(com.au.module_androidcolor.R.dimen.switch_layout_default_corner))
        val selectCorner = typedArray.getDimension(com.au.module_androidui.R.styleable.SwitchLayoutButton_select_corner,
            res.getDimension(com.au.module_androidcolor.R.dimen.switch_layout_default_sel_corner))
        val leftStr = typedArray.getString(com.au.module_androidui.R.styleable.SwitchLayoutButton_first_str)
        val rightStr = typedArray.getString(com.au.module_androidui.R.styleable.SwitchLayoutButton_second_str)

        val paddingInner = typedArray.getDimension(com.au.module_androidui.R.styleable.SwitchLayoutButton_padding_inner, -1f).toInt()

        typedArray.recycle()
        mViewBinding = SwitchButtonsLayoutBinding.inflate(LayoutInflater.from(context), this, true)

        mViewBinding.leftTv.text = leftStr
        mViewBinding.rightTv.text = rightStr
        mViewBinding.root.background = ViewBackgroundBuilder().setBackground(bgColor).setCornerRadius(bgCorner).build()
        mViewBinding.selectBgView.background = ViewBackgroundBuilder().setBackground(selectColor).setCornerRadius(selectCorner).build()

        mViewBinding.root.onClick {
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
                mViewBinding.padding.post { //需要2层post。第一次post设置后，第二post才能得到selectBgView的变化
                    mViewBinding.selectBgView.translationX = (mViewBinding.selectBgView.width + mViewBinding.padding.width).toFloat()
                    isPost = true
                }
            } else {
                isPost = true
            }
            changeTextColor()
        }
    }

    private fun changeTextColor() {
        if (isLeft) {
            mViewBinding.leftTv.setTextColor(textSelectColor)
            mViewBinding.rightTv.setTextColor(textColor)
        } else {
            mViewBinding.leftTv.setTextColor(textColor)
            mViewBinding.rightTv.setTextColor(textSelectColor)
        }
    }

    fun initValue(isLeft:Boolean, leftRightStrs:Pair<String, String>? = null) {
        isInit = true
        if (leftRightStrs != null) {
            mViewBinding.leftTv.text = leftRightStrs.first
            mViewBinding.rightTv.text = leftRightStrs.second
        }

        isInit = true
        if (!isLeft) { //我们默认true。初始化为false。则需要特殊处理移动下block。
            this.isLeft = false
            post { //直接delay处理，初始化为非左边即可。
                if (isPost) { //如果这个post比init函数的post早就不干活。
                    mViewBinding.selectBgView.translationX = (mViewBinding.selectBgView.width + mViewBinding.padding.width).toFloat()
                    changeTextColor()
                }
            }
        }
    }

    fun setValue(isLeft: Boolean) {
        if (!isInit) throw RuntimeException()
        //后续也可能后台改动，进而触发notifyItemChange bindData，则动画
        this.isLeft = isLeft
        handleAnimal()
        changeTextColor()
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