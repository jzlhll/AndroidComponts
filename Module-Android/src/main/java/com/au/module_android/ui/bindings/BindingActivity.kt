package com.au.module_android.ui.bindings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.CallSuper
import androidx.viewbinding.ViewBinding
import com.au.module_android.ui.base.IUiViewBinding
import com.au.module_android.ui.base.AbsActivity
import com.au.module_android.ui.createViewBinding

/**
 * @author au
 * Date: 2023/7/4
 * Description 指导基础类模板
 */
abstract class BindingActivity<VB: ViewBinding> : AbsActivity(), IUiViewBinding<VB> {
    lateinit var binding:VB

    @CallSuper
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val v = onCreatingView(layoutInflater, null, savedInstanceState)
        setContentView(v)
        afterViewCreated(savedInstanceState, binding)
    }

    final override fun onCreatingView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val vb = createViewBinding(javaClass, inflater, container, false) as VB
        binding = vb
        return vb.root
    }
}