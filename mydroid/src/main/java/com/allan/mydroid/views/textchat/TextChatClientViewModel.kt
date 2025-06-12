package com.allan.mydroid.views.textchat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.allan.mydroid.api.Api
import com.allan.mydroid.api.IpPortBean
import com.allan.mydroid.api.TEXT_CHAT_READ_WEBSOCKET_IP_PORT
import com.allan.mydroid.beans.WSChatMessageBean
import com.au.module_android.Globals
import com.au.module_android.json.toJsonString
import com.au.module_android.simplelivedata.NoStickLiveData
import com.au.module_android.utils.logd
import com.au.module_android.utils.loge
import kotlinx.coroutines.launch

class TextChatClientViewModel : ViewModel() {
    var wsClient:TextChatWsClient?= null

    fun isWSClientConnected() = wsClient?.isLive == true
    fun serverInfo() = if(wsClient != null) wsClient?.ip + ":" + wsClient?.port else ""

    val closedData = NoStickLiveData<String>()

    /**
     * 输入ip和port，这是看到server手机上的ip/port是http。
     * 需要请求获取websocket的ip和port。
     */
    fun connectServer(ip: String, port: Int,
                      cannotOpenBlock:(String)->Unit,
                      onTransferClientMsgCallback:((message: WSChatMessageBean)->Unit),
                      successOpenBlock:()->Unit) {
        Api.currentBaseUrl = "http://$ip:$port"
        logd { "currentBaseUrl ${Api.currentBaseUrl}" }
        viewModelScope.launch {
            try {
                val data = Api.requestResultData<IpPortBean>(TEXT_CHAT_READ_WEBSOCKET_IP_PORT)
                if (data != null) {
                    wsClient?.manualShutdown()
                    val newClient = TextChatWsClient(viewModelScope, ip, port, successOpenBlock)
                    newClient.onClosed = { reason->
                        closedData.setValueSafe(reason)
                    }
                    newClient.onTransferClientMsgCallback = onTransferClientMsgCallback
                    Api.connectWSServer(data.ip, data.port, newClient)
                    wsClient = newClient
                }
            } catch (e: Exception) {
                loge { "connectServer: " + e.message }
                cannotOpenBlock(e.message ?: Globals.getString(com.allan.mydroid.R.string.something_error))
            }
        }
    }

    fun send(bean : WSChatMessageBean) {
        wsClient?.sendText(bean.toJsonString())
    }

    fun shutdownWSClient() {
        wsClient?.manualShutdown()
    }
}