package com.au.module_android.ui

import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.annotation.ColorInt
import androidx.appcompat.widget.Toolbar
import androidx.core.view.MenuProvider
import androidx.core.view.get
import androidx.core.view.size
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner

/**
 * @author allan.jiang
 * Date: 2022/12/29
 * Description
 * 1. 处理toolbar的title左或者中；
 * 2. 处理是否显示右侧的menu列表
 */
open class ToolbarMenuManager(private val owner: LifecycleOwner,
                              private val toolbar: Toolbar,
                              private val menuXml: Int? = null,
                              @ColorInt private val color:Int? = null,
                              private val onMenuItemBlock:((MenuItem)->Unit)? = null) {
    private var menuProvider: MenuProvider? = null

    /**
     * 请在onViewCreate中调用
     */
    fun showMenu() {
        menuXml ?: return
        if (menuProvider == null) {
            menuProvider = object : MenuProvider {
                override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                    menuInflater.inflate(menuXml, menu)
                    val c = color
                    if (c != null) {
                        // 遍历所有菜单项
                        for (i in 0 until menu.size) {
                            val menuItem = menu[i]
                            val spannable = SpannableString(menuItem.title)
                            spannable.setSpan(
                                ForegroundColorSpan(c),
                                0,
                                spannable.length,
                                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                            )
                            menuItem.title = spannable
                        }
                    }
                }

                override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                    onMenuItemBlock?.invoke(menuItem)
                    return true
                }
            }
        }
        toolbar.addMenuProvider(menuProvider!!, owner, Lifecycle.State.RESUMED)
    }

    /**
     * 隐藏右上角的menu菜单
     */
    fun hideMenu() {
        menuXml ?: return
        menuProvider?.let {
            toolbar.removeMenuProvider(it)
        }
    }
}