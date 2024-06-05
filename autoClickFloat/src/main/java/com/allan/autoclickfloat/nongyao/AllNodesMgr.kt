package com.allan.autoclickfloat.nongyao

class AllNodesMgr {
    private val tgClubNode = TgClubNode(null)
    private val wechatNode = WechatNode(tgClubNode)

    private var currentNode:AppNode = wechatNode

    suspend fun start(progressChangeBlock:(Int)->Unit) : String {
        while (true) {
            progressChangeBlock(currentNode.progress)
            if (!currentNode.startNewApp()) {
                return "当前正在 $progress %, 无法启动应用。已停止。"
            }

            val result = currentNode.action()
            if (!result) {
                return "当前正在 $progress %, 失败。"
            }

            val next = currentNode.nextNode ?: return "完成！"
            currentNode = next
        }
    }

    fun stop() {
        currentNode.stop()
    }

    val progress:Int
        get() {
            return currentNode.progress
        }
}