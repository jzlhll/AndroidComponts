package com.au.module_android.permissions.permission

import androidx.activity.result.contract.ActivityResultContract
import androidx.core.app.ActivityOptionsCompat
import com.au.module_android.permissions.IContractResult

abstract class IMultiPermissionsResult(val permissions:Array<String>,
                                       cxt: Any,
                                       contract: ActivityResultContract<Array<String>, Map<String, @JvmSuppressWildcards Boolean>>) :
    IContractResult<Array<String>, Map<String, @JvmSuppressWildcards Boolean>>(cxt, contract) {
    /**
     * 使用
     * createMultiPermissionForResult(permissions)
     * createPermissionForResult(permission)
     * 创建，不用传入第二参数。
     *
     * 因为block放在了这里设置。
     */
    abstract fun safeRun(block:()->Unit, notGivePermissionBlock:(()->Unit)? = null, option: ActivityOptionsCompat? = null)
}