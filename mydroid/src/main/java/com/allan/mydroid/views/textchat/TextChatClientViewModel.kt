package com.allan.mydroid.views.textchat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.allan.mydroid.api.Api
import com.allan.mydroid.benas.READ_WEBSOCKET_IP_PORT
import com.au.module_android.simplelivedata.NoStickLiveData
import com.au.module_android.utils.logd
import kotlinx.coroutines.launch

class TextChatClientViewModel : ViewModel() {
    var isConnectingServerData = NoStickLiveData<Boolean>()

    /**
     * 输入ip和port，这是看到server手机上的ip/port是http。
     * 需要请求获取websocket的ip和port。
     */
    fun connectServer(ip: String, port: Int) {
        viewModelScope.launch {
            val result = Api.requestApi(READ_WEBSOCKET_IP_PORT)
            logd { "result $result" }
        }
    }
}