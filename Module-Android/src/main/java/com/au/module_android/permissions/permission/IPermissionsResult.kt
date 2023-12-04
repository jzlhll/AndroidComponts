package com.au.module_android.permissions.permission

interface IPermissionsResult : IPermissionBaseResult {
    /**
     * context指代的是Activity，Fragment，或者View
     */
    fun initAtOnCreate(context: Any)

    var onResultCallback:((Map<String, @JvmSuppressWildcards Boolean>) -> Unit)?

    val permissions:Array<String>
}