package com.allan.autoclickfloat.nongyao

import android.widget.Toast
import com.au.module_androiduilight.toast.toastOnTop
import kotlinx.coroutines.delay

class VipNode(nextNode: AppNode?) : AppNode("com.tencent.mm", 40, nextNode) {
    override suspend fun action(): Boolean {
        delay(5000)
        return false
    }
}