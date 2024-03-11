package com.au.module_android.ui.base

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window

interface IBaseDialog {
    /**
     * 查找到可以用于toast的ViewGroup。
     */
    fun findToastViewGroup() : ViewGroup?

    val window: Window?

    var rootView: View?

    var createdDialog: Dialog?

    /**
     * 提供Dialog、Activity、Fragment、等组件的创建view
     */
    fun onCreatingView(inflater: LayoutInflater,
                       container: ViewGroup? = null,
                       savedInstanceState: Bundle? = null) : View

}