package com.allan.androidlearning.transfer.nanohttp

import com.allan.androidlearning.transfer.CODE_FAIL
import com.allan.androidlearning.transfer.CODE_FAIL_MD5_CHECK
import com.allan.androidlearning.transfer.CODE_FAIL_MERGE_CHUNK
import com.allan.androidlearning.transfer.CODE_FAIL_RECEIVER_CHUNK
import com.allan.androidlearning.transfer.CODE_SUC
import com.allan.androidlearning.transfer.MyDroidGlobalService
import com.allan.androidlearning.transfer.badRequestJsonResponse
import com.allan.androidlearning.transfer.benas.ChunkInfoResult
import com.allan.androidlearning.transfer.jsonResponse
import com.allan.androidlearning.transfer.nanoTempCacheChunksDir
import com.allan.androidlearning.transfer.nanoTempCacheMergedDir
import com.allan.androidlearning.transfer.okJsonResponse
import com.au.module_android.api.ResultBean
import com.au.module_android.utils.ALogJ
import com.au.module_android.utils.getFileMD5
import com.au.module_android.utils.logd
import com.au.module_android.utils.logt
import fi.iki.elonen.NanoHTTPD.IHTTPSession
import fi.iki.elonen.NanoHTTPD.Response
import fi.iki.elonen.NanoHTTPD.Response.Status
import org.json.JSONObject
import java.io.File

class MyDroidHttpChunksMgr() : IChunkMgr{
    val filesMap = HashMap<String, String>() // 存放临时文件路径

    /**
     * key是fileName；value是chunkInfo组
     */
    val fileChunkInfosMap = HashMap<String, ArrayList<ChunkInfoResult>>()
    val fileChunkLock = Any()
    
    private fun transferInfoCallback(info:String) {
        MyDroidGlobalService.onTransferInfoData.setValueSafe(info)
    }

    private fun addChunkInfo(chunkInfo: ChunkInfoResult) {
        synchronized(fileChunkLock) {
            val chunkInfoList = fileChunkInfosMap.getOrPut(chunkInfo.fileName) {
                ArrayList()
            }
            chunkInfoList.add(chunkInfo)
        }
    }

    private fun removeChunkInfoList(fileName: String) : ArrayList<ChunkInfoResult>? {
        synchronized(fileChunkLock) {
            return fileChunkInfosMap.remove(fileName)
        }
    }

    override fun handleUploadChunk(session: IHTTPSession) : Response {
        var fileName:String? = null
        var chunkIndex:Int = -1
        var totalChunks = 0
        try {
            filesMap.clear()
            session.parseBody(filesMap)

            // 1. 获取普通参数
            val params = session.parameters
            fileName = params["fileName"]?.first() ?: ""
            chunkIndex = params["chunkIndex"]?.first()?.toInt() ?: 0
            totalChunks = params["totalChunks"]?.first()?.toInt() ?: 0
            val md5 = params["md5"]?.first() ?: ""
            val shortMd5 = if(md5.length > 8) md5.substring(0, 8) else md5

            // 2. 获取文件块内容（核心）
            val tmpFileStr = filesMap["chunk"]
            if (tmpFileStr.isNullOrEmpty()) {
                return ResultBean<ChunkInfoResult>(CODE_FAIL, "未接收到文件块", null).badRequestJsonResponse()
            }

            val tmpFile = File(tmpFileStr)
            //logdNoFile { "chunk: $fileName $md5 $chunkIndex/$totalChunks chunk:$tmpFileStr ${tmpFile.length()}" }

            // 3. 将临时文件转存 否则框架立刻clear掉了。
            val chunkTmpFileStr = nanoTempCacheChunksDir() + File.separatorChar + tmpFile.name
            val chunkTmpFile = File(chunkTmpFileStr)
            if (chunkTmpFile.exists()) {
                chunkTmpFile.delete()
            }
            tmpFile.renameTo( chunkTmpFile)

            val chunkInfo = ChunkInfoResult(fileName, chunkIndex, totalChunks, md5, chunkTmpFile)
            addChunkInfo(chunkInfo)

            transferInfoCallback("$fileName: $shortMd5 $chunkIndex/$totalChunks")

            return ResultBean(
                CODE_SUC,
                "$fileName Chunk $chunkIndex/$totalChunks received success.", chunkInfo).okJsonResponse()
        } catch (e: Exception) {
            logd { ALogJ.ex(e) }
            transferInfoCallback("$fileName Chunk $chunkIndex/$totalChunks received failed!")
            return ResultBean<ChunkInfoResult>(
                CODE_FAIL_RECEIVER_CHUNK,
                "$fileName Chunk $chunkIndex/$totalChunks received failed!",
                null).okJsonResponse()
        }
    }

