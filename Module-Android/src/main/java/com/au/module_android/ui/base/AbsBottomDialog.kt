package com.au.module_android.ui.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

@Deprecated("基础框架的一环，请使用BindingXXXDialog或者ViewXXXDialog")
abstract class AbsBottomDialog : BottomSheetDialogFragment(), IDialogFragment {
    final override val dialogWindowEnterMode = DialogEnterAnimMode.None

    private var mContentView:View? = null
    private var mRootView:ViewGroup? = null

    final override var rootView: ViewGroup?
        get() = mRootView
        set(value) {mRootView = value}
    final override var contentView: View?
        get() = mContentView
        set(value) {mContentView = value}

    final override fun self() = this

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return onCreateViewSelf(inflater, container, savedInstanceState)
    }
}