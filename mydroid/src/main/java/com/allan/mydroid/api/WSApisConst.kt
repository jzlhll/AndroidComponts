package com.allan.mydroid.api

import com.au.annos.EncryptString

@EncryptString
class WSApisConst {
    companion object {
        const val API_WS_SEND_FILE_LIST = "s_sendFileList"
        const val API_WS_LEFT_SPACE = "s_leftSpace"
        const val API_WS_CLIENT_INIT_CALLBACK = "s_clientInitBack"

        const val API_WS_SEND_FILE_CHUNK = "s_sendFileChunk"
        const val API_WS_SEND_SMALL_FILE_CHUNK = "s_sendSmallFileChunk"

        const val API_WS_SEND_FILE_NOT_EXIST = "s_sendFileNotExist"

        const val API_WS_INIT = "c_wsInit"
        const val API_WS_PING = "c_ping"
        const val API_WS_REQUEST_FILE = "c_requestFile"
        const val API_WS_FILE_DOWNLOAD_COMPLETE = "c_downloadFileComplete"

        //textChat相关api
        const val API_WS_TEXT_CHAT_MSG = "cs_text_chat_msg"
    }
}