package com.au.module_android.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

/**
 * @author allan.jiang
 * Date: 2023/7/4
 * Description 指导基础类模板
 */
abstract class AbsViewActivity : AbsActivity(), IUiView{
    lateinit var root:View

    final override fun creatingView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val v = onCreatingView(inflater, container, savedInstanceState)
        root = v
        onAfterCreatedView(this, savedInstanceState, v)
        return v
    }
}