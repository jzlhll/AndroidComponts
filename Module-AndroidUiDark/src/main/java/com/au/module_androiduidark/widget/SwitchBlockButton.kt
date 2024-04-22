package com.au.module_androidex.widget

import android.animation.ObjectAnimator
import android.content.Context
import android.util.AttributeSet
import androidx.constraintlayout.widget.ConstraintLayout
import com.au.module_androidex.R
import com.au.module_androidex.databinding.BlocksSwitchLayoutBinding

/**
 * 黑白块的开关
 */
class SwitchBlockButton @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) :
    ConstraintLayout(
        context, attrs, defStyleAttr
    ) {
    private lateinit var mViewBinding: BlocksSwitchLayoutBinding
    var state = true
        private set

    private var isInit = false

    private fun initView() {
        val view = inflate(context, R.layout.blocks_switch_layout, this)
        mViewBinding = BlocksSwitchLayoutBinding.bind(view)
    }

    private var animRun: Runnable? = null
        get() {
            if (field == null) {
                field = Runnable { handleAnimal() }
            }
            return field
        }

    init {
        initView()
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
                mViewBinding.selectBgView.translationX = (width - mViewBinding.selectBgView.width).toFloat()
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
                0f, (width - mViewBinding.selectBgView.width).toFloat()
            )
        }
        bgAnimator.duration = 160
        bgAnimator.start()
    }
}