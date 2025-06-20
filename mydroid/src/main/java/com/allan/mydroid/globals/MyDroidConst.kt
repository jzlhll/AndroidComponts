package com.allan.mydroid.globals

import com.allan.mydroid.beansinner.IpInfo
import com.allan.mydroid.api.MyDroidMode
import com.allan.mydroid.beansinner.ReceivingFileInfo
import com.allan.mydroid.beansinner.UriRealInfoEx
import com.allan.mydroid.beansinner.WebSocketClientInfo
import com.au.module_android.Globals
import com.au.module_android.json.fromJson
import com.au.module_android.json.fromJsonList
import com.au.module_android.json.fromJsonLv2
import com.au.module_android.json.toJsonString
import com.au.module_android.simplelivedata.NoStickLiveData
import com.au.module_android.simplelivedata.RealValueLiveData
import com.au.module_android.simplelivedata.asNoStickLiveData
import com.au.module_android.utils.launchOnThread
import com.au.module_android.utils.logdNoFile
import com.au.module_cached.AppDataStore
import java.io.File

object MyDroidConst {
    var serverIsOpen = false
    var currentDroidMode = MyDroidMode.None

    /**
     * 用于通知界面更新。告知有多少通过WS接入的client。
     */
    val clientListLiveData = NoStickLiveData<List<WebSocketClientInfo>>()

    /**
     * 文件合并成功的通知。必须使用observerNoStick。
     */
    val onFileMergedData = NoStickLiveData<File>()

    /**
     * first是IP。second是Port。Third是websocket Port。
     */
    val ipPortData = NoStickLiveData<IpInfo?>()

    /**
     * 通过ipPort组装的服务器名称。
     */
    val serverName:String
        get() {
            val ipPort = ipPortData.realValue
            if (ipPort == null) {
                return Globals.getString(com.allan.mydroid.R.string.server) + "(Error)"
            }
            return Globals.getString(com.allan.mydroid.R.string.server) + "(${ipPort.ip}:${ipPort.httpPort})"
        }

    /**
     * 接收模式下的接收文件的进度
     * Map<filename-md5, ReceivingFileInfo>
     */
    val receiverProgressData = NoStickLiveData<Map<String, ReceivingFileInfo>>()

    /**
     * 这个就全局存活。不重启app不做处理。
     * 从shareReceiver activity处接收数据
     * key是uriUuid
     */
    val sendUriMap: RealValueLiveData<HashMap<String, UriRealInfoEx>> = NoStickLiveData(loadCacheSendUriMap())

    private fun loadCacheSendUriMap() : HashMap<String, UriRealInfoEx>{
        var time = System.currentTimeMillis()
        try {
            val json = AppDataStore.readBlocked("mydroid_sendUriMap", "")
            logdNoFile{"load cache sendUriMap json: $json"}
            if (json.isEmpty()) {
                return hashMapOf()
            }
            val list: HashMap<String, UriRealInfoEx>? = json.fromJson()
            logdNoFile{"load cache sendUriMap json2: $list"}
            return list ?: hashMapOf()
        } finally {
            time = System.currentTimeMillis() - time
            logdNoFile{"load cache sendUriMap time: $time"}
        }
    }

    fun updateSendUriMap(map: HashMap<String, UriRealInfoEx>?) {
        val fixMap = map ?: hashMapOf()
        sendUriMap.asNoStickLiveData().setValueSafe(fixMap)
        Globals.mainScope.launchOnThread {
            AppDataStore.save("mydroid_sendUriMap", fixMap.toJsonString())
        }
    }

    val aliveStoppedData = NoStickLiveData<Unit>()
}