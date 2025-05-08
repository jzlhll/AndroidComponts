package com.allan.androidlearning.transfer

import androidx.annotation.MainThread
import androidx.lifecycle.ViewModel
import com.au.module_android.Globals
import com.au.module_android.simplelivedata.NoStickLiveData
import com.au.module_android.utils.getFileMD5
import com.au.module_android.utils.loge
import com.au.module_android.utils.logt
import fi.iki.elonen.NanoHTTPD
import kotlinx.coroutines.delay
import java.io.File
import java.io.IOException
import java.net.ServerSocket

class MyDroidServerViewModel : ViewModel() {
    init {
        logt { "MyDroidServerViewModel init" }
    }
    val ipPortData = NoStickLiveData<Pair<String, String>>()
    var isSuccessOpenServer = false

    private var httpServer: MyDroidHttpServer? = null
    val magicCode = String.format("%04d", (Math.random() * 10000 + 1).toInt())
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
    fun startServer(errorCallback:(String)->Unit) {
        val p = findAvailablePort()
        httpServer = MyDroidHttpServer(p, magicCode)
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

    suspend fun loadFileList() : List<MergedFileInfo>{
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
}