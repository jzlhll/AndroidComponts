package com.au.module_android.permissions

import android.app.Activity
import android.content.Context
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
abstract class IContractResult<I, O> (cxt:Any, resultContract:ActivityResultContract<I, O>) {
    private var onResultCallback: ActivityResultCallback<O>? = null

    private val mResultCallback = ActivityResultCallback<O> { result -> onResultCallback?.onActivityResult(result) }

    val launcher: ActivityResultLauncher<I> = when (cxt) {
        is Fragment -> {
            cxt.registerForActivityResult(resultContract, mResultCallback)
        }

        is AppCompatActivity -> {
            cxt.registerForActivityResult(resultContract, mResultCallback)
        }

        is View -> {
            val activity = cxt.context.asOrNull<AppCompatActivity>()
            activity?.registerForActivityResult(resultContract, mResultCallback) ?: throw IllegalArgumentException("init at onCreate $cxt is not illegal.")
        }

        else -> {
            throw IllegalArgumentException("init at onCreate $cxt is not illegal.")
        }
    }

    /**
     * 要求在launch之前调用
     */
    protected fun setResultCallback(callback: ActivityResultCallback<O>) {
        onResultCallback = callback
    }

    internal fun Any.toContext():Context {
        if (this is Fragment) {
            return requireContext()
        }
        if (this is Activity) {
            return this
        }
        throw RuntimeException()
    }
}