package com.allan.mydroid.views.textchat

import com.allan.mydroid.api.API_WS_CLIENT_INIT_CALLBACK
import com.allan.mydroid.api.API_WS_INIT
import com.allan.mydroid.api.API_WS_TEXT_CHAT_MSG
import com.allan.mydroid.beans.wsdata.MyDroidModeData
import com.allan.mydroid.beans.WSChatMessageBean
import com.allan.mydroid.beans.wsdata.WSInitData
import com.allan.mydroid.nanohttp.WebsocketServer.Companion.WS_CODE_CLOSE_BY_CLIENT
import com.au.module_android.Globals
import com.au.module_android.json.fromJson
import com.au.module_android.json.toJsonString
import com.au.module_android.utils.logd
import com.au.module_android.utils.logdNoFile
import com.au.module_android.utils.unsafeLazy
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString
import org.json.JSONObject
import java.io.EOFException
import java.util.UUID

/**
 * 运行在客户端中的通过okhttp WebSocket client实例。
 */
class TextChatWsClient(val vmScope: CoroutineScope,
                       val ip: String,
                       val port: Int,
                       private val successOpenBlock: () -> Unit) : WebSocketListener() {
    private var webSocket: WebSocket? = null
    private val wsName = UUID.randomUUID().toString().substring(0, 6)

    private val _goodName by unsafeLazy { "$ip@$wsName" }

    fun goodName() = _goodName

    /**
     * 将服务器发送过来的消息进行转发到UI中去
     */
    var onTransferClientMsgCallback:((message: WSChatMessageBean)->Unit)? = null

    /**
     * 被关闭后的回调
     */
    var onClosed:((reason:String)->Unit) = {}

    /**
     * 服务端随机得到的color
     */
    var color:String? = null

    var isLive = false

    fun sendText(text: String) : Boolean{
        return webSocket?.send(text) ?: false
    }

    fun shutdown(reason:String) {
        manualShutdown()
        onClosed(reason)
    }

    fun manualShutdown() {
        isLive = false
        onTransferClientMsgCallback = null

        if (webSocket != null) {
            webSocket?.cancel()
            webSocket?.close(WS_CODE_CLOSE_BY_CLIENT, null)
            webSocket = null
        }
    }

    override fun onOpen(webSocket: WebSocket, response: Response) {
        logdNoFile{"Connected"}
        this.webSocket = webSocket
        isLive = true

        val bean = WSInitData(API_WS_INIT, wsName, "androidApp")
        webSocket.send(bean.toJsonString())

        successOpenBlock()
    }

    override fun onMessage(webSocket: WebSocket, text: String) {
        val json = try {
            JSONObject(text)
        } catch (e: Exception) {
            null
        }
        val api = json?.optString("api")
        val data = json?.optString("data")
        when (api) {
            API_WS_CLIENT_INIT_CALLBACK -> {
                val dataBean = data?.fromJson<MyDroidModeData>()
                color = (dataBean?.color ?: "#212121")
                logdNoFile { "client get init color $color" }
            }

            API_WS_TEXT_CHAT_MSG -> {
                val bean = text.fromJson<WSChatMessageBean>()
                if (bean != null) {
                    logdNoFile{ "Message received1: $text" }
                    vmScope.launch {
                        onTransferClientMsgCallback?.invoke(bean)
                    }
                } else {
                    logdNoFile{ "Message received0: $text" }
                }
            }
        }
    }
        
    override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
        logdNoFile{"Message received (binary): " + bytes.hex()}
    }

    override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
        logdNoFile{"Closing: " + code + " / " + reason}
//        webSocket.close(WS_CODE_CLOSE_BY_CLIENT, null)
        shutdown(reason)
    }

    override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
        logdNoFile{"Closed: " + code + " / " + reason}
        shutdown(reason)
    }

    override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
        logd{"onFailure: " + t}
        if (t is EOFException) {
            shutdown(Globals.getString(com.allan.mydroid.R.string.server_is_closed))
        } else {
            shutdown("onFailure " + t.message)
        }
    }
}
