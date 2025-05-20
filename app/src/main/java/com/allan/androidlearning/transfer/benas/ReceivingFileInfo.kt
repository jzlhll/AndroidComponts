package com.allan.androidlearning.transfer.benas

import androidx.annotation.Keep
import java.io.File

const val PROCESS_CHUNK = "chunk"
const val PROCESS_MERGING = "merging"
const val PROCESS_ABORTED = "aborted"
const val PROCESS_COMPLETED = "completed"
const val PROCESS_CHUNK_ERROR = "chunkError"
const val PROCESS_MERGE_ERROR = "mergeError"

/**
 * process可选：chunk, merging, aborted, completed, chunkError, mergeError
 */
data class ReceivingFileInfo(val fileName:String,
                             val md5:String,
                             val currentChunkIndex:Int,
                             val totalChunks:Int,
                             val process:String,
                             val error:String? = null)