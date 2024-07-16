package com.au.module_android.permissions.permission

import android.view.View
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContract
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.au.module_android.utils.asOrNull

/**
 * @author au
 * @date :2023/12/13 10:52
 */
abstract class IPermissionResult<I, O> (cxt:Any, resultContract:ActivityResultContract<I, O>) {
    private var onResultCallback: ActivityResultCallback<O>? = null

    private val mResultCallback = ActivityResultCallback<O> { result -> onResultCallback?.onActivityResult(result) }

    val launcher: ActivityResultLauncher<I> = if (cxt is Fragment) {
        cxt.registerForActivityResult(resultContract, mResultCallback)
    } else if (cxt is AppCompatActivity) {
        cxt.registerForActivityResult(resultContract, mResultCallback)
    } else if (cxt is View) {
        val activity = cxt.context.asOrNull<AppCompatActivity>()
        activity?.registerForActivityResult(resultContract, mResultCallback) ?: throw IllegalArgumentException("init at onCreate $cxt is not illegal.")
    } else {
        throw IllegalArgumentException("init at onCreate $cxt is not illegal.")
    }

    /**
     * 要求在launch之前调用
     */
    protected fun setResultCallback(callback: ActivityResultCallback<O>) {
        onResultCallback = callback
    }
}