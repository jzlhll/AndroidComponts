package com.au.module_android.ui.views

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.au.module_android.ui.base.BaseCenterDialog
import com.au.module_android.ui.base.IUiView

abstract class ViewCenterDialog : BaseCenterDialog(), IUiView {
    final override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val root = super.onCreateView(inflater, container, savedInstanceState)
        afterViewCreated(savedInstanceState, root)
        return root
    }
}