    override fun handleMergeChunk(session: IHTTPSession): Response {
        val body = parseRequestBody(session)
        val params = JSONObject(body)
        // 3. 提取关键参数
        val md5 = params.optString("md5")
        val shortMd5 = if(md5.length > 8) md5.substring(0, 8) else md5
        val fileName = params.optString("fileName")
        val totalChunks = params.optInt("totalChunks")
        if (md5.isNullOrEmpty() || fileName.isNullOrEmpty()) {
            return ResultBean<ChunkInfoResult>(CODE_FAIL, "Error merge chunk params.", null).badRequestJsonResponse()
        }
        logt { "handle Merge Chunk $fileName , $md5 , totalChunks:$totalChunks" }

        transferInfoCallback("$fileName: $shortMd5 $totalChunks 合并中...")
        val chunkInfoList = removeChunkInfoList(fileName)
        if (chunkInfoList == null) {
            transferInfoCallback("$fileName: $shortMd5 No chunks?")
            return ResultBean<ChunkInfoResult>(CODE_FAIL_MERGE_CHUNK, "No chunks?", null).jsonResponse(Status.OK)
        }
        chunkInfoList.sortBy { it.chunkIndex }
        if (chunkInfoList.size != totalChunks) {
            transferInfoCallback("$fileName: $shortMd5 Chunks number not match.")
            return ResultBean<ChunkInfoResult>(CODE_FAIL_MERGE_CHUNK, "Chunks number not match.", null).jsonResponse(Status.OK)
        }
        val outputFileStr = nanoTempCacheMergedDir() + File.separatorChar + fileName
        val outputFile = File(outputFileStr)
        if (outputFile.exists()) {
            outputFile.delete()
        }

        // 按顺序合并分片
        outputFile.outputStream().use { output ->
            chunkInfoList.forEach { chunkInfo->
                chunkInfo.chunkTmpFile.inputStream().use { input ->
                    input.copyTo(output)
                }
                Thread.sleep(20)
                chunkInfo.chunkTmpFile.delete() // 删除已合并的分片
            }
        }

        Thread.sleep(100)
        // MD5 校验（需自行实现校验逻辑）
        val fileMd5 = getFileMD5(outputFile.absolutePath)
        if (fileMd5 == md5) {
            MyDroidGlobalService.onFileMergedData.setValueSafe(outputFile)

            transferInfoCallback("$fileName: $shortMd5 传输成功，md5检验通过！")
            return ResultBean<ChunkInfoResult>(
                CODE_SUC,
                "文件合并完成且校验通过", null).okJsonResponse()
        } else {
            outputFile.delete()
            transferInfoCallback("$fileName: $shortMd5 md5 check failed.")
            return ResultBean<ChunkInfoResult>(
                CODE_FAIL_MD5_CHECK,
                "MD5校验失败", null).okJsonResponse()
        }
    }

    // 辅助方法：将请求体转为字符串
    private fun parseRequestBody(session: IHTTPSession): String {
        val contentLength = session.headers["content-length"]?.toInt() ?: 0
        val buffer = ByteArray(contentLength)
        session.inputStream.read(buffer)
        return String(buffer, Charsets.UTF_8)
    }
}