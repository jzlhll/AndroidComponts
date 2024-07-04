package com.au.module_android.permissions.activity

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.core.app.ActivityOptionsCompat

abstract class IActivityResult {
    private var resultCallback:(ActivityResultCallback<ActivityResult>)? = null
    private val resultCallbackWrap = ActivityResultCallback<ActivityResult> {
        resultCallback?.onActivityResult(it)
    }

    fun setOnResultCallback(callback: ActivityResultCallback<ActivityResult>) {
        resultCallback = callback
    }

    fun getOnResultCallback(): ActivityResultCallback<ActivityResult> {
        return resultCallbackWrap
    }

    abstract fun start(intent: Intent, callback:ActivityResultCallback<ActivityResult>? = null, option: ActivityOptionsCompat? = null)

    fun jumpToAppDetail(appContext: Context, callback:(ActivityResult)->Unit) {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        intent.data = Uri.fromParts("package", appContext.packageName, null)
        setOnResultCallback(callback)
        start(intent, null)
    }
}