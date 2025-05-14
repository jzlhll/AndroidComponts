package com.allan.androidlearning.transfer.nanohttp

import androidx.lifecycle.Observer
import com.allan.androidlearning.transfer.CODE_SUC
import com.allan.androidlearning.transfer.MyDroidGlobalService
import com.allan.androidlearning.transfer.htmlbeans.LeftSpaceResult
import com.allan.androidlearning.transfer.benas.UriRealInfoEx
import com.allan.androidlearning.transfer.benas.UriRealInfoHtml
import com.allan.androidlearning.transfer.benas.toCNName
import com.allan.androidlearning.transfer.htmlbeans.FileListForHtmlResult
import com.allan.androidlearning.transfer.htmlbeans.MyDroidModeResult
import com.au.module_android.Globals
import com.au.module_android.api.ResultBean
import com.au.module_android.json.toJsonString
import com.au.module_android.utils.launchOnThread
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
                            var server: MyDroidWebSocketServer,
                            val colorIcon:Int) : NanoWSD.WebSocket(httpSession) {
    val remoteIpStr: String? = httpSession.remoteIpAddress

    var openTs:Long = System.currentTimeMillis()

    private val cTag by unsafeLazy {
        val str = this@MyDroidWebSocket.toString()
        remoteIpStr + "@" + str.substring(str.indexOf("@") + 1)
    }

    var clientTellName = "--"

    private var isActive = true

    private var mShareReceiverUriMapOb = object : Observer<HashMap<String, UriRealInfoEx>> {
        override fun onChanged(map: HashMap<String, UriRealInfoEx>) {
            val cvtList = mutableListOf<UriRealInfoHtml>()
            map.values.forEach { urlRealInfoEx->
                cvtList.add(urlRealInfoEx.copyToHtml())
            }
            server.heartbeatScope.launchOnThread {
                val ret = ResultBean(CODE_SUC, "send files to html!", FileListForHtmlResult(cvtList))
                val json = ret.toJsonString()
                logt { "${Thread.currentThread()} on map changed. send file list to html" }
                logt { json }
                send(json)
            }
        }
    }

    override fun onOpen() {
        logdNoFile { "$cTag on open:" }
        openTs = System.currentTimeMillis() //必须在前面
        server.addIntoConnections(this)

        heartbeat()

        Globals.mainScope.launch {
            MyDroidGlobalService.shareReceiverUriMap.observeForever(mShareReceiverUriMapOb)
        }
    }

    private fun heartbeat() {
        server.heartbeatScope.launch {
            var leftSpaceCount = 0L
            while (isActive) {
                logdNoFile { "${Thread.currentThread()} $cTag heartbeat!" }
                leftSpaceCount++
                try {
                    if (leftSpaceCount % 3 == 0L) { //隔久一点再告知leftSpace
                        val leftSpace = getExternalFreeSpace(Globals.app)
                        send(ResultBean(CODE_SUC, "success!", LeftSpaceResult(leftSpace)).toJsonString())
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
        logdNoFile { "$cTag on close: $reason initByRemote:$initiatedByRemote" }
        isActive = false
        server.removeFromConnections(this)
        Globals.mainScope.launch {
            MyDroidGlobalService.shareReceiverUriMap.removeObserver(mShareReceiverUriMapOb)
        }
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
            val ret = ResultBean(CODE_SUC, "success!", MyDroidModeResult(mode, remoteIpStr, targetName))
            send(ret.toJsonString())
        }
        message.setUnmasked()
    }

    override fun onPong(pong: WebSocketFrame) {
        logdNoFile { "$cTag on Pong: " + pong.textPayload }
        if (pong.textPayload != MyDroidWebSocketServer.PING_PAYLOAD_TEXT) {
            onException(IOException("WS: pong is not same!"))
        }
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