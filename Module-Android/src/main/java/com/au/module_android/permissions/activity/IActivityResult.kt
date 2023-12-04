package com.au.module_android.permissions.activity

import android.content.Intent
import androidx.activity.result.ActivityResult
import androidx.core.app.ActivityOptionsCompat

interface IActivityResult {
    /**
     * context指代的是Activity，Fragment，或者View
     */
    fun initAtOnCreate(context: Any) {}

    fun start(
        intent: Intent,
        option: ActivityOptionsCompat?
    )

    var onResultCallback:((ActivityResult) -> Unit)?
}