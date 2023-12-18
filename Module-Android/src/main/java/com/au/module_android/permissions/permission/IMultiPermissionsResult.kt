package com.au.module_android.permissions.permission

import com.au.module_android.permissions.IResult

interface IMultiPermissionsResult : IResult<Map<String, @JvmSuppressWildcards Boolean>> {
    fun permissions():Array<String>
}