package com.au.module_android.ui.bindings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.EmptySuper
import androidx.viewbinding.ViewBinding
import com.au.module_android.ui.createViewBinding
import com.au.module_android.ui.views.ViewFragment

/**
 * @author au
 * Date: 2023/7/10
 * Description 基础Fragment的通用
 */
abstract class BindingNoToolbarFragment<VB: ViewBinding> : ViewFragment() {
    lateinit var binding:VB private set

    final override fun onUiCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val vb = createViewBinding(javaClass, inflater, container, false) as VB
        binding = vb
        onBindingCreated(savedInstanceState)
        return vb.root
    }

    @EmptySuper
    open fun onBindingCreated(savedInstanceState: Bundle?) {}
}