package com.au.module_android.permissions

import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityOptionsCompat
import androidx.fragment.app.Fragment
import com.au.module_android.utils.ALog

interface IPermission<I, O> {
    fun initAtOnCreate(activity: AppCompatActivity)

    fun initAtOnCreate(fragment: Fragment)

    fun initAtOnCreate(view : View) {
        when (val context = view.context) {
            is AppCompatActivity -> initAtOnCreate(context)
            else -> {
                Log.d(ALog.TAG, "no view's context? or not activity?")
            }
        }
    }

    fun start(
        intent: I,
        option: ActivityOptionsCompat?
    )

    var onResultCallback:((O) -> Unit)?
}