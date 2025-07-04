package com.allan.mydroid.beans.wsdata

import androidx.annotation.Keep

/**
 * 如果未知；则totalFileSize和totalChunks传0。
 */
data class WSChunkActionData(
    @Keep val action: String,
    @Keep val uriUuid:String,
    @Keep val totalFileSize:Long,
    @Keep val totalChunks:Int,
    @Keep val fileName:String
)