package com.allan.androidlearning.transfer

import android.app.Activity
import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Bundle
import androidx.annotation.MainThread
import com.allan.androidlearning.activities2.MyDroidFragment
import com.allan.androidlearning.activities2.MyDroidTransferFragment
import com.allan.androidlearning.transfer.MyDroidWebSocketServer.Companion.WEBSOCKET_READ_TIMEOUT
import com.allan.androidlearning.transfer.benas.IpInfo
import com.allan.androidlearning.transfer.benas.MergedFileInfo
import com.au.module_android.Globals
import com.au.module_android.simplelivedata.NoStickLiveData
import com.au.module_android.ui.FragmentShellActivity
import com.au.module_android.utils.getFileMD5
import com.au.module_android.utils.launchOnThread
import com.au.module_android.utils.logdNoFile
import com.au.module_android.utils.loge
import com.au.module_android.utils.logt
import com.au.module_androidui.toast.ToastBuilder
import com.au.module_cached.AppDataStore
import fi.iki.elonen.NanoHTTPD
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.io.IOException
import java.net.Inet4Address
import java.net.ServerSocket
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

object MyDroidGlobalService : Application.ActivityLifecycleCallbacks{
    val scope = MainScope()
    private var lifeCount = 0
    private var mConnectCb:ConnectivityManager.NetworkCallback? = null

    private val logicFragments = listOf<Class<*>>(
        MyDroidFragment::class.java,
        MyDroidTransferFragment::class.java,
//        MyDroidTransferFragment::class.java,
    )

    val clientsChangeData = NoStickLiveData<Unit>()

    /**
     * 传输的信息，类似log。
     */
    val onTransferInfoData = NoStickLiveData<String>()

    val onFileMergedData = NoStickLiveData<File>()

    /**
     * first是IP。second是Port。Third是websocket Port。
     */
    val ipPortData = NoStickLiveData<IpInfo>()

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

    fun formatSize(bytes: Long): String {
        val units = listOf("B", "KB", "MB", "GB")
        var size = bytes.toDouble()
        var unitIndex = 0

        while (size >= 1024 && unitIndex < units.size - 1) {
            size /= 1024
            unitIndex++
        }
        return "%.2f %s".format(size, units[unitIndex])
    }

    fun loadFileListAsync(cb:(List<MergedFileInfo>)->Unit) {
        scope.launchOnThread {
            cb(loadFileList())
        }
    }

    private suspend fun loadFileList() : List<MergedFileInfo>{
        delay(0)
        val nanoMergedDir = File(nanoTempCacheMergedDir())
        val fileList = ArrayList<MergedFileInfo>()
        if (nanoMergedDir.exists()) {
            nanoMergedDir.listFiles()?.forEach {
                fileList.add(MergedFileInfo(it, getFileMD5(it.absolutePath), formatSize(it.length())))
            }
        }
        fileList.sortByDescending { it.file.lastModified() }
        return fileList
    }

    suspend fun loadExportHistory() : String {
        return AppDataStore.read("mydroidExportHistory", "")
    }

    suspend fun writeNewExportHistory(info:String) {
        val old = loadExportHistory()
        val splits = old.split("\n")
        val fixOld = if (splits.size > 100) {
            val cutList = splits.subList(0, 80)
            cutList.joinToString("\n")
        } else {
            old
        }

        // 获取当前时间戳
        val currentTimeMillis = System.currentTimeMillis()
        // 定义时间格式（例如：2023年10月05日 14:30）
        val formatter = DateTimeFormatter
            .ofPattern("yyyy年MM月dd日 HH:mm")
            .withZone(ZoneId.systemDefault()) // 使用系统默认时区
        // 格式化为字符串
        val formattedTime = formatter.format(Instant.ofEpochMilli(currentTimeMillis))

        AppDataStore.save("mydroidExportHistory", "($formattedTime) $info\n$fixOld")
    }

    private fun startServerInner() {
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

    private fun connectRegister() {
        if (mConnectCb == null) {
            mConnectCb = object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) {
                    val connectivityManager = Globals.app.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
                    val linkProperties = connectivityManager.getLinkProperties(network)
                    val sb = StringBuilder()
                    linkProperties?.linkAddresses?.forEach { linkAddress->
                        if (linkAddress.address is Inet4Address) {
                            sb.append(linkAddress.address.hostAddress)
                        }
                    }

                    val realValue = ipPortData.realValue ?: IpInfo("", null, null)
                    realValue.ip = sb.toString()
                    ipPortData.setValueSafe(realValue)
                }
            }
            val networkRequest = NetworkRequest.Builder()
                .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                .build()
            val connectivityManager = Globals.app.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            connectivityManager.registerNetworkCallback(networkRequest, mConnectCb!!)
        }
    }

    private fun connectUnRegister() {
        mConnectCb ?: return
        val connectivityManager = Globals.app.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        connectivityManager.unregisterNetworkCallback(mConnectCb!!)
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
    }

    override fun onActivityDestroyed(activity: Activity) {
    }

    override fun onActivityPaused(activity: Activity) {
    }

    override fun onActivityResumed(activity: Activity) {
    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
    }

    override fun onActivityStarted(activity: Activity) {
        logdNoFile { "onCreat1 $activity" }
        if (activity is FragmentShellActivity
            && logicFragments.contains(activity.fragmentClass)) {
            lifeCount++

            connectRegister()
            if (!isSuccessOpenServer) {
                startServerInner()
            }
        }
    }

    override fun onActivityStopped(activity: Activity) {
        if (activity is FragmentShellActivity
            && logicFragments.contains(activity.fragmentClass)) {
            lifeCount--
        }
        if (lifeCount == 0) {
            scope.cancel()
            stopServer()

            connectUnRegister()
        }
    }
}