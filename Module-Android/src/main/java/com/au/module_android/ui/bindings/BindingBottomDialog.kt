package com.au.module_android.ui.bindings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewbinding.ViewBinding
import com.au.module_android.ui.base.AbsBottomDialog
import com.au.module_android.ui.base.IBaseDialog
import com.au.module_android.ui.createViewBinding

open class BindingBottomDialog<VB:ViewBinding, D:IBaseDialog>(hasEditText:Boolean = false)
        : AbsBottomDialog<D>(hasEditText) {
    lateinit var binding:VB

    override fun onCreatingView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val vb = createViewBinding(javaClass, inflater, container, false) as VB
        binding = vb
        return vb.root
    }
}