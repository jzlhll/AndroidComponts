package com.allan.mydroid.nanohttp

import org.json.JSONObject

class WebsocketReceiverModeMessenger(client: WebsocketOneClient) : AbsWebSocketClientMessenger(client) {
    override fun onOpen() {
    }

    override fun onClose() {
    }

    override fun onMessage(api:String, json: JSONObject) {
    }
}