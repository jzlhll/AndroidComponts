package com.au.module_imagecompressed.util

import android.webkit.MimeTypeMap

/**
 * @author allan
 * @date :2024/10/24 15:41
 * @description: file和content的Uri形式的获取。
 */
class MimeUtil(val mimeType: String) {
    /**
     * 根据类型和Id，给出一个不错的名字。
     * @return 第一个参数是mimeType，第二个参数是fileName
     */
    fun goodMimeTypeAndFileName() : Pair<String, String> {
        val namePart = "" + System.currentTimeMillis() + "_" + (Math.random() * 100).toInt()
        val extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType)?.lowercase()
        if (extension != null && mimeType.startsWith("video")) {
            return mimeType to "video_$namePart.$extension"
        }
        if (extension != null && mimeType.startsWith("image")) {
            return mimeType to "pic_$namePart.$extension"
        }
        return mimeType to "media_$namePart"
    }

    fun isUriHeic() : Boolean{
        val extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType)
        return extension?.lowercase() == "heic"
    }

    fun isUriVideo(): Boolean {
        return mimeType.startsWith("video/")
    }

    fun isUriImage(): Boolean {
        return mimeType.startsWith("image/")
    }
}