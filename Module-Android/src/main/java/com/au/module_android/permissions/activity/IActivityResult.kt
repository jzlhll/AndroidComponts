package com.au.module_android.permissions.activity

import android.content.Intent
import androidx.activity.result.ActivityResult
import androidx.core.app.ActivityOptionsCompat
import com.au.module_android.permissions.IResult

interface IActivityResult : IResult{
    var onResultCallback:((ActivityResult) -> Unit)?

    @Deprecated("ActivityResult please use start(intent, options)",
        ReplaceWith("throw IllegalAccessException(\"ActivityResult please use start(intent, options)\")")
    )
    override fun start(option: ActivityOptionsCompat?) {
        throw IllegalAccessException("ActivityResult please use start(intent, options)")
    }

    fun start(intent: Intent, option: ActivityOptionsCompat?)
}