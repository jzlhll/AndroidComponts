package com.au.module_android.permissions.permission

import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityOptionsCompat
import androidx.lifecycle.LifecycleOwner
import com.au.module_android.permissions.hasPermission

internal class PermissionsForResult(cxt:LifecycleOwner,
                                    permissions: Array<String>)
        : IMultiPermissionsResult(permissions, cxt) {
    override val resultContract = ActivityResultContracts.RequestMultiplePermissions()

    override fun safeRun(block: () -> Unit, notGivePermissionBlock: (() -> Unit)?, option: ActivityOptionsCompat?) {
        if (hasPermission(*permissions)) {
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
            launcher?.launch(permissions, option)
        }
    }
}