package com.au.module_android.ui

import android.graphics.drawable.Drawable
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import com.au.module_android.ui.toolbar.IHasToolbar
import com.au.module_android.utils.asOrNull
import com.google.android.material.appbar.MaterialToolbar

/**
 * @author au
 * 1. 处理toolbar的title左或者中；
 * 2. 处理是否显示右侧的menu列表
 *
 * menuXml参考：
 * <menu xmlns:android="http://schemas.android.com/apk/res/android"
 *     xmlns:app="http://schemas.android.com/apk/res-auto">
 *     <item
 *         android:id="@+id/close"
 *         app:showAsAction="always"
 *         android:title="@string/skip"/>
 * </menu>
 */
open class ToolbarManager(private val fragment: Fragment,
                          private val menuXml: Int? = null,
                          private val onMenuItemBlock:((MenuItem)->Unit)? = null) {
    private var menuProvider: MenuProvider? = null

    private var navigationIcon: Drawable? = null

    /**
     * 请在框架viewToolbarFragment的，onViewCreate中调用
     * 或者在框架的viewToolbarActivity的onCreate中调用
     */
    fun showMenu() {
        menuXml ?: return
        val menuHost = fragment.requireActivity()
        if (menuProvider == null) {
            menuProvider = object : MenuProvider {
                override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                    menuInflater.inflate(menuXml, menu)
                }

                override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                    onMenuItemBlock?.invoke(menuItem)
                    return true
                }
            }
        }
        menuHost.addMenuProvider(menuProvider!!, fragment, Lifecycle.State.RESUMED)
    }

    /**
     * 隐藏右上角的menu菜单
     */
    fun hideMenu() {
        menuXml ?: return
        menuProvider?.let {
            val menuHost = fragment.requireActivity()
            menuHost.removeMenuProvider(it)
        }
    }

    /**
     * 设置toolbar title的对齐方式；默认就是居中的；因此可以不要调用。只有当想要居左, 才调用传入false
     */
    fun setTitleAlign(isCenter: Boolean) {
        val fragment = fragment.asOrNull<IHasToolbar>() ?: return
        fragment.toolbar.asOrNull<MaterialToolbar>()?.isTitleCentered = isCenter
    }

    /**
     * 隐藏不需要的返回键
     */
    fun hideNavigationIcon() {
        val uiHelper = fragment.asOrNull<IHasToolbar>() ?: return
        val toolbar = uiHelper.toolbar ?: return
        //进行备份
        navigationIcon = toolbar.navigationIcon
        toolbar.navigationIcon = null
    }

    /**
     * 显示出来。这个必须是备份了以后才有得使用的。
     */
    fun showNavigationIcon() {
        val uiHelper = fragment.asOrNull<IHasToolbar>() ?: return
        val toolbar = uiHelper.toolbar ?: return
        //使用备份的
        if (toolbar.navigationIcon == null && navigationIcon != null) {
            toolbar.navigationIcon = navigationIcon
        }
    }
}