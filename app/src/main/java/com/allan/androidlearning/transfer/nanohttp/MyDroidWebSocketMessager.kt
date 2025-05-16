package com.allan.androidlearning.transfer.nanohttp

import com.allan.androidlearning.transfer.CODE_SUC
import com.allan.androidlearning.transfer.MyDroidGlobalService
import com.allan.androidlearning.transfer.benas.UriRealInfoEx
import com.allan.androidlearning.transfer.benas.toCNName
import com.allan.androidlearning.transfer.getWSSendFileChunkSize
import com.allan.androidlearning.transfer.htmlbeans.API_CLIENT_INIT_CALLBACK
import com.allan.androidlearning.transfer.htmlbeans.API_SEND_FILE_CHUNK
import com.allan.androidlearning.transfer.htmlbeans.API_SEND_FILE_START_NOT_EXIST
import com.allan.androidlearning.transfer.htmlbeans.MyDroidModeResult
import com.allan.androidlearning.transfer.htmlbeans.NotExistResult
import com.allan.androidlearning.transfer.htmlbeans.WSResultBean
import com.allan.androidlearning.transfer.htmlbeans.WSSendFileChunkResult
import com.au.module_android.Globals
import com.au.module_android.json.toJsonString
import com.au.module_android.utils.launchOnIOThread
import com.au.module_android.utils.logdNoFile
import com.au.module_android.utils.logt
import com.au.module_androidui.toast.ToastBuilder
import java.io.InputStream

class MyDroidWebSocketMessager(val socket: MyDroidWebSocket) : MyDroidWebSocket.IOnMessage {
//    override fun onSendFile(info: UriRealInfoEx) {
//        url == "/download" -> {
//            val uriUuid = session.parameters["uriUuid"]?.firstOrNull() ?: ""
//            val ipRandomName = session.parameters["ipRandomName"]?.firstOrNull() ?: ""
//            val info = MyDroidGlobalService.shareReceiverUriMap.realValue?.get(uriUuid)
//            if (info == null) {
//                return ResultBean<DownWillStartResult>(CODE_FAIL_FILE_SEND_ERR, "文件不存在！", null).okJsonResponse()
//            } else {
//                webSocketServer?.startSendFileToClientAsync(ipRandomName, info)
//                logdNoFile { "it will start download...." }
//                ResultBean(CODE_SUC, "Success!", DownWillStartResult()).okJsonResponse()
//            }
//        }
//    }

    override fun onNewClientInit() {
        //通过later则不需要注意线程
        ToastBuilder().setMessage("${socket.clientName} 新的网页接入！").setIcon("success").setOnTopLater(200).toast()
        val mode = MyDroidGlobalService.myDroidModeData.realValue?.toCNName() ?: "--"
        val ret = WSResultBean(CODE_SUC, "success!", API_CLIENT_INIT_CALLBACK,
            MyDroidModeResult(mode,
            socket.clientName))
        val json = ret.toJsonString()
        logt { "send: $json" }
        socket.send(json)
    }

    override fun onSendFile(uriUuid:String, info: UriRealInfoEx?) {
        logdNoFile { "${socket.clientName} start SendFileToClient Async client : $info" }
        socket.scope?.launchOnIOThread {
            var fileExists = false

            if (info != null && false) { //todo
                val fileSize = info.fileSize
                val uri = info.uri
                try {
                    Globals.app.contentResolver.openInputStream(uri)?.use {
                        if (fileSize == null) {
                            sendFileNoSize(info.uriUuid, it)
                        } else {
                            sendFile(info.uriUuid, fileSize, it)
                        }
                    }
                    fileExists = true
                } catch (e: Exception) {
                    // 可根据实际需要记录日志或上报错误
                    e.printStackTrace()
                }
            }

            if (!fileExists) {
                socket.send(WSResultBean(CODE_SUC, "文件不存在", API_SEND_FILE_START_NOT_EXIST, NotExistResult(uriUuid)).toJsonString())
            }
        }
    }

    //todo 现在是直接所有包都发过去。没做ack再发
    private fun sendFile(uriUuid:String, fileSize:Long?, inputStream: InputStream) {
        TODO()
    }

    private fun sendFileNoSize(uriUuid:String, inputStream: InputStream) {
        val chunkSize = getWSSendFileChunkSize(null).toInt()
        val buffer = ByteArray(chunkSize)

        // 发送文件元数据
        socket.send(WSResultBean(CODE_SUC, "success!", API_SEND_FILE_CHUNK,
            WSSendFileChunkResult(uriUuid, -1, -1)).toJsonString())

        // 分片发送文件内容
        var bytesRead: Int
        while (inputStream.read(buffer).also { bytesRead = it } != -1) {
            val chunk = if (bytesRead == buffer.size) buffer else buffer.copyOf(bytesRead)
            socket.send(chunk)//发送二进制数据
        }
    }
}