package com.allan.androidlearning.transfer.nanohttp

import com.allan.androidlearning.transfer.CODE_SUC
import com.au.module_android.Globals
import com.au.module_android.api.ResultBean
import com.au.module_android.json.toJsonString
import com.au.module_android.utils.logdNoFile
import com.au.module_android.utils.logt
import com.au.module_android.utils.unsafeLazy
import com.au.module_android.utilsmedia.getExternalFreeSpace
import com.au.module_androidui.toast.ToastBuilder
import fi.iki.elonen.NanoHTTPD
import fi.iki.elonen.NanoWSD
import fi.iki.elonen.NanoWSD.WebSocketFrame
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.IOException

open class MyDroidWebSocket(httpSession: NanoHTTPD.IHTTPSession,
                            var server: MyDroidWebSocketServer) : NanoWSD.WebSocket(httpSession) {
    private val cTag by unsafeLazy {
        val str = this@MyDroidWebSocket.toString()
        str.substring(str.indexOf("@") + 1)
    }

    val remoteIpStr: String? = httpSession.remoteIpAddress
    var clientTellName = "--"

    private var isActive = true

    override fun onOpen() {
        logdNoFile { "$cTag on open:" }
        server.addIntoConnections(this)

        server.heartbeatScope.launch {
            while (isActive) {
                logdNoFile { "$cTag heartbeat!" }
                try {
                    //ping(MyDroidWebSocketServer.PING_PAYLOAD)
                    val leftSpace = getExternalFreeSpace(Globals.app)
                    send(ResultBean(CODE_SUC, "", "leftSpace:$leftSpace").toJsonString())
                    delay(MyDroidWebSocketServer.HEARTBEAT_INTERVAL)
                } catch (e: IOException) {
                    onException(e)
                }
            }
        }
    }

    override fun onClose(code: WebSocketFrame.CloseCode, reason: String, initiatedByRemote: Boolean) {
        logdNoFile { "$cTag on close: $reason initByRemote:$initiatedByRemote" }
        isActive = false
        server.removeFromConnections(this)
    }

    override fun onMessage(message: WebSocketFrame) {
        val text = message.textPayload
        logt { "$cTag on Message:$text" }
        if (text.startsWith("websocket client init:")) {
            val targetName = text.replace("websocket client init:", "")
            clientTellName = targetName
            server.triggerConnectionsList()
            ToastBuilder().setMessage("一个新的网页接入！$remoteIpStr@$targetName").setIcon("success").setOnTopLater(200).toast()
        }
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