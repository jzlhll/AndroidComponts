package com.au.module_android.permissions.permission

import android.view.View
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityOptionsCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.au.module_android.utils.asOrNull
import java.lang.IllegalArgumentException

internal class PermissionForResult(private val permission: String,
                                   private var onResultCallback: ActivityResultCallback<Boolean>?= null) :
    DefaultLifecycleObserver,
    IPermissionResult {
    private val resultContract: ActivityResultContract<String, Boolean> = ActivityResultContracts.RequestPermission()
    private var launcher: ActivityResultLauncher<String>? = null
    override fun permission() = permission
    override fun setOnResultCallback(callback: ActivityResultCallback<Boolean>) {
        onResultCallback = callback
    }

    override fun getOnResultCallback(): ActivityResultCallback<Boolean> {
        return onResultCallback ?: ActivityResultCallback {  }
    }

    override fun initAtOnCreate(context: Any) {
        if (context is Fragment) {
            context.lifecycle.addObserver(this)
            launcher = context.registerForActivityResult(resultContract, getOnResultCallback())
        } else if (context is AppCompatActivity) {
            context.lifecycle.addObserver(this)
            launcher = context.registerForActivityResult(resultContract, getOnResultCallback())
        } else if (context is View) {
            val activity = context.context.asOrNull<AppCompatActivity>()
            if (activity != null) {
                activity.lifecycle.addObserver(this)
                launcher = activity.registerForActivityResult(resultContract, getOnResultCallback())
            } else {
                throw IllegalArgumentException("init at onCreate $context is not illegal.")
            }
        }
    }

    override fun start(option: ActivityOptionsCompat?) {
        launcher?.launch(permission, option)
    }

    override fun onDestroy(owner: LifecycleOwner) {
        super.onDestroy(owner)
        launcher?.unregister()
        launcher = null
    }
}

internal class PermissionsForResult(private val permissions: Array<String>,
                                    private var onResultCallback: ActivityResultCallback<Map<String, Boolean>>?=null)
    : DefaultLifecycleObserver,
            IMultiPermissionsResult {

    override fun permissions() = permissions
    override fun setOnResultCallback(callback: ActivityResultCallback<Map<String, Boolean>>) {
        onResultCallback = callback
    }

    override fun getOnResultCallback(): ActivityResultCallback<Map<String, Boolean>> {
        return onResultCallback ?: ActivityResultCallback {  }
    }

    private var launcher: ActivityResultLauncher<Array<String>>? = null

    private val resultContract = ActivityResultContracts.RequestMultiplePermissions()

    override fun initAtOnCreate(context: Any) {
        if (context is Fragment) {
            context.lifecycle.addObserver(this)
            launcher = context.registerForActivityResult(resultContract, getOnResultCallback())
        } else if (context is AppCompatActivity) {
            context.lifecycle.addObserver(this)
            launcher = context.registerForActivityResult(resultContract, getOnResultCallback())
        } else if (context is View) {
            val activity = context.context.asOrNull<AppCompatActivity>()
            if (activity != null) {
                activity.lifecycle.addObserver(this)
                launcher = activity.registerForActivityResult(resultContract, getOnResultCallback())
            } else {
                throw IllegalArgumentException("init at onCreate $context is not illegal.")
            }
        }
    }

    override fun onDestroy(owner: LifecycleOwner) {
        super.onDestroy(owner)
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