package com.allan.androidlearning.transfer

import androidx.annotation.MainThread
import androidx.lifecycle.ViewModel
import com.au.module_android.simplelivedata.NoStickLiveData
import com.au.module_android.utils.loge
import com.au.module_android.utils.logt
import fi.iki.elonen.NanoHTTPD
import java.io.IOException
import java.net.ServerSocket

class MyDroidServerViewModel : ViewModel() {
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
            logt { "ipport111 " + ipPortData.realValue + ", " + target }
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
}