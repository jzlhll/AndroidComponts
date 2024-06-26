package com.au.module_android.permissions.permission

import androidx.activity.result.ActivityResultCallback
import androidx.core.app.ActivityOptionsCompat
import androidx.lifecycle.DefaultLifecycleObserver
import com.au.module_android.permissions.hasPermission

/**
 * @author au
 * @date :2023/12/13 10:52
 * @description:
 */
interface IPermissionResult<O> : DefaultLifecycleObserver {
    fun setOnResultCallback(callback:(ActivityResultCallback<O>))
    fun getOnResultCallback() : (ActivityResultCallback<O>)

    fun start(option: ActivityOptionsCompat?)

    /**
     * 使用
     * createMultiPermissionForResult(permissions)
     * createPermissionForResult(permission)
     * 创建，不用传入第二参数。
     *
     * 因为block放在了这里设置。
     */
    fun safeRun(block:()->Unit, notGivePermissionBlock:(()->Unit)? = null) {
        when (this) {
            is PermissionForResult -> {
                if(hasPermission(permission())) {
                    block.invoke()
                } else {
                    setOnResultCallback {
                        if(it) block.invoke() else notGivePermissionBlock?.invoke()
                    }
                    this.start(null)
                }
            }

            is PermissionsForResult -> {
                if (hasPermission(*permissions())) {
                    block.invoke()
                } else {
                    setOnResultCallback {
                        var hasPermission = false
                        for (entry in it) {
                            if (!entry.value) {
                                hasPermission = false
                                break
                            } else {
                                hasPermission = true
                            }
                        }
                        if(hasPermission) block.invoke() else notGivePermissionBlock?.invoke()
                    }
                    this.start(null)
                }
            }
        }
    }
}