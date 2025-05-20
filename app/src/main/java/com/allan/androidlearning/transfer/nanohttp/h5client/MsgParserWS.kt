package com.allan.androidlearning.transfer.nanohttp.h5client

import android.net.Uri
import androidx.lifecycle.Observer
import com.allan.androidlearning.transfer.CODE_SUC
import com.allan.androidlearning.transfer.MyDroidConst
import com.allan.androidlearning.transfer.SMALL_FILE_DEFINE_SIZE
import com.allan.androidlearning.transfer.benas.UriRealInfoEx
import com.allan.androidlearning.transfer.benas.UriRealInfoHtml
import com.allan.androidlearning.transfer.getWSSendFileChunkSize
import com.allan.androidlearning.transfer.htmlbeans.API_REQUEST_FILE
import com.allan.androidlearning.transfer.htmlbeans.API_SEND_FILE_CHUNK
import com.allan.androidlearning.transfer.htmlbeans.API_SEND_FILE_LIST
import com.allan.androidlearning.transfer.htmlbeans.API_SEND_FILE_START_NOT_EXIST
import com.allan.androidlearning.transfer.htmlbeans.API_SEND_SMALL_FILE_CHUNK
import com.allan.androidlearning.transfer.htmlbeans.FileListForHtmlResult
import com.allan.androidlearning.transfer.htmlbeans.NotExistResult
import com.allan.androidlearning.transfer.htmlbeans.WSChunkActionResult
import com.allan.androidlearning.transfer.htmlbeans.WSResultBean
import com.allan.androidlearning.transfer.nanohttp.AbsMsgParser
import com.au.module_android.Globals
import com.au.module_android.json.toJsonString
import com.au.module_android.utils.launchOnIOThread
import com.au.module_android.utils.launchOnThread
import com.au.module_android.utils.logdNoFile
import com.au.module_android.utils.logt
import com.au.module_android.utilsmedia.MediaUriResolver
import com.au.module_android.utilsmedia.getRealPath
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.FileInputStream
import java.io.InputStream

class MsgParserWS(client: ClientWebSocket) : AbsMsgParser(client) {
    private val receiverUriMapOb = object : Observer<HashMap<String, UriRealInfoEx>> {
        override fun onChanged(map: HashMap<String, UriRealInfoEx>) {
            val cvtList = mutableListOf<UriRealInfoHtml>()
            map.values.forEach { urlRealInfoEx->
                cvtList.add(urlRealInfoEx.copyToHtml())
            }
            client.server.heartbeatScope.launchOnThread {
                val ret = WSResultBean(CODE_SUC, "send files to html!", API_SEND_FILE_LIST, FileListForHtmlResult(cvtList))
                val json = ret.toJsonString()
                logt { "${Thread.currentThread()} on map changed. send file list to html" }
                logt { "send:$json" }
                client.send(json)
            }
        }
    }

    override fun onOpen() {
        Globals.mainScope.launch {
            MyDroidConst.shareReceiverUriMap.observeForever(receiverUriMapOb)
        }
    }

    override fun onClose() {
        Globals.mainScope.launch {
            MyDroidConst.shareReceiverUriMap.removeObserver(receiverUriMapOb)
        }
    }

    override fun onMessage(json: JSONObject) {
        if (json.has(API_REQUEST_FILE)) {
            val uriUuid = json.optString(API_REQUEST_FILE)
            val info = MyDroidConst.shareReceiverUriMap.value?.get(uriUuid)
            onSendFile(uriUuid, info)
        }
    }

    private fun readFromRealPath(uri: Uri) : InputStream? {
        try {
            val readPathPair = uri.getRealPath(Globals.app)
            val realPath =readPathPair?.first
            if (realPath != null) {
                return FileInputStream(realPath)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return null
    }

    private fun readFromContentResolver(uri: Uri) : InputStream?{
        try {
            //直接读取会跑出Security异常。提示app没有对于media的访问权限
            return Globals.app.contentResolver.openInputStream(uri)
        } catch (e: Exception) {
            // 可根据实际需要记录日志或上报错误
            e.printStackTrace()
        }
        return null
    }

    fun onSendFile(uriUuid:String, info: UriRealInfoEx?) {
        logdNoFile { "${client.clientName} start SendFileToClient Async client : $info" }

        client.scope?.launchOnIOThread {
            var inputStream: InputStream? = null

            if (info != null) {
                val uri = info.uri

                logt { "read from realPath111" }
                inputStream = readFromRealPath(uri)
                logt { "read from realPath222 $inputStream" }
                if (inputStream == null) {
                    logt { "readFrom ContentResolver 111" }
                    inputStream = readFromContentResolver(uri)
                    logt { "readFrom ContentResolver 222 $inputStream" }
                }
                if (inputStream == null) {
                    logt { "resolve MediaStoreUri 111" }
                    val resolvedUri: Uri? = MediaUriResolver().resolveMediaStoreUri(Globals.app, uri)
                    logt { "resolve MediaStoreUri 222 $resolvedUri" }
                    if (resolvedUri != null) {
                        inputStream = readFromContentResolver(uri)
                        logt { "resolve MediaStoreUri 333 $inputStream" }
                    }
                }
            }

            if (inputStream != null && info != null) {
                sendFile(info.uriUuid, info.fileSize, info.goodName(), inputStream)
            } else {
                client.send(WSResultBean(CODE_SUC, "文件不存在", API_SEND_FILE_START_NOT_EXIST, NotExistResult(uriUuid)).toJsonString())
            }
        }
    }

    private suspend fun sendFile(uriUuid:String, fileSize:Long?, fileName:String?, inputStream: InputStream) {
        val chunkSize = getWSSendFileChunkSize(fileSize).toInt()
        val buffer = ByteArray(chunkSize)

        val mgr = BufferManager(chunkSize)

        // 分片发送文件内容
        var bytesRead: Int
        var index = 0
        var offset = 0L

        val api = if(fileSize == null || fileSize >= SMALL_FILE_DEFINE_SIZE) API_SEND_FILE_CHUNK else API_SEND_SMALL_FILE_CHUNK
        val totalChunks = if(fileSize != null) {
            val isNotFull = fileSize % chunkSize > 0
            (fileSize / chunkSize).toInt() + (if(isNotFull) 1 else 0)
        } else 0

        val startJson = WSResultBean(CODE_SUC, "send file start.", api, WSChunkActionResult("start", uriUuid, fileSize ?: 0, totalChunks, "")).toJsonString()
        logt { "send file start $startJson" }
        client.send(startJson)
        delay(100)
        while (inputStream.read(buffer).also { bytesRead = it } != -1) {
            val chunk = if (bytesRead == buffer.size) buffer else buffer.copyOf(bytesRead)
            val arr = mgr.buildChunkPacket(uriUuid, index++, totalChunks, offset, bytesRead, chunk)
            offset += bytesRead
            client.send(arr)
        }
        delay(1000)
        val endJson = WSResultBean(CODE_SUC, "send file end.", api, WSChunkActionResult("end", uriUuid, offset, index, fileName?:"")).toJsonString()
        client.send(endJson)
        logt { "send file end $endJson" }
    }
}