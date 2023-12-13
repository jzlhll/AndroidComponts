package com.au.module_android.permissions.permission

import android.view.View
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContract
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityOptionsCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.au.module_android.utils.asOrNull
import java.lang.IllegalArgumentException

internal class PermissionForResult(private val permission: String,
                                   private val resultContract: ActivityResultContract<String, Boolean>,
                                   override var onResultCallback: ((Boolean) -> Unit)?)
        : ActivityResultCallback<Boolean>,
    DefaultLifecycleObserver,
    IPermissionResult {

    private var launcher: ActivityResultLauncher<String>? = null
    override fun permission() = permission

    override fun initAtOnCreate(context: Any) {
        if (context is Fragment) {
            context.lifecycle.addObserver(this)
            launcher = context.registerForActivityResult(resultContract, this)
        } else if (context is AppCompatActivity) {
            context.lifecycle.addObserver(this)
            launcher = context.registerForActivityResult(resultContract, this)
        } else if (context is View) {
            val activity = context.context.asOrNull<AppCompatActivity>()
            if (activity != null) {
                activity.lifecycle.addObserver(this)
                launcher = activity.registerForActivityResult(resultContract, this)
            } else {
                throw IllegalArgumentException("init at onCreate $context is not illegal.")
            }
        }
    }

    override fun start(option: ActivityOptionsCompat?) {
        launcher?.launch(permission, option)
    }

    override fun onActivityResult(result: Boolean) {
        onResultCallback?.invoke(result)
    }

    override fun onDestroy(owner: LifecycleOwner) {
        super.onDestroy(owner)
        onResultCallback = null
        launcher?.unregister()
        launcher = null
    }
}

internal class PermissionsForResult(private val permissions: Array<String>,
                                    private val resultContract: ActivityResultContract<Array<String>, Map<String, @JvmSuppressWildcards Boolean>>,
                                    override var onResultCallback: ((Map<String, @JvmSuppressWildcards Boolean>) -> Unit)?)
    : ActivityResultCallback<Map<String, @JvmSuppressWildcards Boolean>>,
            DefaultLifecycleObserver,
            IMultiPermissionsResult {

    override fun permissions() = permissions

    private var launcher: ActivityResultLauncher<Array<String>>? = null

    override fun initAtOnCreate(context: Any) {
        if (context is Fragment) {
            context.lifecycle.addObserver(this)
            launcher = context.registerForActivityResult(resultContract, this)
        } else if (context is AppCompatActivity) {
            context.lifecycle.addObserver(this)
            launcher = context.registerForActivityResult(resultContract, this)
        } else if (context is View) {
            val activity = context.context.asOrNull<AppCompatActivity>()
            if (activity != null) {
                activity.lifecycle.addObserver(this)
                launcher = activity.registerForActivityResult(resultContract, this)
            } else {
                throw IllegalArgumentException("init at onCreate $context is not illegal.")
            }
        }
    }

    override fun onActivityResult(result: Map<String, @JvmSuppressWildcards Boolean>) {
        onResultCallback?.invoke(result)
    }

    override fun onDestroy(owner: LifecycleOwner) {
        super.onDestroy(owner)
        onResultCallback = null
        launcher?.unregister()
        launcher = null
    }

    /**
     * 启动activity
     */
    override fun start(option: ActivityOptionsCompat?) {
        launcher?.launch(permissions, option)
    }
}