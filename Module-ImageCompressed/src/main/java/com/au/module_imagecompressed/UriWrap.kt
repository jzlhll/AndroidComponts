package com.au.module_imagecompressed

import android.net.Uri
import androidx.annotation.Keep

/**
 * @param beLimitedSize null表示没有进行过size check；被限制了size则会被标记，那么也意味着是原始的Uri。
 * @param beCopied 被拷贝则代表这个Uri是File型的。
 *
 * 不能拿这个去做gson。需要转换后才能处理。
 */
@Keep
data class UriWrap(var uri: Uri,
                   val totalNum:Int,
                   var fileSize:Long,
                   val isImage:Boolean,
                   var beLimitedSize:Boolean = false,
                   var beCopied:Boolean = false,
                   var mime:String,
                   var fileName:String) {
    override fun toString(): String {
        return "$uri, $fileName, $fileSize, $mime beCopied $beCopied beLimitedSize $beLimitedSize"
    }

    fun toUriStrWrap() = UriStrWrap(uri.toString(), totalNum, fileSize, isImage, beLimitedSize, beCopied, mime, fileName)
}