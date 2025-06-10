package com.allan.mydroid.nanohttp

import com.allan.mydroid.R
import com.allan.mydroid.beans.API_WS_CLIENT_INIT_CALLBACK
import com.allan.mydroid.beans.API_WS_INIT
import com.allan.mydroid.beans.API_WS_LEFT_SPACE
import com.allan.mydroid.beans.API_WS_PING
import com.allan.mydroid.beans.LeftSpaceResult
import com.allan.mydroid.beans.MyDroidModeResult
import com.allan.mydroid.beans.WSResultBean
import com.allan.mydroid.beans.toName
import com.allan.mydroid.globals.CODE_SUC
import com.allan.mydroid.globals.DEBUG_SLOW_RECEIVER_TRANSFER
import com.allan.mydroid.globals.DEBUG_SLOW_SEND_TRANSFER
import com.allan.mydroid.globals.MyDroidConst
import com.allan.mydroid.globals.MyDroidGlobalService
import com.au.module_android.Globals
import com.au.module_android.Globals.resStr
import com.au.module_android.json.toJsonString
import com.au.module_android.utils.launchOnThread
import com.au.module_android.utils.logdNoFile
import com.au.module_android.utils.logt
import com.au.module_android.utilsmedia.getExternalFreeSpace
import com.au.module_androidui.toast.ToastBuilder
import fi.iki.elonen.NanoHTTPD
import fi.iki.elonen.NanoWSD
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import org.json.JSONObject
import java.io.IOException

class WebsocketOneClient(httpSession: NanoHTTPD.IHTTPSession,
                         val server: WebsocketServer,
                         val color: Int) : NanoWSD.WebSocket(httpSession) {
    private val remoteIpStr: String? = httpSession.remoteIpAddress

    /**
     * 初始化的同时赋值
     */
    lateinit var messenger : AbsWebSocketClientMessenger

    /**
     * 客户端名字；就像 192.168.0.6@abde1234
     */
    var clientName = "$remoteIpStr@--"
        private set

    var platform = ""

    var openTs:Long = System.currentTimeMillis()

    private var isActive = true

    private var mFreeSpaceJob : Job? = null

    var scope: CoroutineScope? = null

    override fun onOpen() {
        scope = MainScope()
        logdNoFile { "$clientName on open:" }
        openTs = System.currentTimeMillis() //必须在前面
        server.addIntoConnections(this)


        messenger.onOpen()
        getFreeSpaceJob()
    }

    private fun getFreeSpaceJob() {
        mFreeSpaceJob = scope?.launchOnThread {
            var leftSpaceCount = 0L
            while (isActive) {
                logdNoFile { "${Thread.currentThread()} $clientName heartbeat!" }
                leftSpaceCount++
                try {
                    if (leftSpaceCount * 5 == 1L) { //隔久一点再告知leftSpace
                        val leftSpace = getExternalFreeSpace(Globals.app)
                        val suc = R.string.success_message.resStr()
                        val json = WSResultBean(CODE_SUC, suc, API_WS_LEFT_SPACE, LeftSpaceResult(leftSpace)).toJsonString()
                        logt { "$clientName send: $json" }
                        send(json)
                    }
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
        mFreeSpaceJob?.cancel()
        scope?.cancel()
        scope = null
    }

    override fun onMessage(message: NanoWSD.WebSocketFrame) {
        val text = message.textPayload
        logt { "$clientName on Message:$text" }
        val json = JSONObject(text)
        val api = json.optString("api")

        MyDroidGlobalService.updateAliveTs("when ws on message $api")
        when (api) {
            API_WS_INIT -> {
                val targetName = json.optString("wsName")
                val platform = json.optString("platform")
                this.platform = platform
                clientName = "$remoteIpStr@$targetName"
                server.triggerConnectionsList()

                clientInit()
            }

            API_WS_PING -> {
                logdNoFile { "$clientName get client ping " }
                //later 是否要响应，client定时发过来就行。不需要处理。
//                sendFrame(NanoWSD.WebSocketFrame(NanoWSD.WebSocketFrame.OpCode.Pong,
//                    true,
//                    PING_PLAY_LOAD_DATA))
            }

            else -> {
                messenger.onMessage(text, api, json)
            }
        }

        message.setUnmasked()
    }

    private fun clientInit() {
        //通过later则不需要注意线程
        val message = String.format(R.string.new_webpage_access.resStr(), clientName)
        ToastBuilder().setMessage(message)
            .setIcon("success")
            .setOnTopLater(200).toast()

        val mode = MyDroidConst.currentDroidMode.toName()
        val json = WSResultBean(
            CODE_SUC, R.string.success_message.resStr(),
            API_WS_CLIENT_INIT_CALLBACK,
            MyDroidModeResult(
                mode,
                clientName,
                color,
                debugReceiver = DEBUG_SLOW_RECEIVER_TRANSFER,
                debugSend = DEBUG_SLOW_SEND_TRANSFER
            )
        ).toJsonString()
        logt { "send: $json" }
        send(json)
    }

    override fun onPong(pong: NanoWSD.WebSocketFrame) {
        logdNoFile { "$clientName on Pong: " + pong.textPayload }
//        if (pong.textPayload != WebsocketServer.Companion.PING_PAYLOAD_TEXT) {
//            onException(IOException("WS: pong is not same!"))
//        }
    }

    override fun onException(exception: IOException) {
        logdNoFile{"$clientName on Exception: " + exception.message}
        mFreeSpaceJob?.cancel()
        try {
            // 主动发送关闭帧并终止连接
            close(NanoWSD.WebSocketFrame.CloseCode.InternalServerError, Globals.getString(R.string.server_error_exception), false)
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}