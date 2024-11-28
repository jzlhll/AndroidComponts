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
    private var mViewBinding = BlocksSwitchLayoutBinding.inflate(LayoutInflater.from(context), this)

    private var _isInit = false
    val isInit: Boolean
        get() = _isInit

    /**
     * 默认就是关闭
     */
    private var _isClosed = true
    val isClosed: Boolean
        get() = _isClosed

    /**
     * 是否阻止
     */
    var abort = false

    /**
     * 点击切换的回调函数。
     *
     */
    var valueCallback : ((isClosed:Boolean)->Unit)? = null
    fun initValue(close: Boolean) {
        _isInit = true
        if (!close) {
            _isClosed = false
            post { //直接delay处理，初始化为非左边即可。
                mViewBinding.selectBgView.translationX = (width - mViewBinding.selectBgView.width).toFloat()
            }
        }
    }

    fun setValue(close: Boolean) {
        if (!_isInit) throw RuntimeException()
        if (_isClosed == close) {
            return
        }
        this._isClosed = close
        handleAnimal()
    }

    private fun initView() {
        mViewBinding.root.onClick {
            if (!abort) {
                val newIsClosed = !_isClosed
                setValue(newIsClosed)
                valueCallback?.invoke(newIsClosed)
            }
        }
    }

    init {
        initView()
    }

    private fun handleAnimal() {
        val bgAnimator: ObjectAnimator
        val newIsClosed = _isClosed
        bgAnimator = if (newIsClosed) {
            ObjectAnimator.ofFloat(mViewBinding.selectBgView, "translationX", 0f)
        } else {
            ObjectAnimator.ofFloat(
                mViewBinding.selectBgView, "translationX",
                0f, (width - mViewBinding.selectBgView.width).toFloat()
            )
        }
        bgAnimator.duration = 160
        bgAnimator.start()
    }
}