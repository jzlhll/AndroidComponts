package com.au.module_android.permissions.permission

interface IMultiPermissionsResult : IPermissionResult<Map<String, @JvmSuppressWildcards Boolean>> {
    fun permissions():Array<String>
}