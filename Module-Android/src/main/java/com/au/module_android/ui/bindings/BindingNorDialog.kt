package com.au.module_android.ui.bindings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewbinding.ViewBinding
import com.au.module_android.ui.base.AbsNorDialog
import com.au.module_android.ui.base.DialogMode
import com.au.module_android.ui.base.IBaseDialog
import com.au.module_android.ui.createViewBinding

open class BindingNorDialog<VB:ViewBinding, D:IBaseDialog>(mode: DialogMode = DialogMode.Center)
        : AbsNorDialog<D>(mode){
    lateinit var binding:VB

    final override fun onCreatingView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val vb = createViewBinding(javaClass, inflater, container, false) as VB
        binding = vb
        return vb.root
    }
}