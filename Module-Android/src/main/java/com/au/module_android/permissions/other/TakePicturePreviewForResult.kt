package com.au.module_android.permissions.other

import android.graphics.Bitmap
import android.view.View
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityOptionsCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import com.au.module_android.permissions.permission.IPermissionResult
import com.au.module_android.utils.asOrNull
import java.lang.IllegalArgumentException

/**
 * @author au
 * @date :2023/12/13 11:35
 * @description:
 */
class TakePicturePreviewForResult (context: Any,
                                   private var onResultCallback: ActivityResultCallback<Bitmap?> ?= null)
        : IPermissionResult<Bitmap?> {
    private var launcher: ActivityResultLauncher<Void?>? = null

    private val resultContract = ActivityResultContracts.TakePicturePreview()

    init {
        if (context is Fragment) {
            context.lifecycle.addObserver(this)
        } else if (context is AppCompatActivity) {
            context.lifecycle.addObserver(this)
        } else if (context is View) {
            val activity = context.context.asOrNull<AppCompatActivity>()
            if (activity != null) {
                activity.lifecycle.addObserver(this)
            } else {
                throw IllegalArgumentException("init at onCreate $context is not illegal.")
            }
        }
    }

    override fun onCreate(owner: LifecycleOwner) {
        super.onCreate(owner)
        if (owner is Fragment) {
            launcher = owner.registerForActivityResult(resultContract, getOnResultCallback())
        } else if (owner is AppCompatActivity) {
            launcher = owner.registerForActivityResult(resultContract, getOnResultCallback())
        }
    }

    override fun setOnResultCallback(callback: ActivityResultCallback<Bitmap?>) {
        onResultCallback = callback
    }

    override fun getOnResultCallback(): ActivityResultCallback<Bitmap?> {
        return onResultCallback ?: ActivityResultCallback {  }
    }

    override fun start(option: ActivityOptionsCompat?) {
        launcher?.launch(null, option)
    }

    override fun onDestroy(owner: LifecycleOwner) {
        super.onDestroy(owner)
        launcher?.unregister()
        launcher = null
    }
}