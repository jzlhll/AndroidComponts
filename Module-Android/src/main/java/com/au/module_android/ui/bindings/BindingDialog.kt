package com.au.module_android.ui.bindings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewbinding.ViewBinding
import com.au.module_android.ui.base.AbsDialog
import com.au.module_android.ui.base.DialogMode
import com.au.module_android.ui.createViewBinding

open class BindingDialog<VB:ViewBinding>(mode: DialogMode = DialogMode.Center)
        : AbsDialog(mode){
    lateinit var binding:VB

    private var _onBindingInitBlock:((binding: VB) -> Unit)? = null
    /**
     * 在创建dialog后立刻调用。否则可能错过他的执行.
     * 只用作初始化一些界面。
     */
    fun setOnBindingInitBlock(block : (binding: VB) -> Unit) {
        _onBindingInitBlock = block
    }

    final override fun onCreatingView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val vb = createViewBinding(javaClass, inflater, container, false) as VB
        binding = vb
        _onBindingInitBlock?.invoke(vb)
        _onBindingInitBlock = null
        return vb.root
    }
}