package com.allan.androidlearning.transfer.htmlbeans

import androidx.annotation.Keep

data class WSChunkResult(
    @Keep val action: String,
    @Keep val endIndex:Int
)