package com.au.module_android.utilsmedia

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import android.webkit.MimeTypeMap
import androidx.core.net.toFile
import com.au.module_android.utils.ignoreError
import com.au.module_android.utils.logd

/**
 * @author allan
 * @date :2024/10/24 15:41
 * @description: file和content的Uri形式的获取。
 */
class UriHelper(val uri: Uri, cr: ContentResolver) {
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
}

enum class ContentUriRealPathType {
    FullPath,
    RelativePath,
   // OnlyName
}

/**
 * 通过uri解析真实路径
 * @return 如果解析不出来，则是null。如果解析出来是路径，则是路径 to true；如果只能解析出名字，则是名字 to false。
 */
fun Uri.getRealPath(context: Context): Pair<String, ContentUriRealPathType>? {
    if (this.scheme == "file") {
        return toFile().absolutePath to ContentUriRealPathType.FullPath
    }

    var ret: Pair<String, ContentUriRealPathType>? = null
    val projection = arrayOf<String?>(MediaStore.MediaColumns.DATA, MediaStore.MediaColumns.DISPLAY_NAME)
    ignoreError {
        context.contentResolver.query(this, projection, null, null, null)?.use { cursor->
            if (cursor.moveToFirst()) {
                ignoreError {
                    val relativePathIndex = cursor.getColumnIndex(MediaStore.MediaColumns.RELATIVE_PATH)
                    val relativePath = if (relativePathIndex == -1) null else cursor.getString(relativePathIndex)
                    logd { "MediaStore RELATIVE_PATH: $relativePath" }
                    if (!relativePath.isNullOrEmpty()) {
                        ret = relativePath to ContentUriRealPathType.RelativePath
                    }
                }

                if (ret == null) {
                    ignoreError {
                        val dataIndex = cursor.getColumnIndex(MediaStore.MediaColumns.DATA)
                        val path = if (dataIndex == -1) null else cursor.getString(dataIndex)
                        logd { "MediaStore DATA: $path" }
                        if (path != null && !path.isEmpty()) {
                            ret = path to ContentUriRealPathType.FullPath
                        }
                    }
                }
            }
        }
    }
    return ret
}
