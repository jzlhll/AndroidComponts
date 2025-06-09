package com.allan.mydroid.api

import com.allan.mydroid.nanohttp.MyDroidWSServer
import com.au.module_cached.delegate.AppDataStoreLongCache
import com.au.module_okhttp.creator.AbsOkhttpApi
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.WebSocketListener
import java.util.concurrent.TimeUnit


object Api : AbsOkhttpApi() {
    var timestampOffset by AppDataStoreLongCache("api_timestampOffset", 0L)

    /**
     * 当前的baseUrl
     */
    var currentBaseUrl : String = ""

    override fun getBaseUrl(): String {
        return currentBaseUrl
    }

    // 创建并启动WebSocket连接
    fun connectWSServer(ip:String, port:Int, listener:WebSocketListener) {
        val client = OkHttpClient.Builder()
            //todo 我这里做的是服务器ping。不需要client去ping。这里故意搞长一点。应该不需要client去ping。
            .pingInterval(MyDroidWSServer.HEARTBEAT_INTERVAL * 10, TimeUnit.SECONDS)
            .build()
        val request: Request = Request.Builder().url("ws://$ip:$port").build()
        client.newWebSocket(request, listener)
    }
}