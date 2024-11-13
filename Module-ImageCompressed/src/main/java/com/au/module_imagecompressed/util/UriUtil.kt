package com.au.module_imagecompressed.util

import android.content.ContentResolver
import android.net.Uri
import android.webkit.MimeTypeMap
import androidx.core.net.toFile
import com.au.module_imagecompressed.UriWrap

/**
 * @author allan
 * @date :2024/10/24 15:41
 * @description: file和content的Uri形式的获取。
 */
class UriUtil(private val uri: Uri, cr: ContentResolver) {
    val mimeType = uri.getUriMimeType(cr)

    private val mimeUtil = MimeUtil(mimeType)

    fun isFileScheme() = uri.scheme == "file"

    fun isContentScheme() = uri.scheme == "content"

    /**
     * 根据类型和Id，给出一个不错的名字。
     * @return 第一个参数是mimeType，第二个参数是fileName
     */
    fun goodMimeTypeAndFileName() : Pair<String, String> {
        return if (isFileScheme()) {
            mimeType to uri.toFile().name
        } else {
            goodMimeTypeAndFileNameUri()
        }
    }

    /**
     * 根据类型和Id，给出一个不错的名字。
     * @return 第一个参数是mimeType，第二个参数是fileName
     */
    private fun goodMimeTypeAndFileNameUri() : Pair<String, String> {
        val uriStr = this.toString()
        val last = uriStr.indexOfLast { it == '/' } + 1
        var namePart = (if(last > 0) uriStr.substring(last) else null)
        if (namePart == null || namePart.length > 40) {
            namePart = "" + System.currentTimeMillis() + "_" + (Math.random() * 100).toInt()
        }
        val extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType)?.lowercase()
        if (extension != null && mimeType.startsWith("video")) {
            return mimeType to "video_$namePart.$extension"
        }
        if (extension != null && mimeType.startsWith("image")) {
            return mimeType to "pic_$namePart.$extension"
        }
        return mimeType to "media_$namePart"
    }

    fun isUriHeic() = mimeUtil.isUriHeic()

    fun isUriVideo() = mimeUtil.isUriVideo()

    fun isUriImage() = mimeUtil.isUriImage()

    /**
     * 必须是：
     * 我的cacheDir或者fileDir下的文件来转成UriWrap。
     */
    fun myFileConvertToUriWrap() : UriWrap {
        assert(isFileScheme())
        val file = uri.toFile()
        return UriWrap(uri, 1, file.length(), true, false, true, mimeType, file.name)
    }
}