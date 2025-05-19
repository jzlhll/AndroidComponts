package com.allan.androidlearning.transfer.htmlbeans

import androidx.annotation.Keep

/**
 * 如果未知；则totalFileSize和totalChunks传0。
 */
data class WSChunkResult(
    @Keep val action: String,
    @Keep val uriUuid:String,
    @Keep val totalFileSize:Long,
    @Keep val totalChunks:Int,
    @Keep val fileName:String
)