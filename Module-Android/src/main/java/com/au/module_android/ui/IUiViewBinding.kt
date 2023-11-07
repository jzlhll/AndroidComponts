package com.au.module_android.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.EmptySuper
import androidx.lifecycle.LifecycleOwner
import androidx.viewbinding.ViewBinding

/**
 * @author allan
 * @date :2023/10/8 17:40
 * @description:
 */
interface IUiViewBinding<T:ViewBinding> {

    /**
     * 当view创建完毕。
     */
    @EmptySuper
    fun onAfterCreatedViewBinding(
        savedInstanceState: Bundle?,
        viewBinding: T
    ) {}
}