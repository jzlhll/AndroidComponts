package com.allan.androidlearning.transfer

import com.au.module_android.Globals
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

class MyDroidHttpServer(port: Int, val magicCode:String) : NanoHTTPD(port) {
    init {
        tempFileManagerFactory = MyDroidTempFileMgrFactory()
    }

    private val chunksMgr: IChunkMgr = MyDroidHttpChunksMgr()

    override fun serve(session: IHTTPSession): Response {
        // 处理跨域预检请求 (OPTIONS)
        if (session.method == Method.OPTIONS) {
            val response = newFixedLengthResponse(Response.Status.OK, "text/plain", "")
            response.addHeader("Access-Control-Allow-Origin", "*")
            response.addHeader("Access-Control-Allow-Methods", "GET, POST")
            response.addHeader("Access-Control-Allow-Headers", "Content-Type")
            return response
        }

        val ct = ContentType(session.headers["content-type"]).tryUTF8()
        session.headers.put("content-type", ct.contentTypeHeader)

        return when (session.method) {
            Method.GET -> handleGetRequest(session.uri)
            Method.POST -> handlePostRequest(session)
            else -> newFixedLengthResponse(Status.NOT_FOUND, MIME_PLAINTEXT, "404")
        }
    }

    private fun handleGetRequest(url: String): Response {
        return when {
            // 主页面请求
            url == "/" -> serveAssetFile("transfer/index.html") {it.replace("MyDroidTransfer%d", "MyDroidTransfer-${magicCode}")}
            // JS 文件请求
            url.endsWith(".js") -> {
                val jsName = url.substring(1)
//                serveAssetFile("transfer/$jsName")
                serverJsFile("transfer/$jsName")
            }
            else -> {
                logdNoFile { "handle get request $url" }
                newFixedLengthResponse(Status.NOT_FOUND, "text/plain", "404 Not Found")
            }
        }
    }

    private fun handlePostRequest(session: IHTTPSession): Response {
        if (session.uri == "/upload-chunk") {
            return chunksMgr.handleUploadChunk(session)
        }
        if (session.uri == "/merge-chunks") {
            return chunksMgr.handleMergeChunk(session)
        }
        return newFixedLengthResponse("Invalid request from AppServer")
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
            return newFixedLengthResponse(Status.NOT_FOUND, "text/plain", "404 Not Found")
        }
    }
}
