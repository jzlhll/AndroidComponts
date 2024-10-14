package com.au.module_android.ui.views

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.annotation.CallSuper
import com.au.module_android.ui.ToolbarManager
import com.au.module_android.ui.base.AbsActivity
import com.au.module_android.ui.base.IUi
import com.au.module_android.widget.CustomToolbar
import com.au.module_android.ui.toolbar.IHasToolbar
import com.au.module_android.ui.toolbar.createToolbarLayout

/**
 * @author au
 * Date: 2023/7/4
 * Description 指导基础类模板
 */
abstract class ViewToolbarActivity : AbsActivity(), IUi, IHasToolbar {
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
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val v = onUiCreateView(layoutInflater, null, savedInstanceState)
        root = v

        val info = toolbarInfo()
        if (info != null) {
            if(info.title != null) this.title = info.title //before create toolbar

            val toolbarToLL = createToolbarLayout(layoutInflater.context, v, info.hasBackIcon)

            _realRoot = toolbarToLL.second
            _toolbar = toolbarToLL.first

            if (info.menuBean != null) {
                _toolbarMgr = ToolbarManager(this, info.menuBean).also {
                    if(info.menuBean.showWhenOnCreate) it.showMenu()
                }
            }

            setContentView(toolbarToLL.second)
        } else {
            setContentView(v)
        }
    }

}