package com.allan.androidlearning.transfer

import com.au.module_android.Globals
import com.au.module_android.utils.ALogJ
import com.au.module_android.utils.logd
import com.au.module_android.utils.logdNoFile
import fi.iki.elonen.NanoHTTPD
import fi.iki.elonen.NanoHTTPD.Response.Status
import java.io.File
import java.io.FileNotFoundException

class MyDroidHttpServer(port: Int, val magicCode:String) : NanoHTTPD(port) {
    init {
        tempFileManagerFactory = CustomTempFileManagerFactory()
    }

    override fun serve(session: IHTTPSession): Response {
        val ct = ContentType(session.headers["content-type"]).tryUTF8()
        session.headers.put("content-type", ct.contentTypeHeader)

        return when (session.method) {
            Method.GET -> handleGetRequest(session.uri)
            Method.POST -> handlePostRequest(session)
            else -> newFixedLengthResponse(Status.NOT_FOUND, MIME_PLAINTEXT, "404")
        }
    }

    private fun handleGetRequest(uri: String): Response {
        logdNoFile { "handle get request $uri" }
        return when {
            // 主页面请求
            uri == "/" -> serveAssetFile("transfer/index.html") {it.replace("MyDroidTransfer%d", "MyDroidTransfer-${magicCode}")}
            // JS 文件请求
            uri.contains("sparkmd5.min.js") -> serveAssetFile("transfer/sparkmd5.min.js")

            //"/files" -> newFixedLengthResponse(listFiles()) // 返回文件列表
            else -> newFixedLengthResponse(Status.NOT_FOUND, "text/plain", "404 Not Found")
        }
    }

    private fun handlePostRequest(session: IHTTPSession): Response {
        if (session.uri == "/upload-chunk") {
            return handleUploadChunk(session)
        }
        if (session.uri == "/merge-chunks") {

        }
        return newFixedLengthResponse("Invalid request from AppServer")
    }

    val filesMap = HashMap<String, String>() // 存放临时文件路径
    val fileChunksMap = HashMap<String, List<Pair<Int, String>>>()

    private fun handleUploadChunk(session: IHTTPSession): Response {
        logdNoFile { "handle Upload Chunk" }
        return try {
            filesMap.clear()
            session.parseBody(filesMap) // 关键：解析文件块

            // 1. 获取普通参数
            val params = session.parameters
            val fileName = params["fileName"]?.first() ?: ""
            val chunkIndex = params["chunkIndex"]?.first()?.toInt() ?: 0
            val totalChunks = params["totalChunks"]?.first()?.toInt() ?: 0
            val md5 = params["md5"]?.first() ?: ""


            // 2. 获取文件块内容（核心）
            val chunkTempFile = filesMap["chunk"] // 注意此处 "chunk" 必须与前端的字段名一致
            if (chunkTempFile.isNullOrEmpty()) {
                return newFixedLengthResponse(Status.BAD_REQUEST, "text/plain", "未接收到文件块")
            }

            logdNoFile { "handle fileName $fileName $md5 $chunkIndex/$totalChunks chunk:$chunkTempFile ${chunkTempFile.length}" }
            // 3. 将临时文件转存

//            val tempFile = File(Globals.goodCacheDir, "$fileName.tmp")
//            val outputStream = uploads.getOrPut(fileName) {
//                FileOutputStream(tempFile, true).also {
//                    it.channel.truncate(0) // 清空已有内容（用于重传）
//                }
//            }
//
//            session.inputStream.use { input ->
//                val buffer = ByteArray(8192)
//                var bytesRead: Int
//                while (input.read(buffer).also { bytesRead = it } != -1) {
//                    outputStream.write(buffer, 0, bytesRead)
//                }
//            }
//
//            if (chunkNumber == totalChunks - 1) {
//                outputStream.close()
//                uploads.remove(fileName)
//                tempFile.renameTo(File(Globals.goodFilesDir, fileName))
//            }
            newFixedLengthResponse(Status.OK, "text/plain", "Chunk $chunkIndex received from AppServer.")
        } catch (e: Exception) {
            logd { ALogJ.ex(e) }
            newFixedLengthResponse(Status.INTERNAL_ERROR, "text/plain", e.message)
        }
    }

    private fun serveAssetFile(assetFile: String, replacementBlock:((String)->String) = { it }) : Response {
        return try {
            val text = Globals.app.assets.open(assetFile).bufferedReader().use { it.readText() }
            newFixedLengthResponse(replacementBlock(text))
        } catch (_: FileNotFoundException) {
            newFixedLengthResponse(Status.INTERNAL_ERROR, "application/json", """"{"error": "File $assetFile not found"}""")
        }
    }

    private fun mergeAllChunks(fileName: String, totalChunks: Int, md5: String) {
        val outputFile = File("uploads/$fileName")

        // 按顺序合并分片
        outputFile.outputStream().use { output ->
            for (i in 1..totalChunks) {
                val chunkFile = File("tempDir/${fileName}_part$i")
                chunkFile.inputStream().use { input ->
                    input.copyTo(output)
                }
                chunkFile.delete() // 删除已合并的分片
            }
        }

        // MD5 校验（需自行实现校验逻辑）
//        if (verifyMD5(outputFile, md5)) {
//            println("文件合并完成且校验通过")
//        } else {
//            outputFile.delete()
//            throw IOException("MD5校验失败")
//        }
    }

}
