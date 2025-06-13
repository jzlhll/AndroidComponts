package com.allan.mydroid.globals

import android.app.Activity
import android.os.SystemClock
import androidx.annotation.MainThread
import com.allan.mydroid.beansinner.IpInfo
import com.allan.mydroid.nanohttp.MyDroidHttpServer
import com.allan.mydroid.nanohttp.WebsocketServer
import com.allan.mydroid.views.AbsLiveFragment
import com.au.module_android.Globals
import com.au.module_android.init.IInterestLife
import com.au.module_android.init.InterestActivityCallbacks
import com.au.module_android.ui.FragmentShellActivity
import com.au.module_android.utils.clearDirOldFiles
import com.au.module_android.utils.launchOnIOThread
import com.au.module_android.utils.logd
import com.au.module_android.utils.loge
import com.au.module_android.utils.logt
import fi.iki.elonen.NanoHTTPD
import java.io.IOException
import java.net.ServerSocket

object MyDroidGlobalService : InterestActivityCallbacks() {
    private var httpServer: MyDroidHttpServer?= null
    var websocketServer: WebsocketServer?= null

    private var mLastHttpServerPort = 15555
    private var mLastWsServerPort = 16555

    private const val ALIVE_DEAD_TIME = 5 * 60 * 1000L //N分钟不活跃主动关闭服务
    private const val ALIVE_TS_TOO_FAST = 6 * 1000L //n秒内的更新，只干一次就好。很严谨来讲需要考虑再次post，但是由于相去很远忽略这几秒。

    /**
     * 如果很久没有从html端请求接口，则主动关闭服务
     */
    private var aliveTs = SystemClock.elapsedRealtime()
    private val aliveCheckRun = Runnable {
        if (SystemClock.elapsedRealtime() - aliveTs > ALIVE_DEAD_TIME) {
            logd { "alive Ts timeout, stop server." }
            MyDroidConst.aliveStoppedData.setValueSafe(Unit)
        }
    }

    fun updateAliveTs(from:String) {
        val cur = SystemClock.elapsedRealtime()
        if (cur - aliveTs < ALIVE_TS_TOO_FAST) {
            logd { "Update alive Ts too fast ignore: $from" }
            return
        }
        aliveTs = cur
        logd { "Update alive Ts: $from" }
        Globals.mainHandler.removeCallbacks(aliveCheckRun)
        Globals.mainHandler.postDelayed(aliveCheckRun, ALIVE_DEAD_TIME)
    }

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
        websocketServer = WebsocketServer(wsPort)

        logd { "start server with port: $p, wsPort: $wsPort" }

        try {
            httpServer?.start(NanoHTTPD.SOCKET_READ_TIMEOUT, false)
            websocketServer?.start(WebsocketServer.WEBSOCKET_READ_TIMEOUT.toInt(), false)

            MyDroidConst.serverIsOpen = true
            val realValue = MyDroidConst.ipPortData.realValue ?: IpInfo("", null, null)
            realValue.httpPort = p
            realValue.webSocketPort = wsPort
            logt { "start server and websocket success and setPort $realValue" }
            MyDroidConst.ipPortData.setValueSafe(realValue)

            //检查并清理过期temp文件
            Globals.mainScope.launchOnIOThread {
                clearDirOldFiles(nanoTempCacheChunksDir())
                clearDirOldFiles(cacheImportCopyDir())
            }
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
        MyDroidConst.serverIsOpen = false
    }

    //////////////////////////life////

    private val lifeObservers = ArrayList<IInterestLife>().apply {
        add(MyDroidNetworkObserver())
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
        updateAliveTs("when liveOpen")
    }

    override fun onLifeOpenEach() {
        logd { "on life open each" }
        for (life in lifeObservers) {
            life.onLifeOpenEach()
        }
        updateAliveTs("when liveOpenEach")
    }

    override fun onLifeClose() {
        logd { "on life close." }
        stopServer()
        MyDroidConst.ipPortData.setValueSafe(null)
        MyDroidConst.receiverProgressData.setValueSafe(emptyMap())
        for (life in lifeObservers) {
            life.onLifeClose()
        }
        Globals.mainHandler.removeCallbacks(aliveCheckRun)
    }

    override fun isLifeActivity(activity: Activity): Boolean {
        val isActivity = activity is FragmentShellActivity
        if (!isActivity) {
            return false
        }
        val frgClass = activity.fragmentClass
        return AbsLiveFragment::class.java.isAssignableFrom(frgClass)
    }
}