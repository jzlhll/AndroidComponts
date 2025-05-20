package com.allan.androidlearning.transfer.benas

import androidx.annotation.Keep
import java.io.File

const val MERGE_CHUNKS = "/merge-chunks"
const val UPLOAD_CHUNK = "/upload-chunk"
const val ABORT_UPLOAD_CHUNKS = "/abort-upload-chunks"

@Keep
data class MergedFileInfo(val file: File, val md5:String, val fileSizeInfo:String)