package com.allan.mydroid.nanohttp

import com.allan.mydroid.api.MyDroidMode
import com.allan.mydroid.beans.WSChatMessageBean
import com.allan.mydroid.beansinner.WebSocketClientInfo
import com.allan.mydroid.globals.MyDroidConst
import com.allan.mydroid.globals.MyDroidGlobalService
import com.allan.mydroid.nanohttp.wsmsger.WebsocketNoneModeMessenger
import com.allan.mydroid.nanohttp.wsmsger.WebsocketSendModeMessenger
import com.allan.mydroid.nanohttp.wsmsger.WebsocketTextChatModeMessenger
import com.au.module_android.json.toJsonString
import com.au.module_android.utils.isMainThread
import com.au.module_android.utils.launchOnThread
import com.au.module_android.utils.launchOnUi
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
        const val HEARTBEAT_INTERVAL: Long = 15 * 1000 //45 * 1000

        /**
         * 服务端开启的一条webSocket通道的最长心跳时间。因此设置的心跳要比这个值短。
         */
        const val WEBSOCKET_READ_TIMEOUT = 22 * 1000 //HEARTBEAT_INTERVAL * 2 + HEARTBEAT_INTERVAL / 2

        const val WS_CODE_CLOSE_BY_CLIENT = 1000
    }

    private val executor = Executors.newSingleThreadExecutor()
    private val singleThreadDispatcher = executor.asCoroutineDispatcher()

    /**
     * 多个websocket客户端共享同一线程、scope
     */
    val scope = CoroutineScope(singleThreadDispatcher)

    private val clients: MutableList<WebsocketClientInServer> = CopyOnWriteArrayList()

    /**
     * 将客户端的消息往外通知，主要是给到UI做追加显示
     */
    var onTransferClientMsgCallback:((message: WSChatMessageBean)->Unit)? = null

    fun addIntoConnections(websocket: WebsocketClientInServer) {
        MyDroidGlobalService.updateAliveTs("when new client add")
        clients.add(websocket)
        triggerConnectionsList()
    }

    fun removeFromConnections(websocket: WebsocketClientInServer) {
        clients.remove(websocket)
        triggerConnectionsList()
    }

    /**
     * 触达一下。
     */
    fun triggerConnectionsList() {
        val list = ArrayList<WebSocketClientInfo>()
        clients.forEach {
            list.add(WebSocketClientInfo(it.clientName, it.platform, it.openTs, it.color))
        }
        list.sortByDescending { it.enterTs }
        logdNoFile { "after change websocket client size ${list.size}" }
        MyDroidConst.clientListLiveData.setValueSafe(list)
    }

    override fun openWebSocket(handshake: IHTTPSession): WebSocket {
        val uri = handshake.uri
        val nextColor = nextColor()
        logdNoFile { "open web Socket handshake uri: $uri nextColor $nextColor" }
        //uri = uri.replaceFirst("/", "", true)
        val client = WebsocketClientInServer(handshake, this, nextColor)
        val parser = when (MyDroidConst.currentDroidMode) {
            MyDroidMode.Receiver -> WebsocketNoneModeMessenger(client) //接受文件，都走http而非ws。所以给空实现即可。
            MyDroidMode.TextChat -> WebsocketTextChatModeMessenger(client)
            MyDroidMode.Send -> WebsocketSendModeMessenger(client)
            MyDroidMode.Middle,
            MyDroidMode.None,
            MyDroidMode.Image,
            MyDroidMode.Video -> WebsocketNoneModeMessenger(client)
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

    //textChat////
    /**
     * 当新的客户端消息来了。
     */
    fun onTextChatMessageArrived(client:WebsocketClientInServer, message: WSChatMessageBean) {
        logdNoFile { "${client.clientName} arrived $message" }
        message.timestamp = System.currentTimeMillis()
        message.setStatusToDelivered()
        serverSendTextChatMessage(message)
    }

    /**
     * 服务器发送消息给所有客户端
     */
    fun serverSendTextChatMessage(message: WSChatMessageBean) {
        if (isMainThread) {
            scope.launchOnThread {
                val json = message.toJsonString()
                clients.forEach { c->
                    c.send(json)
                }

                scope.launchOnUi {
                    onTransferClientMsgCallback?.invoke(message)
                }
            }
        } else {
            val json = message.toJsonString()
            clients.forEach { c->
                c.send(json)
            }
            scope.launchOnUi {
                onTransferClientMsgCallback?.invoke(message)
            }
        }
    }

    //color示例////
    private val colorIconList = listOf(
        "#3D7C42",
        "#FF9E80",
        "#6A3188",
        "#895DF8",
        "#CEBE55",
        "#5CCE99",
        "#ce626e",
        "#71a3ce",
    )
    private var currentColorIconIndex = (Math.random() * colorIconList.size).toInt()

    private fun nextColor() : String{
        return if(currentColorIconIndex >= colorIconList.size) {
            currentColorIconIndex = 0
            colorIconList[currentColorIconIndex]
        } else {
            colorIconList[currentColorIconIndex++]
        }
    }
}