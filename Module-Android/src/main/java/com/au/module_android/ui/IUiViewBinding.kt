package com.au.module_android.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.viewbinding.ViewBinding

/**
 * @author allan.jiang
 * @date :2023/10/8 17:40
 * @description:
 */
interface IUiViewBinding<T:ViewBinding> {

    /**
     * 当view创建完毕。
     */
    fun onAfterCreatedViewBinding(
        owner: LifecycleOwner,
        savedInstanceState: Bundle?,
        viewBinding: T
    ) {}
}