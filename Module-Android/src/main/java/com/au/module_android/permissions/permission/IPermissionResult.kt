package com.au.module_android.permissions.permission

import com.au.module_android.permissions.IResult

interface IPermissionResult : IResult<Boolean> {
    fun permission():String
}