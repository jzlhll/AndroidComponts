package com.au.module_android.ui.bindings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewbinding.ViewBinding
import com.au.module_android.ui.base.AbsBottomDialog
import com.au.module_android.ui.base.IUiViewBinding
import com.au.module_android.ui.createViewBinding

abstract class BindingBottomHeightDialog<VB:ViewBinding> : BindingBottomDialog<VB>(){
    override fun afterViewCreated(savedInstanceState: Bundle?, viewBinding: VB) {

    }
}