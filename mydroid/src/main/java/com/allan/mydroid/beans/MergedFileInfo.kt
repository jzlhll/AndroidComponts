package com.allan.mydroid.beans

import androidx.annotation.Keep
import java.io.File

const val MERGE_CHUNKS = "/merge-chunks"
const val UPLOAD_CHUNK = "/upload-chunk"
const val ABORT_UPLOAD_CHUNKS = "/abort-upload-chunks"
const val TEXT_CHAT_READ_WEBSOCKET_IP_PORT = "/text-chat-read-websocket-ip-port"

@Keep
data class MergedFileInfo(val file: File, val md5:String, val fileSizeInfo:String)