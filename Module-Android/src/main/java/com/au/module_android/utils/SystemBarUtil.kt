package com.au.module_android.utils

import android.app.Activity
import android.graphics.Color
import android.os.Build
import android.view.Window
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.window.layout.WindowMetricsCalculator

//参考资料
//https://developer.android.google.cn/develop/ui/views/layout/edge-to-edge?hl=zh-cn
//https://developer.android.google.cn/develop/ui/views/layout/immersive?hl=zh-cn
//https://developer.android.google.cn/develop/ui/views/layout/insets/rounded-corners?hl=zh-cn
//https://developer.android.google.cn/develop/ui/views/layout/edge-to-edge-manually?hl=zh-cn

/**
 * 谨慎使用：activity和fragment已经通过基础框架默认限定实现；现在只需要在Dialog或者特殊临时切换调用
 * 如果是Activity或者Fragment，子类覆盖isPaddingNavBar=false则会让navBar透下去，isPaddingStatusBar=false则会让statusBar透上去。
 *
 * 透明状态栏, 必定做全屏；然后设置参数，修改文字颜色。
 * null 代码会自动检测app的uiMode。一般不要去传参，保持null。
 * true bar显示白色文案（即黑暗模式）。false就bar显示黑色文案（即亮白模式）。
 */
fun transparentStatusBar(window: Window,
                            isBlackStatusBarTextColor: Boolean? = null,
                            isBlackNavigationBarTextColor: Boolean? = null,
                            insetsBlock: (
                                insets: WindowInsetsCompat,
                                statusBarsHeight: Int,
                                navigationBarHeight: Int
                            ) -> WindowInsetsCompat = {insets, _, _ -> insets}
) {
    WindowCompat.setDecorFitsSystemWindows(window, false)
    window.statusBarColor = Color.TRANSPARENT
    window.navigationBarColor = Color.TRANSPARENT
    if (Build.VERSION.SDK_INT >= 29) {
        window.isStatusBarContrastEnforced = false
        window.isNavigationBarContrastEnforced = true
    }

    val wicc = WindowInsetsControllerCompat(window, window.decorView)
    val statusBarDark = isBlackStatusBarTextColor ?: DarkModeUtil.detectDarkMode(window.context)
    val navBarDark = isBlackNavigationBarTextColor ?: DarkModeUtil.detectDarkMode(window.context)
    wicc.isAppearanceLightStatusBars = !statusBarDark
    wicc.isAppearanceLightNavigationBars = !navBarDark

    //预留导航栏的空间
    ViewCompat.setOnApplyWindowInsetsListener(window.decorView) { _, insets ->
        insetsBlock.invoke(
            insets,
            insets.getInsets(WindowInsetsCompat.Type.statusBars()).top,
            insets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom
        )
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