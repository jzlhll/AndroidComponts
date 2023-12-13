package com.au.module_android.permissions.permission

import com.au.module_android.permissions.IResult

interface IMultiPermissionsResult : IResult {
    var onResultCallback:((Map<String, @JvmSuppressWildcards Boolean>) -> Unit)?
    fun permissions():Array<String>
}