package com.au.module_androidui.widget

import android.animation.ObjectAnimator
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import com.au.module_android.click.onClick
import com.au.module_android.utils.unsafeLazy
import com.au.module_androidui.databinding.SwitchButtonBinding

/**
 * 黑白块的开关
 * 布局设置的时候，宽度是2x+4 dp。高度是x+4 dp
 */
class SwitchButton @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) :
    ConstraintLayout(
        context, attrs, defStyleAttr
    ) {
    private var mViewBinding = SwitchButtonBinding.inflate(LayoutInflater.from(context), this, true)

    /**
     * 默认就是关闭
     */
    var isClosed = true
        private set

    private var isInit = false

    private val moveDistance by unsafeLazy {
        val width = width - context.resources.getDimension(com.au.module_androidcolor.R.dimen.switch_button_padding) * 2
        val btnWidth = mViewBinding.selectBgView.width
        width - btnWidth
    }

    /**
     * 点击切换的回调函数
     */
    var valueCallback : ((Boolean)->Unit)? = null

    init {
        mViewBinding.root.onClick {
            val newIsLeft = !isClosed
            setValue(newIsLeft)
            valueCallback?.invoke(newIsLeft)
        }
    }

    fun initValue(close:Boolean) {
        isInit = true
        if (!close) { //我们默认true。初始化为false。则需要特殊处理移动下block。
            this.isClosed = false
            post { //直接delay处理，初始化为非左边即可。
                mViewBinding.selectBgView.translationX = moveDistance
                mViewBinding.root.setBackgroundResource(com.au.module_androidcolor.R.drawable.switch_btn_opened)
            }
        }
    }

    fun setValue(close: Boolean) {
        if (!isInit) throw RuntimeException()
        //后续也可能后台改动，进而触发notifyItemChange bindData，则动画
        this.isClosed = close
        handleAnimal()
    }

    private fun handleAnimal() {
        val bgAnimator: ObjectAnimator
        val newIsLeftOn = isClosed
        bgAnimator = if (newIsLeftOn) {  //从 右边 -> 左边
            ObjectAnimator.ofFloat(mViewBinding.selectBgView, "translationX", 0f)
        } else { //从 true - false
            ObjectAnimator.ofFloat(
                mViewBinding.selectBgView, "translationX",
                0f, moveDistance
            )
        }
        bgAnimator.duration = 160
        bgAnimator.start()
        if (!isClosed) {
            mViewBinding.root.setBackgroundResource(com.au.module_androidcolor.R.drawable.switch_btn_opened)
        } else {
            mViewBinding.root.setBackgroundResource(com.au.module_androidcolor.R.drawable.switch_btn_closed)
        }
    }
}