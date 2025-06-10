package com.allan.mydroid.api

import com.allan.mydroid.nanohttp.WebsocketServer
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
            .pingInterval(WebsocketServer.HEARTBEAT_INTERVAL, TimeUnit.MILLISECONDS)
            .build()
        val request: Request = Request.Builder().url("ws://$ip:$port").build()
        client.newWebSocket(request, listener)
    }
}