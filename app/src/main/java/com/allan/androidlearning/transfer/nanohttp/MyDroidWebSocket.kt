package com.allan.androidlearning.transfer.nanohttp

import com.allan.androidlearning.transfer.CODE_SUC
import com.allan.androidlearning.transfer.MyDroidGlobalService
import com.allan.androidlearning.transfer.benas.LeftSpaceResult
import com.allan.androidlearning.transfer.benas.MyDroidModeResult
import com.allan.androidlearning.transfer.benas.toCNName
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
import org.json.JSONObject
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
                    send(ResultBean(CODE_SUC, "success!", LeftSpaceResult(leftSpace)).toJsonString())
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
        val json = JSONObject(text)
        if (json.has("wsInit")) {
            val targetName = json.optString("wsInit")
            clientTellName = targetName
            server.triggerConnectionsList()
            //通过later则不需要注意线程
            ToastBuilder().setMessage("一个新的网页接入！$remoteIpStr@$targetName").setIcon("success").setOnTopLater(200).toast()

            val mode = MyDroidGlobalService.myDroidModeData.realValue?.toCNName() ?: "--"
            val ret = ResultBean(CODE_SUC, "success!", MyDroidModeResult(mode, targetName))
            send(ret.toJsonString())
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