package com.allan.autoclickfloat.nongyao

import kotlinx.coroutines.delay

/**
 * @author allan
 * @date :2024/6/3 17:52
 * @description:
 */
class WechatNode(nextNode: AppNode?) : AppNode("com.tencent.mm", 20, nextNode) {
    override suspend fun action(): Boolean {
        delay(6000)
        return false
    }
}