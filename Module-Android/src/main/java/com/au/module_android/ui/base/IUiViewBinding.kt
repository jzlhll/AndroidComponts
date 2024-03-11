package com.au.module_android.ui.base

import android.os.Bundle
import androidx.annotation.EmptySuper
import androidx.viewbinding.ViewBinding

/**
 * @author au
 * @date :2023/10/8 17:40
 * @description:
 */
interface IUiViewBinding<T:ViewBinding> : IUi {

    /**
     * 当view创建完毕。
     */
    @EmptySuper
    fun afterViewCreated(
        savedInstanceState: Bundle?,
        viewBinding: T
    ) {}
}