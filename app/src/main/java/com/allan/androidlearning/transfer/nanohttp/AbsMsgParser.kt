package com.allan.androidlearning.transfer.nanohttp

import com.allan.androidlearning.transfer.nanohttp.h5client.ClientWebSocket
import org.json.JSONObject

abstract class AbsMsgParser(val client: ClientWebSocket) {
    abstract fun onOpen()
    abstract fun onClose()

    abstract fun onMessage(api:String, json: JSONObject)
}