package com.au.module_android.permissions.permission

import android.view.View
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityOptionsCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import com.au.module_android.utils.asOrNull
import java.lang.IllegalArgumentException

internal class PermissionForResult(cxt:LifecycleOwner,
                                   private val permission: String,
                                   private var onResultCallback: ActivityResultCallback<Boolean>?= null) :
            IOnePermissionResult {

    private val resultContract: ActivityResultContract<String, Boolean> = ActivityResultContracts.RequestPermission()
    private var launcher: ActivityResultLauncher<String>? = null
    init {
        if (cxt is Fragment) {
            cxt.lifecycle.addObserver(this)
        } else if (cxt is AppCompatActivity) {
            cxt.lifecycle.addObserver(this)
        } else if (cxt is View) {
            val activity = cxt.context.asOrNull<AppCompatActivity>()
            if (activity != null) {
                activity.lifecycle.addObserver(this)
            } else {
                throw IllegalArgumentException("init at onCreate $cxt is not illegal.")
            }
        }
    }

    override fun onCreate(owner: LifecycleOwner) {
        super.onCreate(owner)
        if (owner is Fragment) {
            launcher = owner.registerForActivityResult(resultContract, getOnResultCallback())
        } else if (owner is AppCompatActivity) {
            launcher = owner.registerForActivityResult(resultContract, getOnResultCallback())
        }
    }

    override fun permission() = permission
    override fun setOnResultCallback(callback: ActivityResultCallback<Boolean>) {
        onResultCallback = callback
    }

    override fun getOnResultCallback(): ActivityResultCallback<Boolean> {
        return onResultCallback ?: ActivityResultCallback {  }
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

internal class PermissionsForResult(cxt:Any,
                                    private val permissions: Array<String>,
                                    private var onResultCallback: ActivityResultCallback<Map<String, Boolean>>?=null)
        : IMultiPermissionsResult {

    private var launcher: ActivityResultLauncher<Array<String>>? = null

    private val resultContract = ActivityResultContracts.RequestMultiplePermissions()

    init {
        if (cxt is Fragment) {
            cxt.lifecycle.addObserver(this)
        } else if (cxt is AppCompatActivity) {
            cxt.lifecycle.addObserver(this)
        } else if (cxt is View) {
            val activity = cxt.context.asOrNull<AppCompatActivity>()
            if (activity != null) {
                activity.lifecycle.addObserver(this)
            } else {
                throw IllegalArgumentException("init at onCreate $cxt is not illegal.")
            }
        }
    }

    override fun onCreate(owner: LifecycleOwner) {
        super.onCreate(owner)
        if (owner is Fragment) {
            launcher = owner.registerForActivityResult(resultContract, getOnResultCallback())
        } else if (owner is AppCompatActivity) {
            launcher = owner.registerForActivityResult(resultContract, getOnResultCallback())
        }
    }

    override fun permissions() = permissions
    override fun setOnResultCallback(callback: ActivityResultCallback<Map<String, Boolean>>) {
        onResultCallback = callback
    }

    override fun getOnResultCallback(): ActivityResultCallback<Map<String, Boolean>> {
        return onResultCallback ?: ActivityResultCallback {  }
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