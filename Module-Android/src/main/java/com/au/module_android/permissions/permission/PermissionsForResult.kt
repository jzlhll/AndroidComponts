package com.au.module_android.permissions.permission

import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityOptionsCompat
import com.au.module_android.permissions.hasPermission
import com.au.module_android.utils.logd

internal class PermissionsForResult(cxt:Any,
                                    permissions: Array<String>)
        : IMultiPermissionsResult(permissions, cxt, ActivityResultContracts.RequestMultiplePermissions()) {

    override fun safeRun(block: () -> Unit, notGivePermissionBlock: (() -> Unit)?, option: ActivityOptionsCompat?) {
        if (hasPermission(*permissions)) {
            block.invoke()
        } else {
            setResultCallback {
                var hasPermission = false
                for (entry in it) {
                    logd { "safe run entry $entry" }
                    if (!entry.value) {
                        hasPermission = false
                        break
                    } else {
                        hasPermission = true
                    }
                }
                if(hasPermission) block.invoke() else notGivePermissionBlock?.invoke()
            }
            launcher.launch(permissions, option)
        }
    }
}