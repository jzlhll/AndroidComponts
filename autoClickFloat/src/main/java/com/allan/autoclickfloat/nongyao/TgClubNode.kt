package com.allan.autoclickfloat.nongyao

import kotlinx.coroutines.delay

class TgClubNode(nextNode: AppNode?) : AppNode("com.tencent.tgclub", 20, nextNode) {
    override suspend fun action(): Boolean {
        delay(2000)
        return false
    }
}