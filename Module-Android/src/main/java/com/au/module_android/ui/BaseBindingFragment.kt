package com.au.module_android.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewbinding.ViewBinding

/**
 * @author allan.jiang
 * Date: 2023/7/10
 * Description 基础Fragment的通用
 */
abstract class BaseBindingFragment<VB:ViewBinding> : BaseViewFragment() {
    lateinit var binding:VB

    final override fun onCreatingView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val vb = IUi.createViewBinding(javaClass, inflater, container, false) as VB
        binding = vb
        return vb.root
    }
}