package com.au.module_imagecompressed

import androidx.core.net.toFile
import com.au.module_android.Globals
import com.au.module_android.utilsmedia.UriHelper
import com.au.module_imagecompressed.CropCircleImageFragment.Companion.DIR_CROP
import java.io.File

/**
 * 必须是：
 * 我的cacheDir或者fileDir下的文件来转成UriWrap。
 */
fun UriHelper.imageFileConvertToUriWrap() : UriWrap {
    assert(isFileScheme())
    val file = uri.toFile()
    return UriWrap(uri, 1, file.length(), isImage = true, beLimitedSize = false, beCopied = true, mimeType, file.name)
}

private const val SUB_CACHE_DIR = "luban_disk_cache"
private const val COPY_FILE_PREFIX = "copy_"

fun clearLubanAndCropCache(clearLubanCompress:Boolean = true, clearUCrop:Boolean = true) {
    if (clearLubanCompress) {
        try {
            val cmpImagesPath = File(Globals.app.cacheDir.absolutePath + "/$SUB_CACHE_DIR")
            cmpImagesPath.listFiles()?.forEach {
                it.delete()
            }
        } catch (e:Exception) {
            e.printStackTrace()
        }

        try {
            val cmpImagesPath = File(Globals.app.externalCacheDir?.absolutePath + "/$SUB_CACHE_DIR")
            cmpImagesPath.listFiles()?.forEach {
                it.delete()
            }
        } catch (e:Exception) {
            e.printStackTrace()
        }
    }

    if (clearUCrop) {
        try {
            val cmpImagesPath = File(Globals.app.cacheDir.absolutePath + "/$DIR_CROP")
            cmpImagesPath.listFiles()?.forEach {
                it.delete()
            }
        } catch (e:Exception) {
            e.printStackTrace()
        }

        try {
            val cmpImagesPath = File(Globals.app.externalCacheDir?.absolutePath + "/$DIR_CROP")
            cmpImagesPath.listFiles()?.forEach {
                it.delete()
            }
        } catch (e:Exception) {
            e.printStackTrace()
        }
    }
}