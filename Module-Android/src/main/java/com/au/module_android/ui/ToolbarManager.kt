package com.au.module_android.ui

import android.app.Activity
import android.graphics.drawable.Drawable
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.activity.ComponentActivity
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import com.au.module_android.ui.toolbar.IHasToolbar
import com.au.module_android.ui.toolbar.MenuBean
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
open class ToolbarManager{
    private val fragmentOrActivity: LifecycleOwner
    private val activity: ComponentActivity
    private val menuBean: MenuBean?

    constructor(activity: ComponentActivity,
                menuBean:MenuBean? = null) {
        fragmentOrActivity = activity
        this.menuBean = menuBean
        this.activity = activity
    }

    constructor(fragment: Fragment,
                menuBean:MenuBean? = null) {
        fragmentOrActivity = fragment
        this.menuBean = menuBean
        this.activity = fragment.requireActivity()
    }


    private var menuProvider: MenuProvider? = null

    private var navigationIcon: Drawable? = null

    /**
     * 请在框架viewToolbarFragment的，onViewCreate中调用
     * 或者在框架的viewToolbarActivity的onCreate中调用
     */
    fun showMenu() {
        menuBean ?: return
        if (menuProvider == null) {
            menuProvider = object : MenuProvider {
                override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                    menuInflater.inflate(menuBean.menuXml, menu)
                }

                override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                    menuBean.onMenuItemBlock(menuItem)
                    return true
                }
            }
        }
        activity.addMenuProvider(menuProvider!!, fragmentOrActivity, Lifecycle.State.RESUMED)
    }

    /**
     * 隐藏右上角的menu菜单
     */
    fun hideMenu() {
        menuBean ?: return
        menuProvider?.let {
            activity.removeMenuProvider(it)
        }
    }

    /**
     * 设置toolbar title的对齐方式；默认就是居中的；因此可以不要调用。只有当想要居左, 才调用传入false
     */
    fun setTitleAlign(isCenter: Boolean) {
        val fragment = fragmentOrActivity.asOrNull<IHasToolbar>() ?: return
        fragment.toolbar.asOrNull<MaterialToolbar>()?.isTitleCentered = isCenter
    }

    /**
     * 隐藏不需要的返回键
     */
    fun hideNavigationIcon() {
        val uiHelper = fragmentOrActivity.asOrNull<IHasToolbar>() ?: return
        val toolbar = uiHelper.toolbar ?: return
        //进行备份
        navigationIcon = toolbar.navigationIcon
        toolbar.navigationIcon = null
    }

    /**
     * 显示出来。这个必须是备份了以后才有得使用的。
     */
    fun showNavigationIcon() {
        val uiHelper = fragmentOrActivity.asOrNull<IHasToolbar>() ?: return
        val toolbar = uiHelper.toolbar ?: return
        //使用备份的
        if (toolbar.navigationIcon == null && navigationIcon != null) {
            toolbar.navigationIcon = navigationIcon
        }
    }
}