package com.allan.androidlearning.transfer.nanohttp

import com.allan.androidlearning.transfer.CODE_FAIL_FILE_SEND_ERR
import com.allan.androidlearning.transfer.MyDroidGlobalService
import com.allan.androidlearning.transfer.okJsonResponse
import com.au.module_android.Globals
import com.au.module_android.api.ResultBean
import com.au.module_android.utils.logt
import fi.iki.elonen.NanoHTTPD.IHTTPSession
import fi.iki.elonen.NanoHTTPD.Response
import fi.iki.elonen.NanoHTTPD.newChunkedResponse
import java.io.FileInputStream


class MyDroidSendFileMgr {

    /**
     * url 就是/download?uriUuid=abcdefge&ipRandomName=addf@name 格式
     */
    fun handleDownFileRequest(session: IHTTPSession) : Response {
        session.queryParameterString
        val uriUuid = session.queryParameterString.substringAfter("uriUuid=")
        logt { "handleDownFileRequest $uriUuid " }
        val info = MyDroidGlobalService.shareReceiverUriMap.realValue?.get(uriUuid)
        logt { "handleDownFileRequest info $info" }
        var errorInfo = ""
        do {
            if (info == null) {
                errorInfo = "文件不存在！"
                break
            }

            val uri = info.uri
            val inputStream = try {
                val cr = Globals.app.contentResolver
                cr.openInputStream(uri)
            } catch (e: Exception) {
                null
            }
            if (inputStream == null) {
                errorInfo = "文件流为空！"
                break
            }

            return newChunkedResponse(
                Response.Status.OK,
                "application/octet-stream", // MIME 类型
                inputStream
            ).apply {
                addHeader("Content-Disposition", "attachment; filename=\"${info.name}\"")
                addHeader("Access-Control-Allow-Origin", "*") // 允许跨域
            }
        } while (false)

        return ResultBean<String>(CODE_FAIL_FILE_SEND_ERR, errorInfo).okJsonResponse()
    }
}