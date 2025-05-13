package com.allan.androidlearning.transfer

import android.app.Activity
import androidx.annotation.MainThread
import androidx.lifecycle.LiveData
import com.allan.androidlearning.transfer.benas.IpInfo
import com.allan.androidlearning.transfer.nanohttp.MyDroidHttpServer
import com.allan.androidlearning.transfer.nanohttp.MyDroidWebSocketServer
import com.allan.androidlearning.transfer.nanohttp.MyDroidWebSocketServer.Companion.WEBSOCKET_READ_TIMEOUT
import com.allan.androidlearning.transfer.views.MyDroidFragment
import com.allan.androidlearning.transfer.views.MyDroidReceiverFragment
import com.au.module_android.init.IInterestLife
import com.au.module_android.init.InterestActivityCallbacks
import com.au.module_android.simplelivedata.NoStickLiveData
import com.au.module_android.ui.FragmentShellActivity
import com.au.module_android.utils.logd
import com.au.module_android.utils.logdNoFile
import com.au.module_android.utils.loge
import com.au.module_android.utils.logt
import fi.iki.elonen.NanoHTTPD
import java.io.File
import java.io.IOException
import java.net.ServerSocket

object MyDroidGlobalService : InterestActivityCallbacks() {
    private val _onTransferInfoData = NoStickLiveData<String>()
    /**
     * 传输的信息，类似log。
     */
    val onTransferInfoData : LiveData<String> = _onTransferInfoData

    private val _onFileMergedData = NoStickLiveData<File>()
    val onFileMergedData: LiveData<File> = _onFileMergedData

    private val _ipPortData = NoStickLiveData<IpInfo?>()
    val ipPortOrigData: NoStickLiveData<IpInfo?>
        get() = _ipPortData
    /**
     * first是IP。second是Port。Third是websocket Port。
     */
    val ipPortData: LiveData<IpInfo?> = _ipPortData

    var fileExportSuccessCallbacks = ArrayList<((info:String, exportFileStr:String)->Unit)>()
    var fileExportFailCallbacks = ArrayList<((String)->Unit)>()

    var isSuccessOpenServer = false

    private var httpServer: MyDroidHttpServer?= null
    private var websocketServer: MyDroidWebSocketServer?= null

    private var mLastHttpServerPort = 10595
    private var mLastWsServerPort = 15595

    fun findAvailablePort(): Int {
        while (mLastHttpServerPort < 65535) {
            try {
                ServerSocket(mLastHttpServerPort).close()
                return mLastHttpServerPort
            } catch (_: IOException) { mLastHttpServerPort++ }
        }
        return -1
    }

    fun findAvailableWsPort(): Int {
        while (mLastWsServerPort < 65535) {
            try {
                ServerSocket(mLastWsServerPort).close()
                return mLastWsServerPort
            } catch (_: IOException) { mLastWsServerPort++ }
        }
        return -1
    }

    @MainThread
    fun startServer(errorCallback:(String)->Unit) {
        val p = findAvailablePort()
        val wsPort = findAvailableWsPort()
        httpServer = MyDroidHttpServer(p)
        websocketServer = MyDroidWebSocketServer(wsPort)
        logd { "start server with port: $p, wsPort: $wsPort" }

        try {
            httpServer?.start(NanoHTTPD.SOCKET_READ_TIMEOUT, false)
            websocketServer?.start(WEBSOCKET_READ_TIMEOUT.toInt(), false)

            isSuccessOpenServer = true
            val realValue = _ipPortData.realValue ?: IpInfo("", null, null)
            realValue.httpPort = p
            realValue.webSocketPort = wsPort
            logt { "start server and websocket success and setPort $realValue" }
            _ipPortData.setValueSafe(realValue)
        } catch (e: IOException) {
            val msg = "Port $p WsPort $wsPort occupied ${e.message}"
            loge { msg }
            errorCallback(msg)
        }
    }

    fun stopServer() {
        logd { ">>>stop server." }
        httpServer?.closeAllConnections()
        websocketServer?.closeAllConnections()
        isSuccessOpenServer = false
    }

    //////////////////////////life////

    private val lifeObservers = ArrayList<IInterestLife>().apply {
        addObserverEarly(MyDroidNetworkObserver())
    }

    override fun isLifeActivity(activity: Activity): Boolean {
        return activity is FragmentShellActivity && logicFragments.contains(activity.fragmentClass)
    }

    /**
     * 不做内部list保险。请在application或者最早接入的地方接入。
     */
    fun addObserverEarly(interestLife: IInterestLife) {
        if(!lifeObservers.contains(interestLife)) lifeObservers.add(interestLife)
    }

    override fun onLifeOpen() {
        for (life in lifeObservers) {
            life.onLifeOpen()
        }
    }

    override fun onLifeOpenEach() {
        logdNoFile { "on life open each" }
        for (life in lifeObservers) {
            life.onLifeOpenEach()
        }
    }

    override fun onLifeClose() {
        logdNoFile { "on life close." }
        stopServer()
        _ipPortData.setValueSafe(null)
        for (life in lifeObservers) {
            life.onLifeClose()
        }
    }

    private val logicFragments = listOf<Class<*>>(
        MyDroidFragment::class.java,
        MyDroidReceiverFragment::class.java,
//        MyDroidTransferFragment::class.java,
    )

}