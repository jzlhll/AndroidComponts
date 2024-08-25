package com.au.module_android.ui.views

import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import androidx.annotation.CallSuper
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

    private lateinit var _realRoot: LinearLayout

    private lateinit var _toolbar: CustomToolbar

    override val realRoot: LinearLayout
        get() = _realRoot

    override val toolbar: CustomToolbar
        get() = _toolbar

    @CallSuper
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val v = onUiCreateView(layoutInflater, null, savedInstanceState)
        root = v
        if (hasToolbar()) {
            val toolbarToLL = createToolbarLayout(layoutInflater.context, v)

            _realRoot = toolbarToLL.second
            _toolbar = toolbarToLL.first

            setContentView(toolbarToLL.second)
        } else {
            setContentView(v)
        }
    }

}