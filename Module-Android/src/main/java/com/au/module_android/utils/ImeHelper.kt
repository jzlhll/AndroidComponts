package com.au.module_android.utils
import android.os.Build
import android.view.View
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.core.view.OnApplyWindowInsetsListener
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsAnimationCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner

/**
 * @author allan
 * @date :2024/3/5 14:01
 * @description:
 */
/**
 * 用于修复全屏状态下adjustResize不生效的问题,当弹出输入法时重新设定内容view的高度,使输入框正常显示
 */
class ImeHelper private constructor(private val activity: ComponentActivity) : DefaultLifecycleObserver {
    companion object {
        fun assist(activity: ComponentActivity) : ImeHelper? {
            activity.window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING)
            val instance = ImeHelper(activity)
            activity.lifecycle.addObserver(instance)
            return instance
        }
    }

    val decorView
        get() = activity.window.decorView

    private var _imeMaxHeight:Int = 0

    /**
     * 键盘变化监听器
     * 第一个参数：imeCurrentHeight
     * 第二个参数：imeMaxHeight
     * 第三个参数：statusBarHeight
     * 第四个参数：navigationBarHeight
     */
    private var imeChangeListener: ((Int, Int, Int, Int)->Unit)? = null
    private var onImeMaxHeightListener: ((Int)->Unit)? = null

    /**
     * 设置监听
     */
    fun setOnImeListener(
        listener: ((
            imeOffset: Int,
            imeMaxHeight: Int/*包含导航栏和状态栏总高度*/,
            statusBarHeight: Int,
            navigationBarHeight: Int
        ) -> Unit)?
    ) {
        this.imeChangeListener = listener
    }

    fun setOnImeMaxHeightListener(listener: ((Int)->Unit)?) {
        this.onImeMaxHeightListener = listener
    }

    fun calculate(rootHeight:Int,
                  moveView:View,
                  imeCurrentHeight: Int, imeMaxHeight: Int,
                  navigationBarHeight: Int, offset:Int = 10.dp) : Int?{
        val margin = rootHeight - moveView.bottom
        //1. 键盘的高度小于布局上剩余的高度，不用处理。因为够显示。
        if (imeMaxHeight <= margin) {
            return null
        }

        //2. 将moveView放在键盘上面
        val finalY = -(imeMaxHeight - margin + offset - navigationBarHeight)
        val percentage = imeCurrentHeight.toFloat() / imeMaxHeight
        return (finalY * percentage).toInt()
    }

    /**
     * 动画进行中的高度
     */
    private var _currentOffsetY = 0

    private val mAnimCallback = object : WindowInsetsAnimationCompat.Callback(DISPATCH_MODE_CONTINUE_ON_SUBTREE) {
        override fun onProgress(insets: WindowInsetsCompat, runningAnimations: MutableList<WindowInsetsAnimationCompat>): WindowInsetsCompat {
            val imeMaxHeight = _imeMaxHeight
            if (imeMaxHeight <= 0) {
                return insets
            }
            //键盘高度
            val imeCurrentHeight = insets.getInsets(WindowInsetsCompat.Type.ime()).bottom

            _currentOffsetY = imeCurrentHeight

            //状态栏高度
            val statusBarHeight = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top
            //导航栏高度
            val navigationBarHeight = insets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom
            imeChangeListener?.invoke(imeCurrentHeight, imeMaxHeight, statusBarHeight, navigationBarHeight)
            return insets
        }

        override fun onStart(
            animation: WindowInsetsAnimationCompat,
            bounds: WindowInsetsAnimationCompat.BoundsCompat
        ): WindowInsetsAnimationCompat.BoundsCompat {
            _imeMaxHeight = bounds.upperBound.bottom //动画结束后键盘有多高
            return super.onStart(animation, bounds)
        }
    }

    private val mApplyCallback = OnApplyWindowInsetsListener { v, insets ->
        val imeInsets = insets.getInsets(WindowInsetsCompat.Type.ime())
        val imeHeight = imeInsets.bottom
        // 检测高度变化
        if (imeHeight != _imeMaxHeight && imeHeight > 0) {
            _imeMaxHeight = imeHeight
            if(_currentOffsetY != 0) onImeMaxHeightListener?.invoke(_imeMaxHeight)
        }
        ViewCompat.onApplyWindowInsets(v, insets)
    }

    override fun onDestroy(owner: LifecycleOwner) {
        super.onDestroy(owner)
        this.imeChangeListener = null
        ViewCompat.setWindowInsetsAnimationCallback(decorView, null)
    }

    init {
        ViewCompat.setWindowInsetsAnimationCallback(decorView, mAnimCallback)
        ViewCompat.setOnApplyWindowInsetsListener(decorView, mApplyCallback)
    }
}
