package com.au.module_android.ui.views

import android.os.Bundle
import android.view.View
import androidx.annotation.CallSuper
import com.au.module_android.ui.base.AbsActivity
import com.au.module_android.ui.base.IUi

/**
 * @author au
 * Date: 2023/7/4
 * Description 指导基础类模板
 */
abstract class ViewActivity : AbsActivity(), IUi {
    lateinit var root: View

    @CallSuper
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val v = onUiCreateView(layoutInflater, null, savedInstanceState)
        setContentView(v)
        root = v
    }
}