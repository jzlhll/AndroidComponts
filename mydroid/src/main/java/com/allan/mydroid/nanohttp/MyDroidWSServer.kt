package com.allan.mydroid.nanohttp

import androidx.annotation.ColorRes
import com.allan.mydroid.benas.MyDroidMode
import com.allan.mydroid.benas.WebSocketClientInfo
import com.allan.mydroid.globals.MyDroidConst
import com.allan.mydroid.globals.MyDroidGlobalService
import com.allan.mydroid.nanohttp.h5client.ClientWebSocket
import com.allan.mydroid.nanohttp.h5client.MsgParserReceiverMode
import com.allan.mydroid.nanohttp.h5client.MsgParserSendMode
import com.au.module_android.utils.logdNoFile
import fi.iki.elonen.NanoHTTPD.Response.Status
import fi.iki.elonen.NanoWSD
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.cancel
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.Executors

class MyDroidWSServer(port:Int) : NanoWSD(port) {
    companion object {
        const val HEARTBEAT_INTERVAL: Long = 60 * 1000
        const val WEBSOCKET_READ_TIMEOUT = HEARTBEAT_INTERVAL + 15 * 1000

        const val WS_CODE_CLOSE_BY_CLIENT = 1000

        const val PING_PAYLOAD_TEXT = "myDroid_ping"
        val PING_PAYLOAD = PING_PAYLOAD_TEXT.toByteArray()
    }

    private val executor = Executors.newSingleThreadExecutor()
    private val singleThreadDispatcher = executor.asCoroutineDispatcher()

    /**
     * 多个websocket客户端共享同一线程、scope
     */
    val heartbeatScope = CoroutineScope(singleThreadDispatcher)

    private val connections: MutableList<ClientWebSocket> = CopyOnWriteArrayList()

    private var currentColorIconIndex = 0
    private val colorIconList = listOf(
        com.allan.mydroid.R.color.client_send_1,
        com.allan.mydroid.R.color.client_send_2,
        com.allan.mydroid.R.color.client_send_3,
        com.allan.mydroid.R.color.client_send_4,
        com.allan.mydroid.R.color.client_send_5,
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

    fun addIntoConnections(websocket: ClientWebSocket) {
        MyDroidGlobalService.updateAliveTs("when new client add")
        connections.add(websocket)
        triggerConnectionsList()
    }

    fun removeFromConnections(websocket: ClientWebSocket) {
        connections.remove(websocket)
        triggerConnectionsList()
    }

    /**
     * 触达一下。
     */
    fun triggerConnectionsList() {
        val list = ArrayList<WebSocketClientInfo>()
        connections.forEach {
            list.add(WebSocketClientInfo(it.clientName, it.openTs, it.colorIcon))
        }
        list.sortByDescending { it.enterTs }
        MyDroidConst.clientListLiveData.setValueSafe(list)
    }

    override fun openWebSocket(handshake: IHTTPSession): WebSocket {
        val uri = handshake.uri
        val nextColorIcon = nextColorIcon()
        logdNoFile { "open web Socket handshake uri: $uri nextColorIcon $nextColorIcon" }
        //uri = uri.replaceFirst("/", "", true)
        val client = ClientWebSocket(handshake, this, nextColorIcon())
        val parser = when (MyDroidConst.myDroidMode) {
            MyDroidMode.Receiver -> MsgParserReceiverMode(client)
            MyDroidMode.Send -> MsgParserSendMode(client)
            MyDroidMode.Middle -> MsgParserSendMode(client)
            MyDroidMode.None,
            MyDroidMode.Image,
            MyDroidMode.Video -> MsgParserReceiverMode(client)
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
        heartbeatScope.cancel()
        executor.shutdown()
        MyDroidConst.clientListLiveData.setValueSafe(emptyList())
    }
}