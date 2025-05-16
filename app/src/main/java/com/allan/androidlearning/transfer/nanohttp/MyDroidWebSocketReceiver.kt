package com.allan.androidlearning.transfer.nanohttp

import androidx.lifecycle.Observer
import com.allan.androidlearning.transfer.CODE_SUC
import com.allan.androidlearning.transfer.MyDroidGlobalService
import com.allan.androidlearning.transfer.benas.MyDroidMode
import com.allan.androidlearning.transfer.benas.UriRealInfoEx
import com.allan.androidlearning.transfer.benas.UriRealInfoHtml
import com.allan.androidlearning.transfer.htmlbeans.API_LEFT_SPACE
import com.allan.androidlearning.transfer.htmlbeans.API_REQUEST_FILE
import com.allan.androidlearning.transfer.htmlbeans.API_SEND_FILE_LIST
import com.allan.androidlearning.transfer.htmlbeans.API_WS_INIT
import com.allan.androidlearning.transfer.htmlbeans.FileListForHtmlResult
import com.allan.androidlearning.transfer.htmlbeans.LeftSpaceResult
import com.allan.androidlearning.transfer.htmlbeans.WSResultBean
import com.au.module_android.Globals
import com.au.module_android.json.toJsonString
import com.au.module_android.utils.launchOnThread
import com.au.module_android.utils.logdNoFile
import com.au.module_android.utils.logt
import com.au.module_android.utilsmedia.getExternalFreeSpace
import fi.iki.elonen.NanoHTTPD
import fi.iki.elonen.NanoWSD
import fi.iki.elonen.NanoWSD.WebSocketFrame
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.IOException

open class MyDroidWebSocketReceiver(httpSession: NanoHTTPD.IHTTPSession,
                                    var server: MyDroidWebSocketServer,
                                    val colorIcon:Int) : NanoWSD.WebSocket(httpSession) {
    interface IOnMessage {
        fun onNewClientInit()
        fun onSendFile(uriUuid:String, info: UriRealInfoEx?)
    }

    private val remoteIpStr: String? = httpSession.remoteIpAddress

    /**
     * 客户端名字；就像 192.168.0.6@abde1234
     */
    var clientName = "$remoteIpStr@--"
        private set

    var openTs:Long = System.currentTimeMillis()

    private var isActive = true

    var scope:CoroutineScope? = null

    private val messager : IOnMessage = MyDroidWebSocketMessager(this)

    override fun onOpen() {
        scope = MainScope()

        logdNoFile { "$clientName on open:" }
        openTs = System.currentTimeMillis() //必须在前面
        server.addIntoConnections(this)

        heartbeat()
    }

    private fun heartbeat() {
        server.heartbeatScope.launch {
            var leftSpaceCount = 0L
            while (isActive) {
                logdNoFile { "${Thread.currentThread()} $clientName heartbeat!" }
                leftSpaceCount++
                try {
                    if (leftSpaceCount % 3 == 1L) { //隔久一点再告知leftSpace
                        val leftSpace = getExternalFreeSpace(Globals.app)
                        val json = WSResultBean(CODE_SUC, "success!", API_LEFT_SPACE, LeftSpaceResult(leftSpace)).toJsonString()
                        logt { "send: $json" }
                        send(json)
                    }

                    //心跳 websocket必须ping。浏览器则自动实现了pong。
                    ping(MyDroidWebSocketServer.PING_PAYLOAD)
                    delay(MyDroidWebSocketServer.HEARTBEAT_INTERVAL)
                } catch (e: IOException) {
                    onException(e)
                }
            }
        }
    }

    override fun onClose(code: WebSocketFrame.CloseCode, reason: String, initiatedByRemote: Boolean) {
        logdNoFile { "$clientName on close: $reason initByRemote:$initiatedByRemote" }
        isActive = false
        server.removeFromConnections(this)
        scope?.cancel()
        scope = null
    }

    override fun onMessage(message: WebSocketFrame) {
        val text = message.textPayload
        logt { "$clientName on Message:$text" }
        val json = JSONObject(text)
        if (json.has(API_WS_INIT)) {
            val targetName = json.optString(API_WS_INIT)
            clientName = "$remoteIpStr@$targetName"
            server.triggerConnectionsList()

            messager.onNewClientInit()
        } else if (json.has(API_REQUEST_FILE)) {
            val uriUuid = json.optString(API_REQUEST_FILE)
            val info = MyDroidGlobalService.shareReceiverUriMap.value?.get(uriUuid)
            messager.onSendFile(uriUuid, info)
        }
        message.setUnmasked()
    }

    override fun onPong(pong: WebSocketFrame) {
        logdNoFile { "$clientName on Pong: " + pong.textPayload }
        if (pong.textPayload != MyDroidWebSocketServer.PING_PAYLOAD_TEXT) {
            onException(IOException("WS: pong is not same!"))
        }
    }

    override fun onException(exception: IOException) {
        logdNoFile{"$clientName on Exception: " + exception.message}
        try {
            // 主动发送关闭帧并终止连接
            close(WebSocketFrame.CloseCode.InternalServerError, "Server Error on Exception", false)
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}