package com.au.module_android.permissions.activity

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.core.app.ActivityOptionsCompat
import com.au.module_android.permissions.IResult

interface IActivityResult : IResult<ActivityResult> {
    @Deprecated("ActivityResult please use start(intent, options)",
        ReplaceWith("throw IllegalAccessException(\"ActivityResult please use start(intent, options)\")")
    )
    override fun start(option: ActivityOptionsCompat?) {
        throw IllegalAccessException("ActivityResult please use start(intent, options)")
    }

    fun start(intent: Intent, option: ActivityOptionsCompat?)

    fun jumpToAppDetail(appContext: Context, callback:(ActivityResult)->Unit) {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        intent.data = Uri.fromParts("package", appContext.packageName, null)
        setOnResultCallback(callback)
        start(intent, null)
    }
}