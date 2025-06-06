package com.allan.mydroid.globals

import com.allan.mydroid.benas.IpInfo
import com.allan.mydroid.benas.MyDroidMode
import com.allan.mydroid.benas.ReceivingFileInfo
import com.allan.mydroid.benas.UriRealInfoEx
import com.allan.mydroid.benas.WebSocketClientInfo
import com.au.module_android.simplelivedata.NoStickLiveData
import com.au.module_android.simplelivedata.RealValueLiveData
import java.io.File

object MyDroidConst {
    var serverIsOpen = false
    var myDroidMode = MyDroidMode.None

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
     * 接收模式下的接收文件的进度
     * Map<filename-md5, ReceivingFileInfo>
     */
    val receiverProgressData = NoStickLiveData<Map<String, ReceivingFileInfo>>()

    /**
     * 这个就全局存活。不重启app不做处理。
     * 从shareReceiver activity处接收数据
     * key是uriUuid
     */
    val sendUriMap: RealValueLiveData<HashMap<String, UriRealInfoEx>> = NoStickLiveData(hashMapOf())

    val aliveStoppedData = NoStickLiveData<Unit>()
}