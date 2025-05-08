package com.allan.androidlearning.transfer

import androidx.annotation.MainThread
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
    val ipPortData = NoStickLiveData<Pair<String, String>>()
    var isSuccessOpenServer = false

    private var httpServer: MyDroidHttpServer? = null
    private var mLastRandomServerPort = 10595

    fun findAvailablePort(): Int {
        while (mLastRandomServerPort < 65535) {
            try {
                ServerSocket(mLastRandomServerPort).close()
                return mLastRandomServerPort
            } catch (_: IOException) { mLastRandomServerPort++ }
        }
        return -1
    }

    @MainThread
    fun startServer(transferInfoCallback:((String)->Unit)?, fileMergedSucCallback:(File)->Unit, errorCallback:(String)->Unit) {
        val p = findAvailablePort()
        httpServer = MyDroidHttpServer(p, fileMergedSucCallback).also {
            it.transferInfoCallback = transferInfoCallback
        }
        try {
            logt { "start server with port: $p..." }
            httpServer?.start(NanoHTTPD.SOCKET_READ_TIMEOUT, false)
            isSuccessOpenServer = true
            val ip = ipPortData.realValue?.first ?: ""
            val target = ip to "" + p
            ipPortData.setValueSafe(target)
        } catch (e: IOException) {
            val msg = "Port $p occupied ${e.message}"
            loge { msg }
            errorCallback(msg)
        }
    }

    fun stopServer() {
        httpServer?.closeAllConnections()
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