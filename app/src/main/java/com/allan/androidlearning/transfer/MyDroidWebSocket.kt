package com.allan.androidlearning.transfer

import com.au.module_android.utils.logdNoFile
import com.au.module_android.utils.unsafeLazy
import fi.iki.elonen.NanoHTTPD
import fi.iki.elonen.NanoWSD
import fi.iki.elonen.NanoWSD.WebSocketFrame
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.IOException

open class MyDroidWebSocket(val httpSession: NanoHTTPD.IHTTPSession, var httpServer: MyDroidWebSocketServer) : NanoWSD.WebSocket(httpSession) {
    private val cTag by unsafeLazy {
        val str = this@MyDroidWebSocket.toString()
        str.substring(str.indexOf("@") + 1)
    }

    private var isActive = true

    override fun onOpen() {
        logdNoFile { "$cTag on open:" }
        httpServer.connections.add(this)
        httpServer.heartbeatScope.launch {
            while (isActive) {
                logdNoFile { "$cTag heartbeat!" }
                try {
                    ping(MyDroidWebSocketServer.PING_PAYLOAD)
                } catch (e: IOException) {
                    onException(e)
                }
                delay(MyDroidWebSocketServer.HEARTBEAT_INTERVAL)
            }
        }
    }

    override fun onClose(code: WebSocketFrame.CloseCode, reason: String, initiatedByRemote: Boolean) {
        logdNoFile { "$cTag on close: $reason initByRemote:$initiatedByRemote" }
        isActive = false
        this.httpServer.connections.remove(this)
    }

    override fun onMessage(message: WebSocketFrame) {
        logdNoFile { "$cTag on Message:" + message.textPayload.toString() }
        message.setUnmasked()
    }

    override fun onPong(pong: WebSocketFrame) {
        logdNoFile { "$cTag on Pong: " }
    }

    override fun onException(exception: IOException) {
        logdNoFile{"$cTag on Exception: " + exception.message}
        try {
            // 主动发送关闭帧并终止连接
            close(WebSocketFrame.CloseCode.InternalServerError, "Server Error on Exception", false)
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}