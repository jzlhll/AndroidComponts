package com.allan.mydroid.beans.httpdata

import androidx.annotation.Keep
import java.io.File

@Keep
data class ChunkInfoResult(val fileName:String,
                           val chunkIndex:Int,
                           val totalChunks:Int,
                           val md5:String,
                           @Transient
                     val chunkTmpFile: File
)