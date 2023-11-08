package com.au.module_android.ui

import android.os.Bundle
import android.view.View
import androidx.annotation.CallSuper
import com.au.module_android.ui.base.BaseActivity

/**
 * @author allan
 * Date: 2023/7/4
 * Description 指导基础类模板
 */
abstract class AbsViewActivity : BaseActivity(), IUiView {
    lateinit var root:View

    @CallSuper
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val v = onCreatingView(layoutInflater, null, savedInstanceState)
        setContentView(v)
        afterViewCreated(savedInstanceState, v)
    }
}