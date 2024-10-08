package com.au.module_android.permissions.activity

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityOptionsCompat
import com.au.module_android.permissions.IContractResult

/**
 * 当初始化完成这个对象后，请在onCreate里面调用 函数（onCreate）即可
 */
class ActivityForResult(cxt:Any) : IContractResult<Intent, ActivityResult>(cxt, ActivityResultContracts.StartActivityForResult()) {

    /**
     * 启动activity
     */
    fun start(intent: Intent, option: ActivityOptionsCompat?, callback: ActivityResultCallback<ActivityResult>?) {
        callback?.let { setResultCallback(it) }
        launcher.launch(intent, option)
    }

    fun jumpToAppDetail(appContext: Context) {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        intent.data = Uri.fromParts("package", appContext.packageName, null)
        start(intent, null, null)
    }
}