package com.allan.mydroid.beansinner

/**
 * process可选：chunk, merging, aborted, completed, chunkError, mergeError
 */
data class ReceivingFileInfo(val fileName:String,
                             val md5:String,
                             val currentChunkIndex:Int,
                             val totalChunks:Int,
                             val process:String,
                             val error:String? = null)