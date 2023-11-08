package com.au.module_android.ui.bindings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewbinding.ViewBinding
import com.au.module_android.ui.base.IUiViewBinding
import com.au.module_android.ui.base.BaseFragment
import com.au.module_android.ui.createViewBinding

/**
 * @author allan
 * Date: 2023/7/10
 * Description 基础Fragment的通用
 */
abstract class BindingFragment<VB:ViewBinding> : BaseFragment(), IUiViewBinding<VB> {
    lateinit var binding:VB

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return onCreatingView(layoutInflater, null, savedInstanceState)
    }

    final override fun onCreatingView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val vb = createViewBinding(javaClass, inflater, container, false) as VB
        binding = vb
        return vb.root
    }

    final override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        afterViewCreated(savedInstanceState, binding)
    }
}