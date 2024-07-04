package com.au.module_android.permissions.permission

import android.view.View
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContract
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import com.au.module_android.utils.asOrNull

/**
 * @author au
 * @date :2023/12/13 10:52
 * @description:
 */
abstract class IPermissionResult<I, O> (cxt:LifecycleOwner): DefaultLifecycleObserver {
    private var onResultCallback: ActivityResultCallback<O>? = null

    var launcher: ActivityResultLauncher<I>? = null
        private set

    abstract val resultContract:ActivityResultContract<I, O>

    private var isObserved = false

    init {
        initObserver(cxt)
    }

    override fun onCreate(owner: LifecycleOwner) {
        super.onCreate(owner)
        createLauncher(owner)
    }

    override fun onResume(owner: LifecycleOwner) {
        super.onResume(owner)
        createLauncher(owner)
    }

    override fun onDestroy(owner: LifecycleOwner) {
        super.onDestroy(owner)
        launcher?.unregister()
        clearLauncher()
    }

    private fun initObserver(owner:LifecycleOwner) {
        var isAlreadyResumed = false
        if (!isObserved) {
            if (owner is Fragment) {
                isAlreadyResumed = owner.lifecycle.currentState == Lifecycle.State.RESUMED
                owner.lifecycle.addObserver(this)
            } else if (owner is AppCompatActivity) {
                isAlreadyResumed = owner.lifecycle.currentState == Lifecycle.State.RESUMED
                owner.lifecycle.addObserver(this)
            } else if (owner is View) {
                val activity = owner.context.asOrNull<AppCompatActivity>()
                if (activity != null) {
                    isAlreadyResumed = activity.lifecycle.currentState == Lifecycle.State.RESUMED
                    activity.lifecycle.addObserver(this)
                } else {
                    throw IllegalArgumentException("init at onCreate $owner is not illegal.")
                }
            }
            isObserved = true
        }

        if (isAlreadyResumed) {
            createLauncher(owner)
        }
    }

    private fun createLauncher(owner:LifecycleOwner) {
        if (launcher != null) {
            if (owner is Fragment) {
                launcher = owner.registerForActivityResult(resultContract, getOnResultCallback())
            } else if (owner is AppCompatActivity) {
                launcher = owner.registerForActivityResult(resultContract, getOnResultCallback())
            }
        }
    }

    fun clearLauncher() {
        launcher = null
    }

    protected fun setOnResultCallback(callback: ActivityResultCallback<O>) {
        onResultCallback = callback
    }

    protected fun getOnResultCallback(): ActivityResultCallback<O> {
        return onResultCallback ?: ActivityResultCallback {  }
    }
}