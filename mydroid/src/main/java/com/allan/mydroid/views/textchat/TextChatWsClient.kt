package com.allan.mydroid.views.textchat

import com.allan.mydroid.beans.API_WS_CLIENT_INIT_CALLBACK
import com.allan.mydroid.beans.API_WS_INIT
import com.allan.mydroid.beans.WSChatMessageBean
import com.allan.mydroid.beans.WSInitBean
import com.allan.mydroid.nanohttp.WebsocketServer.Companion.WS_CODE_CLOSE_BY_CLIENT
import com.au.module_android.json.fromJson
import com.au.module_android.json.toJsonString
import com.au.module_android.utils.logdNoFile
import kotlinx.coroutines.CoroutineScope
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString
import org.json.JSONObject
import java.util.UUID

/**
 * 运行在客户端中的通过okhttp WebSocket client实例。
 */
class TextChatWsClient(val vmScope: CoroutineScope,
                       val ip: String, val port: Int,
                       private val successOpenBlock: () -> Unit) : WebSocketListener() {
    private var webSocket: WebSocket? = null
    private val wsName = UUID.randomUUID().toString().substring(0, 6)

    /**
     * 服务端随机得到的color
     */
    var color:Int? = null

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

        val bean = WSInitBean(API_WS_INIT, wsName, "androidApp")
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
        when (api) {
            API_WS_CLIENT_INIT_CALLBACK -> {
                color = json.optInt("color")
            }
        }
        val bean = text.fromJson<WSChatMessageBean>()
        if (bean != null) {
            logdNoFile{ "Message received1: $text" }
        } else {
            logdNoFile{ "Message received0: $text" }
        }
    }
        
    override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
        logdNoFile{"Message received (binary): " + bytes.hex()}
    }

    override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
        logdNoFile{"Closing: " + code + " / " + reason}
//        webSocket.close(WS_CODE_CLOSE_BY_CLIENT, null)
        shutdown()
    }

    override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
        logdNoFile{"Closed: " + code + " / " + reason}
        shutdown()
    }

    override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
        logdNoFile{"onFailure: " + t.message}
    }
}
