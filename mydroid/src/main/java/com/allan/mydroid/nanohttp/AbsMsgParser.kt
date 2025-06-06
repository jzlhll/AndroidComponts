package com.allan.mydroid.nanohttp

import com.allan.mydroid.nanohttp.h5client.ClientWebSocket
import org.json.JSONObject

abstract class AbsMsgParser(val client: ClientWebSocket) {
    abstract fun onOpen()
    abstract fun onClose()

    abstract fun onMessage(api:String, json: JSONObject)
}