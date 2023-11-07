package com.au.module_android.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewbinding.ViewBinding

/**
 * @author allan
 * Date: 2023/7/10
 * Description 基础Fragment的通用
 */
abstract class AbsBindingFragment<VB:ViewBinding> : AbsFragment(), IUiViewBinding<VB> {
    lateinit var binding:VB

    final override fun creatingView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val vb = IUi.createViewBinding(javaClass, inflater, container, false) as VB
        binding = vb
        return vb.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        onAfterCreatedViewBinding(savedInstanceState, binding)
    }
}