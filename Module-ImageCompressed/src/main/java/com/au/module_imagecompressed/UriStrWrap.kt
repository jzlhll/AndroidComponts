package com.au.module_imagecompressed

import androidx.annotation.Keep
import androidx.core.net.toUri

@Keep
data class UriStrWrap(var uriStr:String,
                   val totalNum:Int,
                   var fileSize:Long,
                   val isImage:Boolean,
                   var beLimitedSize:Boolean = false,
                   var beCopied:Boolean = false,
                   var mime:String,
                   var fileName:String) {
    override fun toString(): String {
        return "$uriStr, $fileName, $fileSize, $mime beCopied $beCopied beLimitedSize $beLimitedSize"
    }

    fun toUriWrap() = UriWrap(uriStr.toUri(), totalNum, fileSize, isImage, beLimitedSize, beCopied, mime, fileName)
}