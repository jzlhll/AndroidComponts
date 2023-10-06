package com.au.module_android.permissions

import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityOptionsCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner

/**
 * 当初始化完成这个对象后，请在onCreate里面调用 函数（onCreate）即可
 */
internal class OwnerForResult<I, O>
    private constructor(private val resultContract: ActivityResultContract<I, O>) : ActivityResultCallback<O>,
            DefaultLifecycleObserver,
            IPermission<I, O> {

    companion object {
        fun createForResult(mode:OwnerResultMode) : IPermission<*, *>{
            val resultContract = when (mode) {
                OwnerResultMode.MultiPermission -> ActivityResultContracts.RequestMultiplePermissions()
                OwnerResultMode.Permission -> ActivityResultContracts.RequestPermission()
                OwnerResultMode.StartActivity -> ActivityResultContracts.StartActivityForResult()
            }

            return OwnerForResult(resultContract)
        }

        fun <I, O> createForResult2(resultContract: ActivityResultContract<I, O>) : IPermission<I, O>{
            return OwnerForResult(resultContract)
        }
    }

    private var launcher: ActivityResultLauncher<I>? = null
    private var onResultCallback: ((O) -> Unit)? = null

    override fun initAtOnCreate(activity: AppCompatActivity) {
        activity.lifecycle.addObserver(this)
        launcher = activity.registerForActivityResult(resultContract, this)
    }

    override fun initAtOnCreate(fragment: Fragment) {
        fragment.lifecycle.addObserver(this)
        launcher = fragment.registerForActivityResult(resultContract, this)
    }

    override fun onActivityResult(result: O) {
        onResultCallback?.invoke(result)
    }

    override fun onDestroy(owner: LifecycleOwner) {
        super.onDestroy(owner)
        onResultCallback = null
        launcher?.unregister()
        launcher = null
    }

    /**
     * 启动activity
     */
    override fun start(intent: I, option: ActivityOptionsCompat?, onResultCallback: (O) -> Unit
    ) {
        this.onResultCallback = onResultCallback
        launcher?.launch(intent, option)
    }
}