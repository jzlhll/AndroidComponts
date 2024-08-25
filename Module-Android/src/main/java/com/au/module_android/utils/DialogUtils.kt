package com.au.module_android.utils

import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.withStateAtLeast
import kotlinx.coroutines.launch

/**
 * 确保生命周期【Started】运行
 */
fun DialogFragment.showWhenStated(
    fragment: Fragment,
    tag: String? = this::class.java.simpleName,
    block: ()->Unit
) {
    fragment.lifecycleScope.launch {
        withStateAtLeast(Lifecycle.State.STARTED) {
            show(fragment.childFragmentManager, tag)
        }
    }
    lifecycle.addObserver(observer = object : DefaultLifecycleObserver {
        override fun onStart(owner: LifecycleOwner) {
            block()
        }
    })
}