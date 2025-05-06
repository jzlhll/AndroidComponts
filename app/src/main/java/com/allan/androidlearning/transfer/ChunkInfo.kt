package com.allan.androidlearning.transfer

import java.io.File

data class ChunkInfo(val fileName:String,
                     val chunkIndex:Int,
                     val totalChunks:Int,
                     val md5:String,
                     val chunkTmpFile: File)
