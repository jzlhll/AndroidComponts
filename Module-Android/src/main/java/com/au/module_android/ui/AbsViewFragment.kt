package com.au.module_android.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

/**
 * @author allan.jiang
 * Date: 2023/7/10
 * Description 基础Fragment的通用
 */
abstract class AbsViewFragment : AbsFragment(), IUiView {
    lateinit var root:View

    final override fun creatingView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return onCreatingView(inflater, container, savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        root = view
        onAfterCreatedView(this, savedInstanceState, view)
    }
}