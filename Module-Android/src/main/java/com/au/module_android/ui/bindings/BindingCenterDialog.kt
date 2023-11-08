package com.au.module_android.ui.bindings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewbinding.ViewBinding
import com.au.module_android.ui.base.BaseCenterDialog
import com.au.module_android.ui.base.IUiViewBinding
import com.au.module_android.ui.createViewBinding

abstract class BindingCenterDialog<VB:ViewBinding> : BaseCenterDialog(), IUiViewBinding<VB>{
    lateinit var binding:VB

    final override fun onCreatingView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val vb = createViewBinding(javaClass, inflater, container, false) as VB
        binding = vb
        afterViewCreated(savedInstanceState, vb)
        return vb.root
    }
}