package com.allan.mydroid.nanohttp

import androidx.annotation.ColorRes
import com.allan.mydroid.R
import com.allan.mydroid.beans.MyDroidMode
import com.allan.mydroid.beans.WebSocketClientInfo
import com.allan.mydroid.globals.MyDroidConst
import com.allan.mydroid.globals.MyDroidGlobalService
import com.au.module_android.utils.logdNoFile
import fi.iki.elonen.NanoHTTPD.Response.Status
import fi.iki.elonen.NanoWSD
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.cancel
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.Executors

class WebsocketServer(port:Int) : NanoWSD(port) {
    companion object {
        /**
         * 心跳时间
         */
        const val HEARTBEAT_INTERVAL: Long = 45 * 1000

        /**
         * 服务端开启的一条webSocket通道的最长心跳时间。因此设置的心跳要比这个值短。
         */
        const val WEBSOCKET_READ_TIMEOUT = HEARTBEAT_INTERVAL * 2 + HEARTBEAT_INTERVAL / 2

        const val WS_CODE_CLOSE_BY_CLIENT = 1000
    }

    private val executor = Executors.newSingleThreadExecutor()
    private val singleThreadDispatcher = executor.asCoroutineDispatcher()

    /**
     * 多个websocket客户端共享同一线程、scope
     */
    val scope = CoroutineScope(singleThreadDispatcher)

    private val connections: MutableList<WebsocketOneClient> = CopyOnWriteArrayList()

    private var currentColorIconIndex = 0
    private val colorIconList = listOf(
        R.color.client_send_1,
        R.color.client_send_2,
        R.color.client_send_3,
        R.color.client_send_4,
        R.color.client_send_5,
        )

    @ColorRes
    private fun nextColorIcon() : Int{
        return if(currentColorIconIndex >= colorIconList.size) {
            currentColorIconIndex = 0
            colorIconList[currentColorIconIndex]
        } else {
            colorIconList[currentColorIconIndex++]
        }
    }

    fun addIntoConnections(websocket: WebsocketOneClient) {
        MyDroidGlobalService.updateAliveTs("when new client add")
        connections.add(websocket)
        triggerConnectionsList()
    }

    fun removeFromConnections(websocket: WebsocketOneClient) {
        connections.remove(websocket)
        triggerConnectionsList()
    }

    /**
     * 触达一下。
     */
    fun triggerConnectionsList() {
        val list = ArrayList<WebSocketClientInfo>()
        connections.forEach {
            list.add(WebSocketClientInfo(it.clientName, it.isHtml, it.openTs, it.colorIcon))
        }
        list.sortByDescending { it.enterTs }
        logdNoFile { "after change websocket client size ${list.size}" }
        MyDroidConst.clientListLiveData.setValueSafe(list)
    }

    override fun openWebSocket(handshake: IHTTPSession): WebSocket {
        val uri = handshake.uri
        val nextColorIcon = nextColorIcon()
        logdNoFile { "open web Socket handshake uri: $uri nextColorIcon $nextColorIcon" }
        //uri = uri.replaceFirst("/", "", true)
        val client = WebsocketOneClient(handshake, this, nextColorIcon())
        val parser = when (MyDroidConst.myDroidMode) {
            MyDroidMode.Receiver -> WebsocketReceiverModeMessenger(client)
            MyDroidMode.Send -> WebsocketSendModeMessenger(client)
            MyDroidMode.Middle -> WebsocketSendModeMessenger(client)
            MyDroidMode.None,
            MyDroidMode.Image,
            MyDroidMode.TextChat,
            MyDroidMode.Video -> WebsocketReceiverModeMessenger(client)
        }
        client.messenger = parser
        return client
    }

    public override fun serveHttp(session: IHTTPSession): Response {
        logdNoFile { "serve http in web socket server." }
        return newFixedLengthResponse(Status.OK, MIME_PLAINTEXT, "Command not found")
    }

    override fun stop() {
        super.stop()
        logdNoFile { "stopped and cancel heartbeat scope" }
        scope.cancel()
        executor.shutdown()
        MyDroidConst.clientListLiveData.setValueSafe(emptyList())
    }
}