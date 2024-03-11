package com.au.module_android.ui.base

import android.os.Bundle
import android.view.View
import androidx.annotation.EmptySuper

/**
 * @author au
 * @date :2023/10/8 17:40
 * @description:
 */
interface IUiView: IUi {
    /**
     * 当view创建完毕。
     * 替代fragment的onViewCreated的生命周期的位置
     */
    @EmptySuper
    fun afterViewCreated(
        savedInstanceState: Bundle?,
        rootView: View
    ) {}
}