package com.allan.androidlearning.transfer.nanohttp.h5client

import com.allan.androidlearning.transfer.CODE_SUC
import com.allan.androidlearning.transfer.MyDroidGlobalService
import com.allan.androidlearning.transfer.benas.toCNName
import com.allan.androidlearning.transfer.htmlbeans.API_CLIENT_INIT_CALLBACK
import com.allan.androidlearning.transfer.htmlbeans.API_LEFT_SPACE
import com.allan.androidlearning.transfer.htmlbeans.API_WS_INIT
import com.allan.androidlearning.transfer.htmlbeans.LeftSpaceResult
import com.allan.androidlearning.transfer.htmlbeans.MyDroidModeResult
import com.allan.androidlearning.transfer.htmlbeans.WSResultBean
import com.allan.androidlearning.transfer.nanohttp.AbsMsgParser
import com.allan.androidlearning.transfer.nanohttp.MyDroidWSServer
import com.au.module_android.Globals
import com.au.module_android.json.toJsonString
import com.au.module_android.utils.logdNoFile
import com.au.module_android.utils.logt
import com.au.module_android.utilsmedia.getExternalFreeSpace
import com.au.module_androidui.toast.ToastBuilder
import fi.iki.elonen.NanoHTTPD
import fi.iki.elonen.NanoWSD
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.IOException

class ClientWebSocket(httpSession: NanoHTTPD.IHTTPSession,
                      var server: MyDroidWSServer,
                      val colorIcon:Int) : NanoWSD.WebSocket(httpSession) {
    private val remoteIpStr: String? = httpSession.remoteIpAddress

    /**
     * 初始化的同时赋值
     */
    lateinit var messenger : AbsMsgParser

    /**
     * 客户端名字；就像 192.168.0.6@abde1234
     */
    var clientName = "$remoteIpStr@--"
        private set

    var openTs:Long = System.currentTimeMillis()

    private var isActive = true

    var scope: CoroutineScope? = null

    override fun onOpen() {
        scope = MainScope()

        logdNoFile { "$clientName on open:" }
        openTs = System.currentTimeMillis() //必须在前面
        server.addIntoConnections(this)

        heartbeat()

        messenger.onOpen()
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
                    ping(MyDroidWSServer.Companion.PING_PAYLOAD)
                    delay(MyDroidWSServer.Companion.HEARTBEAT_INTERVAL)
                } catch (e: IOException) {
                    onException(e)
                }
            }
        }
    }

    override fun onClose(code: NanoWSD.WebSocketFrame.CloseCode, reason: String, initiatedByRemote: Boolean) {
        logdNoFile { "$clientName on close: $reason initByRemote:$initiatedByRemote" }
        isActive = false
        server.removeFromConnections(this)
        messenger.onClose()

        scope?.cancel()
        scope = null
    }

    override fun onMessage(message: NanoWSD.WebSocketFrame) {
        val text = message.textPayload
        logt { "$clientName on Message:$text" }
        val json = JSONObject(text)
        if (json.has(API_WS_INIT)) {
            val targetName = json.optString(API_WS_INIT)
            clientName = "$remoteIpStr@$targetName"
            server.triggerConnectionsList()

            clientInit()
        } else {
            messenger.onMessage(json)
        }

        message.setUnmasked()
    }

    private fun clientInit() {
        //通过later则不需要注意线程
        ToastBuilder().setMessage("$clientName 新的网页接入！")
            .setIcon("success")
            .setOnTopLater(200).toast()

        val mode = MyDroidGlobalService.myDroidModeData.realValue?.toCNName() ?: "--"
        val json = WSResultBean(CODE_SUC, "success!",
                    API_CLIENT_INIT_CALLBACK,
                    MyDroidModeResult(mode, clientName)).toJsonString()
        logt { "send: $json" }
        send(json)
    }

    override fun onPong(pong: NanoWSD.WebSocketFrame) {
        logdNoFile { "$clientName on Pong: " + pong.textPayload }
        if (pong.textPayload != MyDroidWSServer.Companion.PING_PAYLOAD_TEXT) {
            onException(IOException("WS: pong is not same!"))
        }
    }

    override fun onException(exception: IOException) {
        logdNoFile{"$clientName on Exception: " + exception.message}
        try {
            // 主动发送关闭帧并终止连接
            close(NanoWSD.WebSocketFrame.CloseCode.InternalServerError, "Server Error on Exception", false)
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}