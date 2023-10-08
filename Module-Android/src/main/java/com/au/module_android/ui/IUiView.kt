package com.au.module_android.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner

/**
 * @author allan.jiang
 * @date :2023/10/8 17:40
 * @description:
 */
interface IUiView {

    /**
     * onCommonCreateView的替代版。
     * 不想使用泛型。则泛型ViewBinding，使用ViewBinding。通过该函数传入View当做界面的root。
     */
    fun onCreatingView(inflater: LayoutInflater,
                              container: ViewGroup? = null,
                              savedInstanceState: Bundle? = null) : View

    /**
     * 当view创建完毕。
     * @param rootViewOrViewBinding 就是onCreatingView创建的view 或者 ViewBinding
     */
    fun onAfterCreatedView(
        owner: LifecycleOwner,
        savedInstanceState: Bundle?,
        rootView: View
    ) {}
}