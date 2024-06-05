package com.allan.autoclickfloat.nongyao

import com.au.module_android.Globals
import com.au.module_android.utils.openAppActivity
import kotlinx.coroutines.delay

class TgClubNode(nextNode: AppNode?) : AppNode("com.tencent.tgclub", 20, nextNode) {
    override suspend fun action(): Boolean {
        delay(8000)
        openAppActivity(Globals.app, "com.tencent.tgclub", "")
        return false
    }
}