package com.allan.androidlearning.transfer.nanohttp.h5client

import android.net.Uri
import androidx.lifecycle.Observer
import com.allan.androidlearning.transfer.CODE_SUC
import com.allan.androidlearning.transfer.MyDroidConst
import com.allan.androidlearning.transfer.SMALL_FILE_DEFINE_SIZE
import com.allan.androidlearning.transfer.benas.UriRealInfoEx
import com.allan.androidlearning.transfer.benas.UriRealInfoHtml
import com.allan.androidlearning.transfer.getWSSendFileChunkSize
import com.allan.androidlearning.transfer.htmlbeans.API_WS_FILE_DOWNLOAD_COMPLETE
import com.allan.androidlearning.transfer.htmlbeans.API_WS_REQUEST_FILE
import com.allan.androidlearning.transfer.htmlbeans.API_WS_SEND_FILE_CHUNK
import com.allan.androidlearning.transfer.htmlbeans.API_WS_SEND_FILE_LIST
import com.allan.androidlearning.transfer.htmlbeans.API_WS_SEND_FILE_NOT_EXIST
import com.allan.androidlearning.transfer.htmlbeans.API_WS_SEND_SMALL_FILE_CHUNK
import com.allan.androidlearning.transfer.htmlbeans.FileListForHtmlResult
import com.allan.androidlearning.transfer.htmlbeans.NotExistResult
import com.allan.androidlearning.transfer.htmlbeans.WSChunkActionResult
import com.allan.androidlearning.transfer.htmlbeans.WSResultBean
import com.allan.androidlearning.transfer.nanohttp.AbsMsgParser
import com.au.module_android.Globals
import com.au.module_android.Globals.resStr
import com.au.module_android.json.toJsonString
import com.au.module_android.utils.launchOnIOThread
import com.au.module_android.utils.launchOnThread
import com.au.module_android.utils.logdNoFile
import com.au.module_android.utils.logt
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.FileInputStream
import java.io.InputStream

class MsgParserWS(client: ClientWebSocket) : AbsMsgParser(client) {
    private val sendUriMapOb = object : Observer<HashMap<String, UriRealInfoEx>> {
        override fun onChanged(map: HashMap<String, UriRealInfoEx>) {
            val cvtList = mutableListOf<UriRealInfoHtml>()
            map.values.forEach { urlRealInfoEx->
                if (urlRealInfoEx.isChecked) {
                    cvtList.add(urlRealInfoEx.copyToHtml())
                }
            }
            client.server.heartbeatScope.launchOnThread {
                val ret = WSResultBean(CODE_SUC, com.allan.androidlearning.R.string.send_files_to_html.resStr(), API_WS_SEND_FILE_LIST, FileListForHtmlResult(cvtList))
                val json = ret.toJsonString()
                logt { "${Thread.currentThread()} on map changed. send file list to html" }
                logt { "send:$json" }
                client.send(json)
            }
        }
    }

    override fun onOpen() {
        Globals.mainScope.launch {
            MyDroidConst.sendUriMap.observeForever(sendUriMapOb)
        }
    }

    override fun onClose() {
        Globals.mainScope.launch {
            MyDroidConst.sendUriMap.removeObserver(sendUriMapOb)
        }
    }

    override fun onMessage(json: JSONObject) {
        if (json.has(API_WS_REQUEST_FILE)) {
            val uriUuid = json.optString(API_WS_REQUEST_FILE)
            val info = MyDroidConst.sendUriMap.value?.get(uriUuid)
            onSendFile(uriUuid, info)
        } else if (json.has(API_WS_FILE_DOWNLOAD_COMPLETE)) {
            val uriUuid = json.optString(API_WS_REQUEST_FILE)
            val info = MyDroidConst.sendUriMap.value?.get(uriUuid)
        }
    }

    private fun readFromRealPath(info: UriRealInfoEx) : InputStream? {
        try {
            val path = info.goodPath()
            if (path != null) {
                return FileInputStream(path)
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

    fun onFileSendComplete(info: UriRealInfoEx?) {
        logdNoFile { "${client.clientName} onFile Send Complete : $info" }
        client.scope?.launchOnIOThread {

        }
    }

    fun onSendFile(uriUuid:String, info: UriRealInfoEx?) {
        logdNoFile { "${client.clientName} onSend File : $info" }

        client.scope?.launchOnIOThread {
            var inputStream: InputStream? = null

            if (info != null) {
                val uri = info.uri

                //1. 尝试从真实路径直接读取
                logt { "onSend File try read from real Path ${info.goodPath()}" }
                inputStream = readFromRealPath(info)

                //2. 尝试从ContentResolver中直接读取
                if (inputStream == null) {
                    inputStream = readFromContentResolver(uri)
                    logt { "onSend File 222 $inputStream" }
                }
                //3. 转成media uri再读一次试试
//                if (inputStream == null) {
//                    val resolvedUri: Uri? = MediaUriResolver().resolveMediaStoreUri(Globals.app, uri)
//                    logt { "onSend File MediaStoreUri 333 $resolvedUri" }
//                    if (resolvedUri != null) {
//                        inputStream = readFromContentResolver(resolvedUri)
//                        logt { "onSend File MediaStoreUri 333 $inputStream" }
//                    }
//                }
            }

            if (inputStream != null && info != null) {
                sendFile(info.uriUuid, info.fileSize, info.goodName(), inputStream)
            } else {
                client.send(WSResultBean(CODE_SUC,
                    com.allan.androidlearning.R.string.file_not_exist.resStr(),
                    API_WS_SEND_FILE_NOT_EXIST, NotExistResult(uriUuid)).toJsonString())
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

        val api = if(fileSize == null || fileSize >= SMALL_FILE_DEFINE_SIZE) API_WS_SEND_FILE_CHUNK else API_WS_SEND_SMALL_FILE_CHUNK
        val totalChunks = if(fileSize != null) {
            val isNotFull = fileSize % chunkSize > 0
            (fileSize / chunkSize).toInt() + (if(isNotFull) 1 else 0)
        } else 0

        val startJson = WSResultBean(CODE_SUC,
            com.allan.androidlearning.R.string.send_file_start.resStr(), api,
            WSChunkActionResult("start",
                uriUuid, fileSize ?: 0,
                totalChunks, "")).toJsonString()
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
        val endJson = WSResultBean(CODE_SUC, 
            Globals.getString(com.allan.androidlearning.R.string.send_file_end), 
            api, 
            WSChunkActionResult("end", uriUuid, offset, index, fileName?:"")).toJsonString()
        client.send(endJson)
        logt { "send file end $endJson" }
    }
}