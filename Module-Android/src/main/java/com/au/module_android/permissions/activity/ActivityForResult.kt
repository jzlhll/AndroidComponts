package com.au.module_android.permissions.activity

import android.content.Intent
import android.view.View
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityOptionsCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.au.module_android.utils.asOrNull
import java.lang.IllegalArgumentException

/**
 * 当初始化完成这个对象后，请在onCreate里面调用 函数（onCreate）即可
 */
internal class ActivityForResult(context:Any,
                                 private var resultCallback:(ActivityResultCallback<ActivityResult>)? = null)
        :  DefaultLifecycleObserver, IActivityResult {
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

    private var launcher: ActivityResultLauncher<Intent>? = null
    private val resultContract: ActivityResultContract<Intent, ActivityResult> = ActivityResultContracts.StartActivityForResult()
    private val resultCallbackWrap = ActivityResultCallback<ActivityResult> {
        resultCallback?.onActivityResult(it)
    }

    override fun setOnResultCallback(callback: ActivityResultCallback<ActivityResult>) {
        resultCallback = callback
    }

    override fun getOnResultCallback(): ActivityResultCallback<ActivityResult> {
        return resultCallbackWrap
    }

    override fun onCreate(owner: LifecycleOwner) {
        super.onCreate(owner)
        if (owner is Fragment) {
            launcher = owner.registerForActivityResult(resultContract, getOnResultCallback())
        } else if (owner is AppCompatActivity) {
            owner.lifecycle.addObserver(this)
            launcher = owner.registerForActivityResult(resultContract, getOnResultCallback())
        }
    }

    override fun onDestroy(owner: LifecycleOwner) {
        super.onDestroy(owner)
        launcher?.unregister()
        launcher = null
    }

    /**
     * 启动activity
     */
    override fun start(intent: Intent, option: ActivityOptionsCompat?) {
        launcher?.launch(intent, option)
    }
}