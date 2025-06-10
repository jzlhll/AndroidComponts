package com.allan.mydroid.nanohttp

import org.json.JSONObject

abstract class AbsWebSocketClientMessenger(val client: WebsocketOneClient) {
    abstract fun onOpen()
    abstract fun onClose()

    abstract fun onMessage(api:String, json: JSONObject)
}