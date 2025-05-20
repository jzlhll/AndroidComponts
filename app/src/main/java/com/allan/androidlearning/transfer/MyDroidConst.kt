package com.allan.androidlearning.transfer

import com.allan.androidlearning.transfer.benas.IpInfo
import com.allan.androidlearning.transfer.benas.MyDroidMode
import com.allan.androidlearning.transfer.benas.UriRealInfoEx
import com.allan.androidlearning.transfer.benas.WebSocketClientInfo
import com.au.module_android.simplelivedata.NoStickLiveData
import com.au.module_android.simplelivedata.RealValueLiveData
import java.io.File

object MyDroidConst {
    val serverIsOpen: RealValueLiveData<Boolean> = NoStickLiveData(false)

    /**
     * 用于通知界面更新。告知有多少通过WS接入的client。
     */
    val clientListLiveData = NoStickLiveData<List<WebSocketClientInfo>>()

    /**
     * 当前的模式
     */
    val myDroidModeData = NoStickLiveData<MyDroidMode>()

    /**
     * 传输的信息，类似log。
     */
    val onTransferInfoData = NoStickLiveData<String>()

    val onFileMergedData = NoStickLiveData<File>()

    /**
     * first是IP。second是Port。Third是websocket Port。
     */
    val ipPortData = NoStickLiveData<IpInfo?>()

    /**
     * 从shareReceiver activity处接收数据
     * key是uriUuid
     */
    val shareReceiverUriMap: RealValueLiveData<HashMap<String, UriRealInfoEx>> = NoStickLiveData(hashMapOf())
}