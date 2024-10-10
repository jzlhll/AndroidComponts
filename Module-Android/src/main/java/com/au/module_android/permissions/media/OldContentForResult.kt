package com.au.module_android.permissions.media

import android.net.Uri
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import com.au.module_android.permissions.IContractResult

class OldContentForResult(cxt:Any) : IContractResult<String, Uri?>(cxt, ActivityResultContracts.GetContent()) {
    fun open(mimeType:String, callback: ActivityResultCallback<Uri?>?) {
        callback?.let { setResultCallback(it) }
        launcher.launch(mimeType)
    }
}

class OldMultiContentForResult(cxt:Any) : IContractResult<String, List<@JvmSuppressWildcards Uri>>(cxt, ActivityResultContracts.GetMultipleContents()) {
    fun open(mimeType:String, callback: ActivityResultCallback<List<@JvmSuppressWildcards Uri>>?) {
        callback?.let { setResultCallback(it) }
        launcher.launch(mimeType)
    }
}