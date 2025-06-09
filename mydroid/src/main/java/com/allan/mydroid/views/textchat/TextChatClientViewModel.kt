package com.allan.mydroid.views.textchat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.allan.mydroid.api.Api
import com.allan.mydroid.api.IpPortBean
import com.allan.mydroid.benas.READ_WEBSOCKET_IP_PORT
import com.au.module_android.utils.logd
import com.au.module_android.utils.loge
import kotlinx.coroutines.launch

class TextChatClientViewModel : ViewModel() {
    private var wsClient:TextChatWsClient?= null

    fun isWSClientConnected() = wsClient?.isLive == true
    fun serverInfo() = if(wsClient != null) wsClient?.ip + ":" + wsClient?.port else ""

    /**
     * 输入ip和port，这是看到server手机上的ip/port是http。
     * 需要请求获取websocket的ip和port。
     */
    fun connectServer(ip: String, port: Int, successOpenBlock:()->Unit) {
        Api.currentBaseUrl = "http://$ip:$port"
        logd { "currentBaseUrl ${Api.currentBaseUrl}" }
        viewModelScope.launch {
            try {
                val data = Api.requestResultData<IpPortBean>(READ_WEBSOCKET_IP_PORT)
                logd { "data $data" }
                if (data != null) {
                    wsClient?.shutdown()
                    val newClient = TextChatWsClient(ip, port, successOpenBlock)
                    Api.connectWSServer(data.ip, data.port, newClient)
                    wsClient = newClient
                }
            } catch (e: Exception) {
                loge { "connectServer: " + e.message }
            }
        }
    }

    fun shutdownWSClient() {
        wsClient?.shutdown()
    }
}