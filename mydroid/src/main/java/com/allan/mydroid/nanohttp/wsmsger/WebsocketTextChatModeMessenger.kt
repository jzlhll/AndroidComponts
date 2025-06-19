package com.allan.mydroid.nanohttp.wsmsger

import com.allan.mydroid.api.WSApisConst.Companion.API_WS_TEXT_CHAT_MSG
import com.allan.mydroid.beans.WSChatMessageBean
import com.allan.mydroid.nanohttp.AbsWebSocketClientMessenger
import com.allan.mydroid.nanohttp.WebsocketClientInServer
import com.au.module_android.json.fromJson
import org.json.JSONObject

/**
 * 重申：这是运行在websocket server中的client实现。注意与TextChatWsClient区分。
 */
class WebsocketTextChatModeMessenger(client: WebsocketClientInServer) : AbsWebSocketClientMessenger(client) {
    override fun onOpen() {
    }

    override fun onClose() {
    }

    override fun onMessage(origJsonStr:String, api: String, json: JSONObject) {
        when (api) {
            API_WS_TEXT_CHAT_MSG -> {
                val chatBean = origJsonStr.fromJson<WSChatMessageBean>() ?: return
                client.server.onTextChatMessageArrived(client, chatBean)
            }
        }
    }
}