package com.au.module_android.comp

import android.content.res.Resources
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner

/**
 * 指导合并Fragment，activity，Dialog等主界面生命周期
 */
interface ICommon {
    /**
     * 提供Dialog、Activity、Fragment、等组件的创建view
     */
    fun onCommonCreateView(inflater: LayoutInflater,
                         container: ViewGroup? = null,
                         savedInstanceState: Bundle? = null) : View

    /**
     * 当view创建完毕
     */
    fun onCommonAfterCreateView(
        owner: LifecycleOwner,
        savedInstanceState: Bundle?,
        resources: Resources
    )

    /**
     * 检查是否是竖屏的。
     */
    fun checkScreenRotationIsPort(resources: Resources): Boolean {
        val metrics = resources.displayMetrics
        return metrics.widthPixels < metrics.heightPixels
    }
}