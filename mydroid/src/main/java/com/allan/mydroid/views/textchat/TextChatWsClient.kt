package com.allan.mydroid.views.textchat

import com.allan.mydroid.nanohttp.WebsocketServer.Companion.WS_CODE_CLOSE_BY_CLIENT
import com.au.module_android.utils.logdNoFile
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString

class TextChatWsClient(val ip: String, val port: Int, private val successOpenBlock: () -> Unit) : WebSocketListener() {
    private var webSocket: WebSocket? = null
    var isLive = false

    fun sendText(text: String) : Boolean{
        return webSocket?.send(text) ?: false
    }

    fun shutdown() {
        isLive = false
        webSocket?.cancel()
        webSocket?.close(WS_CODE_CLOSE_BY_CLIENT, null)
    }

    override fun onOpen(webSocket: WebSocket, response: Response) {
        logdNoFile{"Connected"}
        this.webSocket = webSocket
        isLive = true
        successOpenBlock()
    }

    override fun onMessage(webSocket: WebSocket, text: String) {
        logdNoFile{"Message received: " + text}
    }
        
    override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
        logdNoFile{"Message received (binary): " + bytes.hex()}
    }

    override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
        logdNoFile{"Closing: " + code + " / " + reason}
//        webSocket.close(WS_CODE_CLOSE_BY_CLIENT, null)
        isLive = false
    }

    override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
        logdNoFile{"Closed: " + code + " / " + reason}
        this.webSocket = null
        isLive = false
    }

    override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
        logdNoFile{"onFailure: " + t.message}
    }
}
