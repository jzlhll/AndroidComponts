package com.allan.autoclickfloat.nongyao

/**
 * @author allan
 * @date :2024/6/3 17:52
 * @description:
 */
class WechatNode(nextNode: AppNode?) : AppNode("com.tencent.mm", 20, nextNode) {
    override suspend fun action(): Boolean {
        return false
    }
}