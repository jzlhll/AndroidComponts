package com.au.module_android.ui.base

import com.au.module.android.BuildConfig

/**
 * @author allan
 * @date :2024/8/27 10:38
 * @description: 如果activity想要实现全屏就实现IFullWindow
 * 并在onCreate里面将contentView调用setEdgeToEdge(this, contentView)
 */
interface IFullWindow {
    /**
     * 由于新框架默认全面屏: 是否将顶部statusBar padding到合理的位置
     *
     *    所以子类不得再调用window.decorView
     *    ViewCompat.setOnApplyWindowInsetsListener(window.decorView)
     */
    fun isPaddingStatusBar() = BuildConfig.ENABLE_EDGE_TO_EDGE

    /**
     * 由于新框架默认全面屏: 是否将底部navigationBar padding到合理的位置
     *
     *    所以子类不得再调用window.decorView
     *    ViewCompat.setOnApplyWindowInsetsListener(window.decorView)
     */
    fun isPaddingNavBar() = BuildConfig.ENABLE_EDGE_TO_EDGE

    /**
     * android15已经默认要求设置了。这里就强制处理。
     */
    fun fullWindowSetEdgeToEdge() = true
}