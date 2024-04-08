package com.au.module_android.permissions.permission

interface IOnePermissionResult : IPermissionResult<Boolean> {
    fun permission():String
}