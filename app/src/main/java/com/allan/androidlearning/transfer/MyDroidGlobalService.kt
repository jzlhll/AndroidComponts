package com.allan.androidlearning.transfer

import android.app.Activity
import androidx.annotation.MainThread
import com.allan.androidlearning.transfer.benas.IpInfo
import com.allan.androidlearning.transfer.nanohttp.MyDroidHttpServer
import com.allan.androidlearning.transfer.nanohttp.MyDroidWebSocketServer
import com.allan.androidlearning.transfer.nanohttp.MyDroidWebSocketServer.Companion.WEBSOCKET_READ_TIMEOUT
import com.allan.androidlearning.transfer.views.MyDroidFragment
import com.allan.androidlearning.transfer.views.MyDroidReceiverFragment
import com.au.module_android.init.InterestActivityCallbacks
import com.au.module_android.simplelivedata.NoStickLiveData
import com.au.module_android.ui.FragmentShellActivity
import com.au.module_android.utils.logdNoFile
import com.au.module_android.utils.loge
import com.au.module_android.utils.logt
import com.au.module_androidui.toast.ToastBuilder
import fi.iki.elonen.NanoHTTPD
import kotlinx.coroutines.launch
import java.io.File
import java.io.IOException
import java.net.Inet4Address
import java.net.NetworkInterface
import java.net.ServerSocket


object MyDroidGlobalService : InterestActivityCallbacks() {
    val clientsChangeData = NoStickLiveData<Unit>()

    private val logicFragments = listOf<Class<*>>(
        MyDroidFragment::class.java,
        MyDroidReceiverFragment::class.java,
//        MyDroidTransferFragment::class.java,
    )
    /**
     * 传输的信息，类似log。
     */
    val onTransferInfoData = NoStickLiveData<String>()

    val onFileMergedData = NoStickLiveData<File>()

    /**
     * first是IP。second是Port。Third是websocket Port。
     */
    val ipPortData = NoStickLiveData<IpInfo?>()

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
    private fun startServer(errorCallback:(String)->Unit) {
        val p = findAvailablePort()
        val wsPort = findAvailableWsPort()
        httpServer = MyDroidHttpServer(ipPortData, p)
        websocketServer = MyDroidWebSocketServer(wsPort)

        try {
            logt { "start server with port: $p, wsPort: $wsPort" }
            httpServer?.start(NanoHTTPD.SOCKET_READ_TIMEOUT, false)
            websocketServer?.start(WEBSOCKET_READ_TIMEOUT.toInt(), false)

            isSuccessOpenServer = true
            val realValue = ipPortData.realValue ?: IpInfo("", null, null)
            realValue.httpPort = p
            realValue.webSocketPort = wsPort
            logt { "start server and websocket success and setPort $realValue" }
            ipPortData.setValueSafe(realValue)
        } catch (e: IOException) {
            val msg = "Port $p WsPort $wsPort occupied ${e.message}"
            loge { msg }
            errorCallback(msg)
        }
    }

    private fun stopServer() {
        httpServer?.closeAllConnections()
        websocketServer?.closeAllConnections()

        isSuccessOpenServer = false
    }

    override fun isLifeActivity(activity: Activity): Boolean {
        return activity is FragmentShellActivity && logicFragments.contains(activity.fragmentClass)
    }

    override fun onLifeOpen() {
    }

    override fun onLifeOpenEach() {
        logdNoFile { "on life open each" }
        getIpAddressAndStartServer()
    }

    override fun onLifeClose() {
        logdNoFile { "on life close." }
        stopServer()
        ipPortData.setValueSafe(null)
    }

    fun getIpAddressAndStartServer() {
        if (getIpAddress()) {
            if (!isSuccessOpenServer) {
                logdNoFile { "viewModel11 ${ipPortData.realValue}" }
                startServer{ msg->
                    scope.launch {
                        ToastBuilder()
                            .setOnTop()
                            .setIcon("error")
                            .setMessage(msg)
                            .toast()
                    }
                }
            }
        }
    }
    private fun getIpAddress() : Boolean {
        try {
            val interfaces = NetworkInterface.getNetworkInterfaces()
            while (interfaces.hasMoreElements()) {
                val netInterface = interfaces.nextElement()
                if (netInterface.displayName.equals("wlan0") || netInterface.name.startsWith("ap")) {
                    for (addr in netInterface.getInterfaceAddresses()) {
                        val inetAddr = addr.address
                        if (inetAddr is Inet4Address) {
                            val ip = inetAddr.hostAddress ?: "0.0.0.0"
                            val realValue = ipPortData.realValue ?: IpInfo("", null, null)
                            realValue.ip = ip
                            logt { "get IpAddress set ip portData $realValue" }
                            ipPortData.setValueSafe(realValue)
                            return true
                        }
                    }
                }
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
        ipPortData.setValueSafe(null)
        return false
    }
}