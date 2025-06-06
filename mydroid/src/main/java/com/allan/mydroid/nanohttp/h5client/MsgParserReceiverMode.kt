package com.allan.mydroid.nanohttp.h5client

import com.allan.mydroid.nanohttp.AbsMsgParser
import org.json.JSONObject

class MsgParserReceiverMode(client: ClientWebSocket) : AbsMsgParser(client) {
    override fun onOpen() {
    }

    override fun onClose() {
    }

    override fun onMessage(api:String, json: JSONObject) {
    }
}