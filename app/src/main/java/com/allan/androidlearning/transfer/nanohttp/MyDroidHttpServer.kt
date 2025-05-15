package com.allan.androidlearning.transfer.nanohttp

import android.os.Handler
import android.os.HandlerThread
import com.allan.androidlearning.transfer.CODE_SUC
import com.allan.androidlearning.transfer.MyDroidGlobalService
import com.allan.androidlearning.transfer.benas.MyDroidMode
import com.allan.androidlearning.transfer.htmlbeans.IpPortResult
import com.allan.androidlearning.transfer.okJsonResponse
import com.au.module_android.Globals
import com.au.module_android.api.ResultBean
import com.au.module_android.utils.logdNoFile
import fi.iki.elonen.NanoHTTPD
import fi.iki.elonen.NanoHTTPD.Response
import fi.iki.elonen.NanoHTTPD.Response.Status
import java.io.FileNotFoundException
import java.io.IOException
import java.io.InputStream

interface IChunkMgr {
    fun handleUploadChunk(session: NanoHTTPD.IHTTPSession): Response
    fun handleMergeChunk(session: NanoHTTPD.IHTTPSession) : Response
}

interface IMyDroidHttpServer {
    /**
     * 启动一些周期性活动。
     */
    fun startPeriodWork()
}

class MyDroidHttpServer(httpPort: Int) : NanoHTTPD(httpPort), IMyDroidHttpServer {
    private val handleThread: HandlerThread
    private val handle: Handler

    init {
        tempFileManagerFactory = MyDroidTempFileMgrFactory()
        handleThread = HandlerThread("MyDroidHttpServer")
        handleThread.start()
        handle = Handler(handleThread.looper)
    }

    private val chunksMgr: IChunkMgr = MyDroidHttpChunksMgr()
    private val sendFileMgr by lazy { MyDroidSendFileMgr() }

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
        return when {
            // 主页面请求
            url == "/" -> {
                //todo 增加middle页面
                if (MyDroidGlobalService.myDroidModeData.realValue == MyDroidMode.Send) {
                    serveAssetFile("transfer/send.html")
                } else {
                    serveAssetFile("transfer/receiver.html")
                }
            }
            // JS 文件请求
            url.endsWith(".js") -> {
                val jsName = url.substring(1)
//                serveAssetFile("transfer/$jsName")
                serverJsFile("transfer/$jsName")
            }
            url == "/download" -> {
                sendFileMgr.handleDownFileRequest(session)
            }
            else -> {
                logdNoFile { "handle get request $url" }
                newFixedLengthResponse(Status.NOT_FOUND, MIME_PLAINTEXT, "404 Not Found")
            }
        }
    }


    private fun handlePostRequest(session: IHTTPSession): Response {
        return when (session.uri) {
            "/upload-chunk" -> chunksMgr.handleUploadChunk(session)
            "/merge-chunks" -> chunksMgr.handleMergeChunk(session)
            "/read-websocket-ip-port" -> getWebsocketIpPort()
            else -> newFixedLengthResponse("Invalid request from AppServer") // 或者其他默认响应
        }
    }

    private fun getWebsocketIpPort() : Response{
        val data = MyDroidGlobalService.ipPortData
        val ip = data.value?.ip
        val wsPort = data.value?.webSocketPort

        return if (ip != null && wsPort != null) {
            val info = IpPortResult(ip, wsPort)
            logdNoFile { "get websocket ipPort $info" }
            ResultBean(CODE_SUC, "Success!", info).okJsonResponse()
        } else {
            newFixedLengthResponse("Invalid request from AppServer") // 或者其他默认响应
        }
    }

    private fun serveAssetFile(assetFile: String, replacementBlock:((String)->String) = { it }) : Response {
        return try {
            val text = Globals.app.assets.open(assetFile).bufferedReader().use { it.readText() }
            val response = newFixedLengthResponse(replacementBlock(text))
            logdNoFile { "serve Asset File read success $assetFile." }
            return response
        } catch (_: FileNotFoundException) {
            newFixedLengthResponse(Status.INTERNAL_ERROR, "application/json", """"{"error": "File $assetFile not found"}""")
        }
    }

    private fun serverJsFile(jsAssetFile:String) : Response{
        try {
            val `is`: InputStream = Globals.app.assets.open(jsAssetFile)
            val response = newChunkedResponse(Status.OK, "application/javascript", `is`)
            logdNoFile { "server Js File read success $jsAssetFile." }
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
        handle.removeCallbacksAndMessages(null)
        handleThread.quit()
        super.stop()
    }
}