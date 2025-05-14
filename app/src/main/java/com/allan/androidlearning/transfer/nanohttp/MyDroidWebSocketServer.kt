package com.allan.androidlearning.transfer.nanohttp

import androidx.annotation.ColorRes
import com.allan.androidlearning.transfer.MyDroidGlobalService
import com.allan.androidlearning.transfer.benas.WebSocketClientInfo
import com.au.module_android.utils.logdNoFile
import fi.iki.elonen.NanoHTTPD.Response.Status
import fi.iki.elonen.NanoWSD
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.cancel
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.Executors

class MyDroidWebSocketServer(port:Int) : NanoWSD(port) {
    companion object {
        const val HEARTBEAT_INTERVAL: Long = 60 * 1000
        const val WEBSOCKET_READ_TIMEOUT = HEARTBEAT_INTERVAL + 15 * 1000

        const val PING_PAYLOAD_TEXT = "p"
        val PING_PAYLOAD = PING_PAYLOAD_TEXT.toByteArray()
    }

    private val executor = Executors.newSingleThreadExecutor()
    private val singleThreadDispatcher = executor.asCoroutineDispatcher()

    /**
     * 多个websocket客户端共享同一线程、scope
     */
    val heartbeatScope = CoroutineScope(singleThreadDispatcher)

    private val connections: MutableList<MyDroidWebSocket> = CopyOnWriteArrayList()

    private var currentColorIconIndex = 0
    private val colorIconList = listOf(
        com.allan.androidlearning.R.color.client_send_1,
        com.allan.androidlearning.R.color.client_send_2,
        com.allan.androidlearning.R.color.client_send_3,
        com.allan.androidlearning.R.color.client_send_4,
        com.allan.androidlearning.R.color.client_send_5,
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

    fun addIntoConnections(websocket:MyDroidWebSocket) {
        connections.add(websocket)
        triggerConnectionsList()
    }

    fun removeFromConnections(websocket: MyDroidWebSocket) {
        connections.remove(websocket)
        triggerConnectionsList()
    }

    /**
     * 触达一下。
     */
    fun triggerConnectionsList() {
        val list = ArrayList<WebSocketClientInfo>()
        connections.forEach {
            list.add(WebSocketClientInfo(it.remoteIpStr ?: "--", it.clientTellName, it.openTs, it.colorIcon))
        }
        list.sortByDescending { it.enterTs }
        MyDroidGlobalService.clientListLiveData.setValueSafe(list)
    }

    override fun openWebSocket(handshake: IHTTPSession): WebSocket {
        val uri = handshake.uri
        val nextColorIcon = nextColorIcon()
        logdNoFile { "open web Socket handshake uri: $uri nextColorIcon $nextColorIcon" }
        //uri = uri.replaceFirst("/", "", true)
        return MyDroidWebSocket(handshake, this, nextColorIcon())
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
    }
}