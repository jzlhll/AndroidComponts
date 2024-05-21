package com.au.module_androiduilight.widget

import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import androidx.constraintlayout.widget.ConstraintLayout
import com.au.module_android.utils.ViewBackgroundBuilder
import com.au.module_android.utils.dp
import com.au.module_androiduilight.R
import com.au.module_androiduilight.databinding.SwitchButtonsLayoutBinding
import kotlin.math.max

/**
 * 自定义SwitchView 全新设计。通过merge减少嵌套。
 */
class SwitchLayoutButton @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) :
    ConstraintLayout(
        context, attrs, defStyleAttr
    ) {
    private lateinit var mViewBinding: SwitchButtonsLayoutBinding
    var state = true
        private set

    private var isInit = false

    private var paddingInner = 0
    private fun initView(attrs: AttributeSet?) {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.SwitchLayoutButton)
        val leftStr = typedArray.getString(R.styleable.SwitchLayoutButton_first_str)
        val rightStr = typedArray.getString(R.styleable.SwitchLayoutButton_second_str)
        paddingInner = typedArray.getDimension(R.styleable.SwitchLayoutButton_padding_inner, 1.5f.dp).toInt()
        val bgColor = typedArray.getColor(R.styleable.SwitchLayoutButton_bg_color, Color.parseColor("#f0f0f0"))
        val selectColor = typedArray.getColor(R.styleable.SwitchLayoutButton_select_color, Color.WHITE)
        val bgCorner = typedArray.getDimension(R.styleable.SwitchLayoutButton_bg_corner, 8f.dp)
        val selectCorner = typedArray.getDimension(R.styleable.SwitchLayoutButton_select_corner, 7f.dp)

        typedArray.recycle()
        val view = inflate(context, R.layout.switch_buttons_layout, this)
        view.setPadding(paddingInner, paddingInner, paddingInner, paddingInner)

        mViewBinding = SwitchButtonsLayoutBinding.bind(view)
        mViewBinding.leftTv.text = leftStr
        mViewBinding.rightTv.text = rightStr
        view.background = ViewBackgroundBuilder().setBackground(bgColor).setCornerRadius(bgCorner).build()
        mViewBinding.selectBgView.background = ViewBackgroundBuilder().setBackground(selectColor).setCornerRadius(selectCorner).build()
    }

    fun setTextViewValue(left: String?, right: String?) {
        mViewBinding.leftTv.text = left
        mViewBinding.rightTv.text = right
        val leftWidth = mViewBinding.leftTv.paint.measureText(left)
        val rightWidth = mViewBinding.rightTv.paint.measureText(right)
        val maxWidth = max(leftWidth, rightWidth) + 12.dp
        mViewBinding.selectBgView.layoutParams = mViewBinding.selectBgView.layoutParams.apply {
            width = maxWidth.toInt()
        }
        mViewBinding.root.layoutParams = mViewBinding.root.layoutParams.apply {
            width = (2 * maxWidth).toInt()
        }
    }

    private var animRun: Runnable? = null
        get() {
            if (field == null) {
                field = Runnable { handleAnimal() }
            }
            return field
        }

    init {
        initView(attrs)
    }

    fun setOpenLeft(isOpen: Boolean) {
        if (state != isOpen) {
            state = isOpen
            post(animRun)
        }
    }

    private fun setInitStatus(isOpen: Boolean) {
        isInit = true
        if (state != isOpen) {
            state = isOpen
            post { //直接delay处理，初始化为非左边即可。
                mViewBinding.selectBgView.translationX = (width - mViewBinding.selectBgView.width - paddingInner * 2).toFloat()
            }
        }
    }

    fun setStatus(isOpen: Boolean) {
        if (!isInit) {//第一次初始化，我们只需要针对当前不是true的移动下即可
            setInitStatus(isOpen)
        } else {
            //后续也可能后台改动，进而触发notifyItemChange bindData，则动画
            state = isOpen
            handleAnimal()
        }
    }

    private fun handleAnimal() {
        val bgAnimator: ObjectAnimator
        val newIsLeftOn = state
        bgAnimator = if (newIsLeftOn) {  //从 false - true
            ObjectAnimator.ofFloat(mViewBinding.selectBgView, "translationX", 0f)
        } else { //从 true - false
            ObjectAnimator.ofFloat(
                mViewBinding.selectBgView, "translationX",
                0f, (width - mViewBinding.selectBgView.width - paddingInner * 2).toFloat()
            )
        }
        bgAnimator.duration = 160
        bgAnimator.start()
    }
}