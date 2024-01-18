package com.au.module_android.utils

import android.app.Activity
import android.graphics.Color
import android.os.Build
import android.util.Log
import android.view.View
import android.view.WindowInsets
import android.view.WindowManager
import androidx.annotation.ColorInt
import androidx.annotation.RequiresApi
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat


/*** 设置状态栏和导航栏的颜色和状态栏文本模式
 * 兼容到android12的导航栏和状态栏的设置
 * @param navigationBarColor 底部导航栏颜色，
 * @param isNavigationBarLightMode 是否是黑色按钮导航栏
 *
 * 如果需要设置透到顶部和底部，则设置对应的颜色为Color.TRANSPARENT
 */
@Suppress("DEPRECATION")
fun Activity.systemBarTranslate(
    @ColorInt statusBarColor: Int?,
    isStatusBarLightMode: Boolean = true,
    @ColorInt navigationBarColor: Int?,
    isNavigationBarLightMode: Boolean = false,
    block:((statusBarHeight:Int, navigationBarHeight:Int)->Unit)? = null
) {
    Log.d("tyiot_app", "systemBarTranslate: $statusBarColor $isStatusBarLightMode, $navigationBarColor $isNavigationBarLightMode")
    val decorView = window.decorView
    window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
    if (Build.VERSION.SDK_INT in
        arrayOf(Build.VERSION_CODES.LOLLIPOP, Build.VERSION_CODES.LOLLIPOP_MR1)
    ) window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
    if (statusBarColor == Color.TRANSPARENT) {
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.R -> {
                WindowCompat.setDecorFitsSystemWindows(window, false)
                Log.d("tyiot_app", "systemBarTranslate setOnApplyWindowInsetsListener: $statusBarColor $isStatusBarLightMode, $navigationBarColor $isNavigationBarLightMode")
                decorView.setOnApplyWindowInsetsListener { view, insets ->
                    val navigationBars = insets.getInsets(WindowInsetsCompat.Type.navigationBars())
                    val statusBars = insets.getInsets(WindowInsetsCompat.Type.statusBars())
                    block?.invoke(statusBars.top, navigationBars.bottom)
                    WindowInsets.CONSUMED //最好不要让同一个activity里面的各种fragment或者View再次监听，避免混乱。
                    //因为肯定超过api20.则肯定不会为null
                    // view.findViewById<View>(android.R.id.content).updatePadding(bottom = navigationBars.bottom)
                    //val insetsCompat = WindowInsetsCompat.toWindowInsetsCompat(insets)
                    //ViewCompat.onApplyWindowInsets(view, insetsCompat).toWindowInsets()!!
                }
            }
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ->
                changeStatusBarMode(isStatusBarLightMode)
            else -> window.setFlags(
                WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,
                WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
            )
        }
    }

    when {
        Build.VERSION.SDK_INT < Build.VERSION_CODES.R -> {
            decorView.systemUiVisibility = decorView.systemUiVisibility or
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
                decorView.systemUiVisibility =
                    decorView.systemUiVisibility or View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
                if (!isNavigationBarLightMode) {
                    decorView.systemUiVisibility =
                        decorView.systemUiVisibility xor View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
                }
            }
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R && navigationBarColor == Color.TRANSPARENT) {
                decorView.systemUiVisibility =
                    decorView.systemUiVisibility or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            }
        }
        else -> with(WindowInsetsControllerCompat(window, decorView)) {
            isAppearanceLightStatusBars = isStatusBarLightMode
            isAppearanceLightNavigationBars =
                if (navigationBarColor == Color.TRANSPARENT) true else isNavigationBarLightMode
            show(WindowInsetsCompat.Type.systemBars())
        }
    }
    statusBarColor?.let { window.statusBarColor = it }
    navigationBarColor?.let { window.navigationBarColor = it }
}

/** 修改状态栏上文字图标的颜色 **/
@RequiresApi(Build.VERSION_CODES.M)
private fun Activity.changeStatusBarMode(isLightMode: Boolean) = with(window.decorView) {
    systemUiVisibility = systemUiVisibility or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
    if (!isLightMode) {
        systemUiVisibility = systemUiVisibility xor View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
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