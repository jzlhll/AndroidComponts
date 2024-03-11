package com.au.module_android.permissions.other

import android.net.Uri
import android.view.View
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityOptionsCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.au.module_android.permissions.IResult
import com.au.module_android.utils.asOrNull
import java.lang.IllegalArgumentException

/**
 * @author au
 * @date :2023/12/13 11:35
 * @description:
 * @param getPictureFileUri 请使用AndroidUtils的方法来实现.getPictureFileUri.
 */
class TakePictureForResult (private val getPictureFileUri: Uri,
                            private var onResultCallback: ActivityResultCallback<Boolean>? = null)
        : DefaultLifecycleObserver, IResult<Boolean> {

    private var launcher: ActivityResultLauncher<Uri?>? = null

    private val resultContract = ActivityResultContracts.TakePicture()

    override fun getOnResultCallback(): ActivityResultCallback<Boolean> {
        return onResultCallback ?: ActivityResultCallback { }
    }

    override fun setOnResultCallback(callback: ActivityResultCallback<Boolean>) {
        onResultCallback = callback
    }

    override fun initAtOnCreate(context: Any) {
        if (context is Fragment) {
            context.lifecycle.addObserver(this)
            launcher = context.registerForActivityResult(resultContract, getOnResultCallback())
        } else if (context is AppCompatActivity) {
            context.lifecycle.addObserver(this)
            launcher = context.registerForActivityResult(resultContract, getOnResultCallback())
        } else if (context is View) {
            val activity = context.context.asOrNull<AppCompatActivity>()
            if (activity != null) {
                activity.lifecycle.addObserver(this)
                launcher = activity.registerForActivityResult(resultContract, getOnResultCallback())
            } else {
                throw IllegalArgumentException("init at onCreate $context is not illegal.")
            }
        }
    }

    override fun start(option: ActivityOptionsCompat?) {
        launcher?.launch(getPictureFileUri, option)
    }

    override fun onDestroy(owner: LifecycleOwner) {
        super.onDestroy(owner)
        launcher?.unregister()
        launcher = null
    }
}