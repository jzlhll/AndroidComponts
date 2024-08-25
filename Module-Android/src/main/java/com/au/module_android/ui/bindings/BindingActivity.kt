package com.au.module_android.ui.bindings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewbinding.ViewBinding
import com.au.module_android.ui.base.IUi
import com.au.module_android.ui.createViewBinding
import com.au.module_android.ui.toolbar.IHasToolbar
import com.au.module_android.ui.views.ViewToolbarActivity

/**
 * @author au
 * Date: 2023/7/4
 * Description 指导基础类模板
 */
abstract class BindingActivity<VB: ViewBinding> : ViewToolbarActivity(), IUi, IHasToolbar {
    lateinit var binding:VB private set

    final override fun onUiCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val vb = createViewBinding(javaClass, inflater, container, false) as VB
        binding = vb
        return vb.root
    }
}