package com.allan.androidlearning.transfer

import androidx.annotation.DrawableRes
import com.allan.androidlearning.R
import com.au.module_android.Globals
import com.au.module_android.api.ResultBean
import com.au.module_android.json.toJsonString
import com.au.module_android.utilsmedia.MediaHelper
import fi.iki.elonen.NanoHTTPD.Response
import fi.iki.elonen.NanoHTTPD.Response.Status
import fi.iki.elonen.NanoHTTPD.newFixedLengthResponse
import java.io.File

/**
 * 调试模式下，上传文件会变慢。
 */
const val DEBUG_SLOW_RECEIVER_TRANSFER = false
/**
 * 调试模式下，上传文件会变慢。
 */
const val DEBUG_SLOW_SEND_TRANSFER = false

const val KEY_AUTO_ENTER_SEND_VIEW = "key_auto_import"
const val KEY_START_TYPE = "entry_start_type"
const val MY_DROID_SHARE_IMPORT_URIS = "myDroidShareReceiverUris"

const val CACHE_IMPORT_COPY_DIR = "nanoImport"
fun cacheImportCopyDir() = Globals.goodCacheDir.absolutePath + File.separatorChar + CACHE_IMPORT_COPY_DIR

private const val TEMP_CACHE_DIR = "nanoTmp"
fun nanoTempCacheDir()  = Globals.goodCacheDir.absolutePath + File.separatorChar + "shared" + File.separatorChar + TEMP_CACHE_DIR

private const val TEMP_CACHE_CHUNKS_DIR = "nanoChunksTmp"
fun nanoTempCacheChunksDir()  = Globals.goodCacheDir.absolutePath + File.separatorChar + "shared" + File.separatorChar + TEMP_CACHE_CHUNKS_DIR

private const val TEMP_CACHE_MERGED_DIR = "nanoMerged"
fun nanoTempCacheMergedDir()  = Globals.goodCacheDir.absolutePath + File.separatorChar + "shared" + File.separatorChar + TEMP_CACHE_MERGED_DIR

const val MIME_TYPE_JSON = "application/json; charset=UTF-8"

/**
 * 250MB就认为是小文件。
 */
const val SMALL_FILE_DEFINE_SIZE = 250 * 1024 * 1024L

const val CODE_SUC = "0"
const val CODE_FAIL = "-1"
const val CODE_FAIL_FILE_SEND_ERR = "-2"
const val CODE_FAIL_RECEIVER_CHUNK = "-101"
const val CODE_FAIL_MERGE_CHUNK = "-102"
const val CODE_FAIL_MD5_CHECK = "-103"

// 64k 600kb/s 256k 2MB/s 512k 2.8~3MB/s 1M 6MB/s 分块越大，越快。
fun getWSSendFileChunkSize(fileSize:Long?) : Long{
    val b = 1024L
    val mb = 1024 * 1024L
    if (fileSize == null) return mb
    return if (fileSize <= 10 * mb) {
        b * 256
    } else if (fileSize <= 100 * mb) {
        b * 512
    } else if (fileSize <= 500 * mb) {
        mb
    } else {
        2 * mb
    }
}

fun ResultBean<*>.okJsonResponse() : Response{
    return newFixedLengthResponse(
        Status.OK,
        MIME_TYPE_JSON,
        this.toJsonString()
    )
}

fun ResultBean<*>.badRequestJsonResponse() : Response{
    return newFixedLengthResponse(
        Status.BAD_REQUEST,
        MIME_TYPE_JSON,
        this.toJsonString()
    )
}

fun ResultBean<*>.jsonResponse(status: Response.IStatus) : Response{
    return newFixedLengthResponse(
        status,
        MIME_TYPE_JSON,
        this.toJsonString()
    )
}

@DrawableRes
fun getIcon(fileName: String?): Int {
    // 提取文件后缀并转为小写（处理无后缀的情况）
    val extension = fileName?.substringAfterLast('.', "")?.lowercase() ?: ""
    return when (extension) {
        // 文本/文档类型
        "doc", "docx" -> R.drawable.ic_filetype_doc
        "xls", "xlsx" -> R.drawable.ic_filetype_xls
        "pdf" -> R.drawable.ic_filetype_pdf
        "txt", "log", "md" -> R.drawable.ic_filetype_text
        // 压缩包类型
        "zip", "rar", "tar", "gz", "7z" -> R.drawable.ic_filetype_archive
        // 代码文件类型（可选扩展）
        "java", "kt", "py", "js", "html", "css" -> R.drawable.ic_filetype_code
        // 其他类型
        else ->
            if (MediaHelper.isImageFileSimple(extension)) {
                R.drawable.ic_filetype_image
            } else if (MediaHelper.isAudioFileSimple(extension)) {
                R.drawable.ic_filetype_audio
            } else if (MediaHelper.isVideoFileSimple(extension)) {
                R.drawable.ic_filetype_video
            } else {
                R.drawable.ic_filetype_other
            }
    }
}