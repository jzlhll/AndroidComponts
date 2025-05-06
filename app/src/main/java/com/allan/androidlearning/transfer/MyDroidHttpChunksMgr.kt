package com.allan.androidlearning.transfer

import com.au.module_android.Globals
import com.au.module_android.utils.ALogJ
import com.au.module_android.utils.logd
import com.au.module_android.utils.logdNoFile
import fi.iki.elonen.NanoHTTPD.IHTTPSession
import fi.iki.elonen.NanoHTTPD.Response
import fi.iki.elonen.NanoHTTPD.Response.Status
import fi.iki.elonen.NanoHTTPD.newFixedLengthResponse
import org.json.JSONObject
import java.io.File
import kotlin.collections.first

class MyDroidHttpChunksMgr : IChunkMgr{
    val filesMap = HashMap<String, String>() // 存放临时文件路径

    /**
     * key是fileName；value是chunkInfo组
     */
    val fileChunkInfosMap = HashMap<String, ArrayList<ChunkInfo>>()
    val fileChunkLock = Any()

    private fun addChunkInfo(chunkInfo: ChunkInfo) {
        synchronized(fileChunkLock) {
            val chunkInfoList = fileChunkInfosMap.getOrPut(chunkInfo.fileName) {
                ArrayList()
            }
            chunkInfoList.add(chunkInfo)
        }
    }

    private fun removeChunkInfoList(fileName: String) : ArrayList<ChunkInfo>? {
        synchronized(fileChunkLock) {
            return fileChunkInfosMap.remove(fileName)
        }
    }

    override fun handleUploadChunk(session: IHTTPSession) : Response {
        val tag = "handle Upload Chunk"
        logdNoFile { "$tag start---" }
        return try {
            filesMap.clear()
            session.parseBody(filesMap)

            // 1. 获取普通参数
            val params = session.parameters
            val fileName = params["fileName"]?.first() ?: ""
            val chunkIndex = params["chunkIndex"]?.first()?.toInt() ?: 0
            val totalChunks = params["totalChunks"]?.first()?.toInt() ?: 0
            val md5 = params["md5"]?.first() ?: ""

            // 2. 获取文件块内容（核心）
            val tmpFileStr = filesMap["chunk"]
            if (tmpFileStr.isNullOrEmpty()) {
                return newFixedLengthResponse(Status.BAD_REQUEST, "text/plain", "未接收到文件块")
            }

            val tmpFile = File(tmpFileStr)
            logdNoFile { "$tag chunk: $fileName $md5 $chunkIndex/$totalChunks chunk:$tmpFileStr ${tmpFile.length()}" }

            // 3. 将临时文件转存 否则框架立刻clear掉了。
            val chunkTmpFileStr = Globals.goodCacheDir.absolutePath + File.separatorChar + TEMP_CACHE_CHUNKS_DIR + File.separatorChar + tmpFile.name
            val chunkTmpFile = File(chunkTmpFileStr)
            if (chunkTmpFile.exists()) {
                chunkTmpFile.delete()
            }
            tmpFile.renameTo( chunkTmpFile)

            addChunkInfo(ChunkInfo(fileName, chunkIndex, totalChunks, md5, chunkTmpFile))
            newFixedLengthResponse(Status.OK, "text/plain", "Chunk $chunkIndex received from AppServer.")
        } catch (e: Exception) {
            logd { ALogJ.ex(e) }
            newFixedLengthResponse(Status.INTERNAL_ERROR, "text/plain", e.message)
        }
    }

    override fun handleMergeChunk(session: IHTTPSession): Response {
        val body = parseRequestBody(session)
        val params = JSONObject(body)
        // 3. 提取关键参数
        val fileHash = params.getString("fileHash")
        val fileName = params.getString("fileName")
        val totalChunks = params.getInt("totalChunks")
        if (fileHash.isNullOrEmpty() || fileName.isNullOrEmpty()) {
            return newFixedLengthResponse(Status.BAD_REQUEST, "text/plain", "Error merge chunk params.")
        }
        logdNoFile { "handle Merge Chunk $fileName $fileHash" }
        val chunkInfoList = removeChunkInfoList(fileName)
        if (chunkInfoList == null) {
            return newFixedLengthResponse(Status.INTERNAL_ERROR, "text/plain", "No chunks?")
        }
        chunkInfoList.sortBy { it.chunkIndex }
        if (chunkInfoList.size != totalChunks) {
            return newFixedLengthResponse(Status.INTERNAL_ERROR, "text/plain", "Chunks number not match.")
        }

        val fileStr = Globals.goodCacheDir.absolutePath + File.separatorChar + TEMP_CACHE_CHUNKS_DIR + File.separatorChar + fileName
        val file = File(fileStr)
        if (file.exists()) {
            file.delete()
        }

        // 按顺序合并分片
        file.outputStream().use { output ->
            chunkInfoList.forEach { chunkInfo->
                chunkInfo.chunkTmpFile.inputStream().use { input ->
                    input.copyTo(output)
                }
                Thread.sleep(10)
                chunkInfo.chunkTmpFile.delete() // 删除已合并的分片
            }
        }

        Thread.sleep(100)

//        // MD5 校验（需自行实现校验逻辑）
//        if (verifyMD5(outputFile, md5)) {
//            println("文件合并完成且校验通过")
//        } else {
//            outputFile.delete()
//            throw IOException("MD5校验失败")
//        }
        return newFixedLengthResponse(Status.INTERNAL_ERROR, "text/plain", "lalalalalalal")
    }

    // 辅助方法：将请求体转为字符串
    private fun parseRequestBody(session: IHTTPSession): String {
        val contentLength = session.headers["content-length"]?.toInt() ?: 0
        val buffer = ByteArray(contentLength)
        session.inputStream.read(buffer)
        return String(buffer, Charsets.UTF_8)
    }
}