package com.au.module_androidui.widget

import android.animation.ObjectAnimator
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import com.au.module_android.click.onClick
import com.au.module_androidui.databinding.BlocksSwitchLayoutBinding

/**
 * 黑白块的开关
 * 布局设置的时候，宽度是2x+4 dp。高度是x+4 dp
 */
class SwitchBlockButton @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) :
    ConstraintLayout(
        context, attrs, defStyleAttr
    ) {
    private var mViewBinding: BlocksSwitchLayoutBinding = BlocksSwitchLayoutBinding.inflate(LayoutInflater.from(context), this, true)
    var isLeft = true
        private set

    private var isInit = false

    /**
     * 点击切换的回调函数
     */
    var valueCallback : ((Boolean)->Unit)? = null

    init {
        mViewBinding.root.onClick {
            val newIsLeft = !isLeft
            setValue(newIsLeft)
            valueCallback?.invoke(newIsLeft)
        }
    }

    fun initValue(isLeft:Boolean) {
        isInit = true
        if (!isLeft) { //我们默认true。初始化为false。则需要特殊处理移动下block。
            this.isLeft = false
            post { //直接delay处理，初始化为非左边即可。
                mViewBinding.selectBgView.translationX = (mViewBinding.selectBgView.width).toFloat()
            }
        }
    }

    fun setValue(isLeft: Boolean) {
        if (!isInit) throw RuntimeException()
        //后续也可能后台改动，进而触发notifyItemChange bindData，则动画
        this.isLeft = isLeft
        handleAnimal()
    }

    private fun handleAnimal() {
        val bgAnimator: ObjectAnimator
        val newIsLeftOn = isLeft
        bgAnimator = if (newIsLeftOn) {  //从 右边 -> 左边
            ObjectAnimator.ofFloat(mViewBinding.selectBgView, "translationX", 0f)
        } else { //从 true - false
            ObjectAnimator.ofFloat(
                mViewBinding.selectBgView, "translationX",
                0f, (mViewBinding.selectBgView.width).toFloat()
            )
        }
        bgAnimator.duration = 160
        bgAnimator.start()
    }
}