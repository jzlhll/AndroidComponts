package com.allan.mydroid.nanohttp

import com.allan.mydroid.R
import com.allan.mydroid.api.ABORT_UPLOAD_CHUNKS
import com.allan.mydroid.api.MERGE_CHUNKS
import com.allan.mydroid.api.MyDroidMode
import com.allan.mydroid.api.READ_WEBSOCKET_IP_PORT
import com.allan.mydroid.api.TEXT_CHAT_READ_WEBSOCKET_IP_PORT
import com.allan.mydroid.api.UPLOAD_CHUNK
import com.allan.mydroid.beans.httpdata.IpPortResult
import com.allan.mydroid.globals.CODE_SUC
import com.allan.mydroid.globals.MyDroidConst
import com.allan.mydroid.globals.MyDroidGlobalService
import com.allan.mydroid.globals.okJsonResponse
import com.au.module_android.Globals
import com.au.module_android.Globals.resStr
import com.au.module_android.api.ResultBean
import com.au.module_android.utils.logdNoFile
import com.modulenative.AppNative
import fi.iki.elonen.NanoHTTPD
import fi.iki.elonen.NanoHTTPD.Response
import fi.iki.elonen.NanoHTTPD.Response.Status
import java.io.FileNotFoundException
import java.io.IOException

interface IChunkMgr {
    fun handleUploadChunk(session: NanoHTTPD.IHTTPSession): Response
    fun handleMergeChunk(session: NanoHTTPD.IHTTPSession) : Response
    fun handleAbortChunk(session: NanoHTTPD.IHTTPSession) : Response
}

interface IMyDroidHttpServer {
    /**
     * 启动一些周期性活动。
     */
    fun startPeriodWork()
}

class MyDroidHttpServer(httpPort: Int) : NanoHTTPD(httpPort), IMyDroidHttpServer {
    init {
        tempFileManagerFactory = MyDroidTempFileMgrFactory()
    }

    private val chunksMgr: IChunkMgr = MyDroidHttpChunksMgr()

    override fun serve(session: IHTTPSession): Response {
        // 处理跨域预检请求 (OPTIONS)
        if (session.method == Method.OPTIONS) {
            return handleOptionRequest()
        }

        val ct = ContentType(session.headers["content-type"]).tryUTF8()
        session.headers.put("content-type", ct.contentTypeHeader)

        return when (session.method) {
            Method.GET -> handleGetRequest(session)
            Method.POST -> handlePostRequest(session)
            else -> newFixedLengthResponse(Status.NOT_FOUND, MIME_PLAINTEXT, "404")
        }
    }

    /*
     response.addHeader("Access-Control-Allow-Headers", "Content-Type, Accept, token, Authorization, " +
         "X-Auth-Token,X-XSRF-TOKEN,Access-Control-Allow-Headers");
 response.addHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, HEAD");
 response.addHeader("Access-Control-Allow-Credentials", "true");
 response.addHeader("Access-Control-Allow-Origin", "*");
 response.addHeader("Access-Control-Max-Age", "" + 42 * 60 * 60);
     */

    private fun handleOptionRequest(): Response {
        val response = newFixedLengthResponse(Status.OK, MIME_PLAINTEXT, "")
        response.addHeader("Access-Control-Allow-Origin", "*")
        response.addHeader("Access-Control-Allow-Methods", "GET, POST")
        response.addHeader("Access-Control-Allow-Headers", "Content-Type")
        return response
    }

    private fun handleGetRequest(session: IHTTPSession): Response {
        val url = session.uri ?: ""
        logdNoFile { "handle Get Request $url" }
        MyDroidGlobalService.updateAliveTs("http get request")
        val error:String
        when {
            // 主页面请求
            url == "/" -> {
                when (MyDroidConst.currentDroidMode) {
                    MyDroidMode.Send -> {
                        return serveAssetFile("transfer/send.html")
                    }
                    MyDroidMode.Receiver -> {
                        return serveAssetFile("transfer/receiver.html")
                    }
                    else -> {
                        error = Globals.getString(R.string.server_not_support) + "(E02)"
                    }
                }
            }
            url == READ_WEBSOCKET_IP_PORT ->
                return getWebsocketIpPort()
            url == TEXT_CHAT_READ_WEBSOCKET_IP_PORT -> {
                return getWebsocketIpPortWrap()
            }
            // JS 文件请求
            url.endsWith(".js") -> {
                val jsName = url.substring(1)
//                serveAssetFile("transfer/$jsName")
                return serverJsFile("transfer/$jsName")
            }
            else -> {
                error = Globals.getString(R.string.server_not_support) + "(E01)"
            }
        }

        return newFixedLengthResponse(Status.NOT_FOUND, MIME_PLAINTEXT, error)
    }

    private fun handlePostRequest(session: IHTTPSession): Response {
        MyDroidGlobalService.updateAliveTs("http post request")
        return when (session.uri) {
            UPLOAD_CHUNK -> chunksMgr.handleUploadChunk(session)
            MERGE_CHUNKS -> chunksMgr.handleMergeChunk(session)
            ABORT_UPLOAD_CHUNKS -> chunksMgr.handleAbortChunk(session)
            READ_WEBSOCKET_IP_PORT -> getWebsocketIpPort()
            TEXT_CHAT_READ_WEBSOCKET_IP_PORT -> getWebsocketIpPortWrap()
            else -> newFixedLengthResponse(R.string.invalid_request_from_appserver.resStr()) // 或者其他默认响应
        }
    }

    private fun getWebsocketIpPortWrap(): Response {
        if (MyDroidConst.currentDroidMode == MyDroidMode.TextChat) {
            return getWebsocketIpPort()
        } else {
            val error = Globals.getString(R.string.server_is_not_textchat)
            return newFixedLengthResponse(Status.NOT_FOUND, MIME_PLAINTEXT, error)
        }
    }

    private fun getWebsocketIpPort() : Response{
        val data = MyDroidConst.ipPortData
        val ip = data.value?.ip
        val wsPort = data.value?.webSocketPort

        return if (ip != null && wsPort != null) {
            val info = IpPortResult(ip, wsPort)
            logdNoFile { "get websocket ipPort $info" }
            ResultBean(CODE_SUC, "Success!", info).okJsonResponse()
        } else {
            newFixedLengthResponse(R.string.invalid_request_from_appserver.resStr()) // 或者其他默认响应
        }
    }

    private fun serveAssetFile(assetFile: String, replacementBlock:((String)->String) = { it }) : Response {
        return try {
            val text = AppNative.asts(Globals.app, assetFile)
            val response = newFixedLengthResponse(replacementBlock(text))
            logdNoFile { "serve Asset File read success $assetFile." }
            return response
        } catch (_: FileNotFoundException) {
            newFixedLengthResponse(Status.INTERNAL_ERROR, "application/json", """"{"error": "File $assetFile not found"}""")
        }
    }

    private fun serverJsFile(jsAssetFile:String) : Response{
        try {
            val text = AppNative.asts(Globals.app, jsAssetFile)
            val response = newFixedLengthResponse(text)
            logdNoFile { "serve Asset File read success $jsAssetFile." }
            return response
        } catch (_: IOException) {
            return newFixedLengthResponse(Status.NOT_FOUND, MIME_PLAINTEXT, "404 Not Found")
        }
    }

    override fun startPeriodWork() {
//        handle.removeCallbacks(mPeriodSpaceRun)
//        handle.post(mPeriodSpaceRun)
    }

    override fun stop() {
        logdNoFile { "stop all." }
        super.stop()
    }
}