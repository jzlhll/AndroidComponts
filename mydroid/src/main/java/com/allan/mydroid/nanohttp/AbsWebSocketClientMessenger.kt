package com.allan.mydroid.nanohttp

import org.json.JSONObject

/**
 * Created by au 2025/06.
 * WebSocket通信存在server中的client对象。
 */
abstract class AbsWebSocketClientMessenger(val client: WebsocketClientInServer) {
    abstract fun onOpen()
    abstract fun onClose()

    abstract fun onMessage(origJsonStr:String, api:String, json: JSONObject)
}