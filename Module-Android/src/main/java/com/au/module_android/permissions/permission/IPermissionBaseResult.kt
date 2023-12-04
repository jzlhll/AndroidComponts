package com.au.module_android.permissions.permission

import androidx.core.app.ActivityOptionsCompat

interface IPermissionBaseResult {
    fun start(
        option: ActivityOptionsCompat?
    )
}