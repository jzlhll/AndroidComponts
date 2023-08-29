package com.au.module_android.arct

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding

/**
 * @author allan.jiang
 * Date: 2023/7/10
 * Description 基础Fragment的通用
 */
abstract class BaseBindingFragment<VB:ViewBinding> : BaseViewFragment() {
    lateinit var binding:VB

    final override fun onCommonCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val vb = ICommon.createViewBinding(javaClass, inflater, container, false) as VB
        binding = vb
        return vb.root
    }
}