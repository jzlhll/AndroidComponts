package com.au.module_android.ui.bindings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewbinding.ViewBinding
import com.au.module_android.ui.base.AbsBottomDialog
import com.au.module_android.ui.createViewBinding

open class BindingBottomDialog<VB:ViewBinding>(hasEditText:Boolean = false)
        : AbsBottomDialog(hasEditText) {
    lateinit var binding:VB private set
    private var _onBindingInitBlock:((binding: VB) -> Unit)? = null

    @Deprecated("please call setOnBindingInitBlock")
    override fun setOnInitUiBlock(block: (rootView: View) -> Unit) {
        throw IllegalAccessException("please call setOnBindingInitBlock")
    }

    /**
     * 在创建dialog后立刻调用。否则可能错过他的执行.
     * 只用作初始化一些界面。
     */
    fun setOnBindingInitBlock(block : (binding: VB) -> Unit) {
        _onBindingInitBlock = block
    }

    override fun onCreatingView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val vb = createViewBinding(javaClass, inflater, container, false) as VB
        binding = vb
        return vb.root
    }
}