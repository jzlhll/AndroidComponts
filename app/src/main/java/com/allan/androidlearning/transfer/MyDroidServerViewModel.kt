package com.allan.androidlearning.transfer

import androidx.annotation.MainThread
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.allan.androidlearning.transfer.MyDroidWebSocketServer.Companion.WEBSOCKET_READ_TIMEOUT
import com.allan.androidlearning.transfer.benas.IpInfo
import com.allan.androidlearning.transfer.benas.MergedFileInfo
import com.au.module_android.simplelivedata.NoStickLiveData
import com.au.module_android.utils.getFileMD5
import com.au.module_android.utils.launchOnThread
import com.au.module_android.utils.loge
import com.au.module_android.utils.logt
import com.au.module_cached.AppDataStore
import fi.iki.elonen.NanoHTTPD
import kotlinx.coroutines.delay
import java.io.File
import java.io.IOException
import java.net.ServerSocket
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class MyDroidServerViewModel : ViewModel() {
    init {
        logt { "MyDroidServerViewModel init" }
    }

    /**
     * first是IP。second是Port。Third是websocket Port。
     */
    val ipPortData = NoStickLiveData<IpInfo>()
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
    fun startServer(transferInfoCallback:((String)->Unit)?, fileMergedSucCallback:(File)->Unit, errorCallback:(String)->Unit) {
        val p = findAvailablePort()
        val wsPort = findAvailableWsPort()
        httpServer = MyDroidHttpServer(ipPortData, p, fileMergedSucCallback).also {
            it.transferInfoCallback = transferInfoCallback
        }
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

    fun stopServer() {
        httpServer?.closeAllConnections()
        websocketServer?.closeAllConnections()
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
        viewModelScope.launchOnThread {
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
}