package com.au.module_android.ui.base

/**
 * @author allan
 * @date :2024/8/27 10:38
 * @description: 如果activity想要实现全屏就实现IFullWindow
 * 并在onCreate里面将contentView调用setEdgeToEdge(this, contentView)
 */
interface IFullWindow {
    /**
     * 是否将顶部statusBar padding到合理的位置
     */
    fun isPaddingStatusBar() = true

    /**
     * 底部navigationBar padding到合理的位置
     */
    fun isPaddingNavBar() = true

    /**
     * android15已经默认要求设置了。这里就强制处理。
     */
    fun fullWindowSetEdgeToEdge() = true
}