package com.au.module_android.permissions.permission

import android.net.Uri
import com.au.module_android.permissions.IResult

interface IPermissionResult : IResult {
    var onResultCallback:((Uri) -> Unit)?

    fun permission():String
}