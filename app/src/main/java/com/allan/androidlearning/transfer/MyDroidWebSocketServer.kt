package com.allan.androidlearning.transfer

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
        const val HEARTBEAT_INTERVAL: Long = 45 * 1000
        const val WEBSOCKET_READ_TIMEOUT = HEARTBEAT_INTERVAL + 15 * 1000
    }

    private val executor = Executors.newSingleThreadExecutor()
    private val singleThreadDispatcher = executor.asCoroutineDispatcher()
    val heartbeatScope = CoroutineScope(singleThreadDispatcher)

    private val connections: MutableList<MyDroidWebSocket> = CopyOnWriteArrayList()

    fun addIntoConnections(websocket:MyDroidWebSocket) {
        connections.add(websocket)
    }

    fun removeFromConnections(websocket: MyDroidWebSocket) {
        connections.remove(websocket)
    }

    fun getAllClientsInfo() : String {
        val sb = StringBuilder()
        connections.forEach {
            sb.append(it.remoteIpStr).append("@").append(it.clientRandomCode)
        }
        return sb.toString()
    }

    override fun openWebSocket(handshake: IHTTPSession): WebSocket {
        var uri = handshake.uri
        logdNoFile { "open web Socket handshake uri: $uri" }
        uri = uri.replaceFirst("/", "", true)
        return MyDroidWebSocket(handshake, this)
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