package com.au.module_android.ui.base

import android.content.res.Resources
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

/**
 * 指导合并Fragment，activity，Dialog等主界面生命周期
 */
interface IUi {
    /**
     * 检查是否是竖屏的。
     */
    fun checkScreenRotationIsPort(resources: Resources): Boolean {
        val metrics = resources.displayMetrics
        return metrics.widthPixels < metrics.heightPixels
    }

    fun onUiCreateView(inflater: LayoutInflater,
                       container: ViewGroup? = null,
                       savedInstanceState: Bundle? = null) : View
}