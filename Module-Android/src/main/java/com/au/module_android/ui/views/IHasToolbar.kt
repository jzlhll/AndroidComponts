package com.au.module_android.ui.views

import android.widget.RelativeLayout
import com.au.module_android.ui.ToolbarManager
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.progressindicator.CircularProgressIndicator

/**
 * @author allan
 * @date :2024/8/21 14:17
 * @description:
 */
interface IHasToolbar {
    fun toolbarInfo() : ToolbarInfo? = null

    /**
     * 如果toolbarInfo()!=null，则会从基类创建ViewGroup(RelativeLayout)。
     * 如果false，则这个返回null。只有你自己的ui的root。
     */
    val realRoot: RelativeLayout?

    /**
     * 是否存在基础容器。上面有toolbar下面是content
     */
    fun hasRealRoot():Boolean = realRoot != null

    /**
     * hasToolbar() = true 才会有。
     */
    val toolbar : MaterialToolbar?

    val indicator:CircularProgressIndicator?

    /**
     * hasToolbarManager()!=null 才会有。
     */
    val toolbarManager:ToolbarManager?
}