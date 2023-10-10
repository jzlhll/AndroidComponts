package com.au.module_android.permissions

import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContract
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityOptionsCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner

/**
 * 当初始化完成这个对象后，请在onCreate里面调用 函数（onCreate）即可
 */
internal class OwnerForResult<I, O>(private val resultContract: ActivityResultContract<I, O>,
                                    override var onResultCallback:((O) -> Unit)?) : ActivityResultCallback<O>,
            DefaultLifecycleObserver,
            IPermission<I, O> {

    private var launcher: ActivityResultLauncher<I>? = null

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
    override fun start(intent: I, option: ActivityOptionsCompat?) {
        launcher?.launch(intent, option)
    }
}