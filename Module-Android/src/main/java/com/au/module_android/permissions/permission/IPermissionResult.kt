package com.au.module_android.permissions.permission

interface IPermissionResult : IPermissionBaseResult {
    /**
     * context指代的是Activity，Fragment，或者View
     */
    fun initAtOnCreate(context: Any)

    var onResultCallback:((Boolean) -> Unit)?

    val permission:String?
}