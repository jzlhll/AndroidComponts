package com.au.module_android.permissions.permission

import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityOptionsCompat
import com.au.module_android.permissions.hasPermission

internal class PermissionForResult(cxt: Any,
                                   permission: String) :
    IOnePermissionResult(permission, cxt, ActivityResultContracts.RequestPermission()) {

    override fun safeRun(block: () -> Unit, notGivePermissionBlock: (() -> Unit)?, option: ActivityOptionsCompat?) {
        if(hasPermission(permission)) {
            block.invoke()
        } else {
            setResultCallback {
                if(it) block.invoke() else notGivePermissionBlock?.invoke()
            }
            launcher.launch(permission, option)
        }
    }
}