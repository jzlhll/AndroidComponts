package com.au.module_android.permissions.permission

import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityOptionsCompat
import androidx.lifecycle.LifecycleOwner
import com.au.module_android.permissions.hasPermission

internal class PermissionForResult(cxt: LifecycleOwner,
                                   permission: String) :
    IOnePermissionResult(permission, cxt) {
    override val resultContract: ActivityResultContract<String, Boolean> = ActivityResultContracts.RequestPermission()

    override fun safeRun(block: () -> Unit, notGivePermissionBlock: (() -> Unit)?, option: ActivityOptionsCompat?) {
        if(hasPermission(permission)) {
            block.invoke()
        } else {
            setOnResultCallback {
                if(it) block.invoke() else notGivePermissionBlock?.invoke()
            }
            launcher?.launch(permission, option)
        }
    }
}