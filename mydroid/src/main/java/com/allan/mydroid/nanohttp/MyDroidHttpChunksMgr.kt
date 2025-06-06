package com.allan.mydroid.nanohttp

import com.allan.mydroid.benas.ChunkInfoResult
import com.allan.mydroid.benas.PROCESS_CHUNK
import com.allan.mydroid.benas.PROCESS_CHUNK_ERROR
import com.allan.mydroid.benas.PROCESS_COMPLETED
import com.allan.mydroid.benas.PROCESS_MERGE_ERROR
import com.allan.mydroid.benas.PROCESS_MERGING
import com.allan.mydroid.benas.ReceivingFileInfo
import com.allan.mydroid.globals.CODE_FAIL
import com.allan.mydroid.globals.CODE_FAIL_MD5_CHECK
import com.allan.mydroid.globals.CODE_FAIL_MERGE_CHUNK
import com.allan.mydroid.globals.CODE_FAIL_RECEIVER_CHUNK
import com.allan.mydroid.globals.CODE_SUC
import com.allan.mydroid.globals.MyDroidConst
import com.allan.mydroid.globals.badRequestJsonResponse
import com.allan.mydroid.globals.jsonResponse
import com.allan.mydroid.globals.nanoTempCacheChunksDir
import com.allan.mydroid.globals.nanoTempCacheMergedDir
import com.allan.mydroid.globals.okJsonResponse
import com.au.module_android.Globals.resStr
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
    private val parseBodyFileMap = HashMap<String, String>() // 存放临时文件路径

    /**
     * key是fileName-md5
     * value是chunkInfo组
     */
    private val fileChunkInfosMap = HashMap<String, ArrayList<ChunkInfoResult>>()
    /**
     * 用于加锁上面的操作
     */
    private val cLock = Any()
    
    private fun addChunkInfo(chunkInfo: ChunkInfoResult) {
        synchronized(cLock) {
            val fileName = chunkInfo.fileName
            val md5 = chunkInfo.md5
            val chunkInfoList = fileChunkInfosMap.getOrPut("$fileName-$md5") {
                ArrayList()
            }
            chunkInfoList.add(chunkInfo)
        }
    }

    private fun removeChunkInfoList(fileName: String, md5:String) : ArrayList<ChunkInfoResult>? {
        synchronized(cLock) {
            return fileChunkInfosMap.remove("$fileName-$md5")
        }
    }

    override fun handleUploadChunk(session: IHTTPSession) : Response {
        var fileName = ""
        var chunkIndex:Int = -1
        var totalChunks = 0
        var md5 = ""

        try {
            // 1. 获取普通参数
            parseBodyFileMap.clear()
            session.parseBody(parseBodyFileMap)

            val params = session.parameters
            fileName = params["fileName"]?.first() ?: ""
            chunkIndex = params["chunkIndex"]?.first()?.toInt() ?: 0
            totalChunks = params["totalChunks"]?.first()?.toInt() ?: 0
            md5 = params["md5"]?.first() ?: ""

            // 2. 获取文件块内容（核心）
            val tmpFileStr = parseBodyFileMap["chunk"]
            if (tmpFileStr.isNullOrEmpty()) {
                val s = com.allan.mydroid.R.string.file_not_received.resStr()
                return ResultBean<ChunkInfoResult>(CODE_FAIL, s, null).badRequestJsonResponse()
            }

            val tmpFile = File(tmpFileStr)
            logt{ "chunk: $fileName $md5, $chunkIndex/$totalChunks $tmpFileStr ${tmpFile.length()}" }

            // 3. 将临时文件转存 否则框架立刻clear掉了。
            val chunkTmpFileStr = nanoTempCacheChunksDir() + File.separatorChar + tmpFile.name
            val chunkTmpFile = File(chunkTmpFileStr)
            if (chunkTmpFile.exists()) {
                chunkTmpFile.delete()
            }
            tmpFile.renameTo( chunkTmpFile)

            val chunkInfo = ChunkInfoResult(fileName, chunkIndex, totalChunks, md5, chunkTmpFile)
            addChunkInfo(chunkInfo)

            MyDroidConst.receiverProgressData.setValueSafe(
                mapOf(
                    "$fileName-$md5" to ReceivingFileInfo(
                        fileName,
                        md5,
                        chunkIndex,
                        totalChunks,
                        PROCESS_CHUNK
                    )
                )
            )

            return ResultBean(
                CODE_SUC,
                "$fileName Chunk $chunkIndex/$totalChunks received success.", chunkInfo).okJsonResponse()
        } catch (e: Exception) {
            logd { ALogJ.ex(e) }
            MyDroidConst.receiverProgressData.setValueSafe(
                mapOf(
                    "$fileName-$md5" to ReceivingFileInfo(
                        fileName,
                        md5,
                        chunkIndex,
                        totalChunks,
                        PROCESS_CHUNK_ERROR,
                        com.allan.mydroid.R.string.chunk_receiver_error.resStr()
                    )
                )
            )
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
        val fileName = params.optString("fileName")
        val totalChunks = params.optInt("totalChunks")
        var lastModified = params.optLong("lastModified", System.currentTimeMillis())
        if (md5.isNullOrEmpty() || fileName.isNullOrEmpty()) {
            return ResultBean<ChunkInfoResult>(
                CODE_FAIL,
                com.allan.mydroid.R.string.error_merge_chunk_params.resStr(), null).badRequestJsonResponse()
        }
        logt { "handle Merge Chunk $fileName , $md5 , totalChunks:$totalChunks" }

        MyDroidConst.receiverProgressData.setValueSafe(
            mapOf(
                "$fileName-$md5" to ReceivingFileInfo(
                    fileName,
                    md5,
                    totalChunks,
                    totalChunks,
                    PROCESS_MERGING
                )
            )
        )

        val chunkInfoList = removeChunkInfoList(fileName, md5)
        if (chunkInfoList == null) {
            val noChunkStr = com.allan.mydroid.R.string.no_chunks.resStr()

            MyDroidConst.receiverProgressData.setValueSafe(
                mapOf(
                    "$fileName-$md5" to ReceivingFileInfo(
                        fileName,
                        md5,
                        totalChunks,
                        totalChunks,
                        PROCESS_MERGE_ERROR,
                        noChunkStr
                    )
                )
            )

            return ResultBean<ChunkInfoResult>(CODE_FAIL_MERGE_CHUNK, noChunkStr, null).jsonResponse(Status.OK)
        }
        chunkInfoList.sortBy { it.chunkIndex }
        if (chunkInfoList.size != totalChunks) {
            val chunkNumNotMatchStr = com.allan.mydroid.R.string.chunks_number_not_match.resStr()
            MyDroidConst.receiverProgressData.setValueSafe(
                mapOf(
                    "$fileName-$md5" to ReceivingFileInfo(
                        fileName,
                        md5,
                        totalChunks,
                        totalChunks,
                        PROCESS_MERGE_ERROR,
                        chunkNumNotMatchStr
                    )
                )
            )
            return ResultBean<ChunkInfoResult>(CODE_FAIL_MERGE_CHUNK, chunkNumNotMatchStr, null).jsonResponse(Status.OK)
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
            outputFile.setLastModified(lastModified)
            Thread.sleep(10)
            MyDroidConst.onFileMergedData.setValueSafe(outputFile)

            MyDroidConst.receiverProgressData.setValueSafe(
                mapOf(
                    "$fileName-$md5" to ReceivingFileInfo(
                        fileName,
                        md5,
                        totalChunks,
                        totalChunks,
                        PROCESS_COMPLETED
                    )
                )
            )
            return ResultBean<ChunkInfoResult>(
                CODE_SUC,
                com.allan.mydroid.R.string.file_merge_success.resStr()
                , null).okJsonResponse()
        } else {
            outputFile.delete()
            val md5FailStr = com.allan.mydroid.R.string.md5_check_failed.resStr()
            MyDroidConst.receiverProgressData.setValueSafe(
                mapOf(
                    "$fileName-$md5" to ReceivingFileInfo(
                        fileName,
                        md5,
                        totalChunks,
                        totalChunks,
                        PROCESS_MERGE_ERROR,
                        md5FailStr
                    )
                )
            )

            return ResultBean<ChunkInfoResult>(
                CODE_FAIL_MD5_CHECK,
                md5FailStr, null).okJsonResponse()
        }
    }

    override fun handleAbortChunk(session: IHTTPSession): Response {
        logt { "clear up when abort chunk." }
        val body = parseRequestBody(session)
        val params = JSONObject(body)
        // 3. 提取关键参数
        val fileName = params.optString("fileName")
        val md5 = params.optString("md5")

        removeChunkInfoList(fileName, md5)?.forEach { chunkInfo->
            Thread.sleep(20)
            chunkInfo.chunkTmpFile.delete() // 删除已合并的分片
        }
        return ResultBean<String>(
            CODE_SUC,
            com.allan.mydroid.R.string.clear_up.resStr(), null).jsonResponse(Status.OK)
    }

    // 辅助方法：将请求体转为字符串
    private fun parseRequestBody(session: IHTTPSession): String {
        val contentLength = session.headers["content-length"]?.toInt() ?: 0
        val buffer = ByteArray(contentLength)
        session.inputStream.read(buffer)
        return String(buffer, Charsets.UTF_8)
    }
}