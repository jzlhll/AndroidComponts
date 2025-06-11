package com.allan.mydroid.nanohttp.wsmsger

import com.allan.mydroid.nanohttp.AbsWebSocketClientMessenger
import com.allan.mydroid.nanohttp.WebsocketClientInServer
import org.json.JSONObject

class WebsocketNoneModeMessenger(client: WebsocketClientInServer) : AbsWebSocketClientMessenger(client) {
    override fun onOpen() {
    }

    override fun onClose() {
    }

    override fun onMessage(origJsonStr:String, api:String, json: JSONObject) {
    }
}