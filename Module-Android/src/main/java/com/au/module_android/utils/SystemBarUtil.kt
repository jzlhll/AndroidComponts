package com.au.module_android.utils

import android.app.Activity
import android.graphics.Color
import android.graphics.Point
import android.os.Build
import android.view.Window
import android.view.WindowInsets
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.fragment.app.Fragment

fun transparentStatusBar(fragment: Fragment,
    isBlackStatusBarTextColor: Boolean? = null,
    isBlackNavigationBarTextColor: Boolean? = null,
    insetsBlock: (
        insets: WindowInsetsCompat,
        statusBarsHeight: Int,
        navigationBarHeight: Int
    ) -> WindowInsetsCompat = {insets, _, _ -> insets}
) {
    val window = fragment.activity?.window
    if (window != null) {
        transparentStatusBar(window, isBlackStatusBarTextColor, isBlackNavigationBarTextColor, insetsBlock)
    }
}

fun transparentStatusBar(activity: Activity,
    isBlackStatusBarTextColor: Boolean? = null,
    isBlackNavigationBarTextColor: Boolean? = null,
    insetsBlock: (
        insets: WindowInsetsCompat,
        statusBarsHeight: Int,
        navigationBarHeight: Int
    ) -> WindowInsetsCompat = {insets, _, _ -> insets}
) {
    transparentStatusBar(activity.window, isBlackStatusBarTextColor, isBlackNavigationBarTextColor, insetsBlock)
}

/**
 * 透明状态栏, 必定做全屏；然后设置参数，修改文字颜色。
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
    //设置状态栏颜色透明
    window.statusBarColor = Color.TRANSPARENT
    //处理状态栏文字颜色
    if (isBlackStatusBarTextColor != null || isBlackNavigationBarTextColor != null) {
        WindowCompat.getInsetsController(window, window.decorView).apply {
            if (isBlackStatusBarTextColor != null) {
                isAppearanceLightStatusBars = isBlackStatusBarTextColor
            }
            if (isBlackNavigationBarTextColor != null) {
                isAppearanceLightNavigationBars = isBlackNavigationBarTextColor
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
 * 获取屏幕尺寸
 * isOnlyDisplay:true 不计算状态栏 也不计算navigationBar
 */
fun Activity.getScreenSize(displayMode:Int, portrait:Boolean = true): Point {
    return window.getScreenSize(displayMode, portrait)
}

/**
 * 获取屏幕尺寸
 * @param displayMode : 0 获取屏幕的高度； 1 抛掉statusBar高度；2抛掉statusBar和navBar高度。
 */
fun Window.getScreenSize(displayMode:Int, portrait:Boolean = true): Point {
    val point = Point()
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        val metrics = windowManager.currentWindowMetrics
        val bounds = metrics.bounds
        point.x = bounds.width()
        point.y = bounds.height()
        if (displayMode > 0) {
            val windowInsets = metrics.windowInsets
            val insets = windowInsets.getInsetsIgnoringVisibility(WindowInsets.Type.navigationBars() or WindowInsets.Type.displayCutout())
            //需要减去状态栏和导航栏高度
            if (portrait) {
                point.y -= insets.top
                if(displayMode == 2) point.y -= insets.bottom
            } else {
                point.x = bounds.width() - insets.left
                if(displayMode == 2) point.y -= insets.right
            }
        }
    } else {
        if (displayMode == 0) {
            windowManager.defaultDisplay.getSize(point)
        } else {
            windowManager.defaultDisplay?.getRealSize(point) //todo 低版本没有实现 mode=1的情况。
        }
    }
    return point
}

fun Window.getScreenSizeWithStatusAndNavHeight(displayMode:Int, portrait:Boolean = true): Triple<Point, Int, Int> {
    val point = Point()
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        val metrics = windowManager.currentWindowMetrics
        val bounds = metrics.bounds
        point.x = bounds.width()
        point.y = bounds.height()
        if (displayMode > 0) {
            val windowInsets = metrics.windowInsets
            val insets = windowInsets.getInsetsIgnoringVisibility(WindowInsets.Type.navigationBars() or WindowInsets.Type.displayCutout())
            //需要减去状态栏和导航栏高度
            if (portrait) {
                point.y -= insets.top
                if(displayMode == 2) point.y -= insets.bottom
                return Triple(point, insets.top, insets.bottom)
            } else {
                point.x = bounds.width() - insets.left
                if(displayMode == 2) point.y -= insets.right
                return Triple(point, insets.left, insets.right)
            }
        }
        return Triple(point, 0, 0)
    } else {
        if (displayMode == 0) {
            windowManager.defaultDisplay.getSize(point)
        } else {
            windowManager.defaultDisplay?.getRealSize(point) //todo 低版本没有实现 mode=1的情况。
        }
        return Triple(point, 0, 0) //todo 低版本没有实现 mode=1的情况。
    }
}