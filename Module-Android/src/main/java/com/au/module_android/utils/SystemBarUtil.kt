package com.au.module_android.utils

import android.app.Activity
import android.graphics.Color
import android.view.Window
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.fragment.app.Fragment
import androidx.window.layout.WindowMetricsCalculator

//参考资料
//https://developer.android.google.cn/develop/ui/views/layout/edge-to-edge?hl=zh-cn
//https://developer.android.google.cn/develop/ui/views/layout/immersive?hl=zh-cn
//https://developer.android.google.cn/develop/ui/views/layout/insets/rounded-corners?hl=zh-cn
//https://developer.android.google.cn/develop/ui/views/layout/edge-to-edge-manually?hl=zh-cn

@Deprecated("谨慎使用：activity和fragment已经通过基础框架默认限定实现；现在只需要在Dialog或者特殊临时切换调用")
fun transparentStatusBar(fragment: Fragment,
    isBlackStatusBarTextColor: Boolean? = null,
    isBlackNavigationBarTextColor: Boolean? = null,
    force:Boolean = false,
    insetsBlock: (
        insets: WindowInsetsCompat,
        statusBarsHeight: Int,
        navigationBarHeight: Int
    ) -> WindowInsetsCompat = {insets, _, _ -> insets}
) {
    val window = fragment.activity?.window
    if (window != null) {
        transparentStatusBar(window, isBlackStatusBarTextColor, isBlackNavigationBarTextColor, force, insetsBlock)
    }
}

@Deprecated("谨慎使用：activity和fragment已经通过基础框架默认限定实现；现在只需要在Dialog或者特殊临时切换调用")
fun transparentStatusBar(activity: Activity,
    isBlackStatusBarTextColor: Boolean? = null,
    isBlackNavigationBarTextColor: Boolean? = null,
    force:Boolean = false,
    insetsBlock: (
        insets: WindowInsetsCompat,
        statusBarsHeight: Int,
        navigationBarHeight: Int
    ) -> WindowInsetsCompat = {insets, _, _ -> insets}
) {
    transparentStatusBar(activity.window, isBlackStatusBarTextColor, isBlackNavigationBarTextColor, force, insetsBlock)
}

/**
 * 透明状态栏, 必定做全屏；然后设置参数，修改文字颜色。
 * @param noForce 表示是否强制按照2个color来显示bar的文字颜色。默认false情况下，代码会结合当前是否是黑暗模式来处理
 */
@Deprecated("谨慎使用：activity和fragment已经通过基础框架默认限定实现；现在只需要在Dialog或者特殊临时切换调用")
fun transparentStatusBar(window: Window,
    isBlackStatusBarTextColor: Boolean? = null,
    isBlackNavigationBarTextColor: Boolean? = null,
    noForce:Boolean = false,
    insetsBlock: (
        insets: WindowInsetsCompat,
        statusBarsHeight: Int,
        navigationBarHeight: Int
    ) -> WindowInsetsCompat = {insets, _, _ -> insets}
) {
    var isBlackStatusBarTextColorFix = isBlackStatusBarTextColor
    var isBlackNavigationBarTextColorFix = isBlackNavigationBarTextColor
    if (!noForce) {
        val isDarkUi = DarkModeUtil().currentIfDark(window.context)
        if (!isDarkUi) {
            isBlackStatusBarTextColorFix = true
            isBlackNavigationBarTextColorFix = true
        } else {
            isBlackStatusBarTextColorFix = false
            isBlackNavigationBarTextColorFix = false
        }
    }

    //预留导航栏的空间
    ViewCompat.setOnApplyWindowInsetsListener(window.decorView) { _, insets ->
        insetsBlock.invoke(
            insets,
            insets.getInsets(WindowInsetsCompat.Type.statusBars()).top,
            insets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom
        )
    }
    //设置系统不要给状态栏和导航栏预留空间，否则无法透明状态栏 全屏传入false
    WindowCompat.setDecorFitsSystemWindows(window, false)
    //处理状态栏文字颜色
    if (isBlackStatusBarTextColorFix != null || isBlackNavigationBarTextColorFix != null) {
        WindowCompat.getInsetsController(window, window.decorView).apply {
            if (isBlackStatusBarTextColorFix != null) {
                isAppearanceLightStatusBars = isBlackStatusBarTextColorFix
            }
            if (isBlackNavigationBarTextColorFix != null) {
                isAppearanceLightNavigationBars = isBlackNavigationBarTextColorFix
            }
        }
    }
}

fun Activity.myHideSystemUI() {
    WindowCompat.setDecorFitsSystemWindows(window, false)
    WindowCompat.getInsetsController(window, window.decorView).let { controller ->
        controller.hide(WindowInsetsCompat.Type.systemBars())
        controller.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    }
}

fun Activity.myShowSystemUI() {
    WindowCompat.setDecorFitsSystemWindows(window, true)
    WindowCompat.getInsetsController(window, window.decorView).show(WindowInsetsCompat.Type.systemBars())
}

/**
 * 无需等待界面渲染成功，即在onCreate就可以调用，而且里面已经做了低版本兼容，感谢jetpack window库
 * 获取的就是整个屏幕的高度。包含了statusBar，navigationBar的高度一起。与wm size一致。
 * 这个方法100%可靠。虽然我们看api上描述说低版本是近似值，但是也是最接近最合理的值，不会是0的。
 */
fun Activity.getScreenFullSize() : Pair<Int, Int> {
    val m = WindowMetricsCalculator.getOrCreate().computeCurrentWindowMetrics(this)
    //computeMaximumWindowMetrics(this) 区别就是多屏，类似华为推上去的效果。不分屏就是一样的。
    return m.bounds.width() to m.bounds.height()
}

/**
 * 必须在activity已经完全渲染之后，一般地，我们是通过
 * ViewCompat.setOnApplyWindowInsetsListener(decorView) { _, insets ->
 *         val navHeight = insets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom
 *         val statusHeight = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top
 *
 *  来得到结果的。但是它并不一定会回调，必须调用WindowCompat.setDecorFitsSystemWindows(this, false)。
 *
 *  想要获取，要么，如上，使用transparentStatusBar的方法。
 *  要么，同View.post，再调用本函数获取。
 */
fun Activity.currentStatusBarAndNavBarHeight() : Pair<Int, Int>? {
    val insets = ViewCompat.getRootWindowInsets(window.decorView) ?: return null
    val nav = insets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom
    val sta = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top
    return sta to nav
}