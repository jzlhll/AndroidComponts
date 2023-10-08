package com.au.module_android.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewbinding.ViewBinding
import com.au.module_android.ui.IUi.Companion.createViewBinding

/**
 * @author allan.jiang
 * Date: 2023/7/4
 * Description 指导基础类模板
 */
abstract class AbsBindingActivity<VB: ViewBinding> : AbsViewActivity() {
    lateinit var binding:VB

    override fun onCreatingView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val vb = createViewBinding(javaClass, inflater, container, false) as VB
        binding = vb
        return vb.root
    }
}