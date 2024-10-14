package com.au.module_android.ui.views

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.CallSuper
import com.au.module_android.ui.ToolbarManager
import com.au.module_android.ui.base.AbsFragment
import com.au.module_android.ui.base.IUi
import com.au.module_android.ui.toolbar.IHasToolbar
import com.au.module_android.ui.toolbar.createToolbarLayout
import com.au.module_android.widget.CustomToolbar

/**
 * @author au
 * Date: 2023/7/10
 * Description 基础Fragment的通用
 */
abstract class ViewToolbarFragment : AbsFragment(), IUi, IHasToolbar {
    lateinit var root: View

    private var _realRoot: ViewGroup? = null

    private var _toolbar: CustomToolbar? = null

    private var _toolbarMgr: ToolbarManager? = null

    final override val realRoot: ViewGroup?
        get() = _realRoot

    final override val toolbar: CustomToolbar?
        get() = _toolbar

    final override val toolbarManager: ToolbarManager?
        get() = _toolbarMgr

    @CallSuper
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v = onUiCreateView(layoutInflater, null, savedInstanceState)
        root = v

        val info = toolbarInfo()
        if (info != null) {
            if(info.title != null) requireActivity().title = info.title //before create toolbar

            val toolbarToLL = createToolbarLayout(layoutInflater.context, v, info.hasBackIcon)

            _realRoot = toolbarToLL.second
            _toolbar = toolbarToLL.first

            if (info.menuBean != null) {
                _toolbarMgr = ToolbarManager(this, info.menuBean).also {
                    if(info.menuBean.showWhenOnCreate) it.showMenu()
                }
            }

            return toolbarToLL.second
        } else {
            return v
        }
    }
}