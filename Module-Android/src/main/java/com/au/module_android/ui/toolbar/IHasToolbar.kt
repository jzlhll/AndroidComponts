package com.au.module_android.ui.toolbar

import android.widget.LinearLayout
import com.au.module_android.ui.ToolbarManager
import com.google.android.material.appbar.MaterialToolbar

/**
 * @author allan
 * @date :2024/8/21 14:17
 * @description:
 */
interface IHasToolbar {
    fun hasToolbar():Boolean = false

    fun hasToolbarManager() : ToolbarManager.MenuBean? = null

    /**
     * 如果hasToolbar()=true，则会从基类创建LinearLayout。
     * 如果false，则这个返回null。只有你自己的ui的root。
     */
    val realRoot: LinearLayout?

    /**
     * hasToolbar() = true 才会有。
     */
    val toolbar : MaterialToolbar?

    /**
     * hasToolbarManager()!=null 才会有。
     */
    val toolbarManager:ToolbarManager?
}