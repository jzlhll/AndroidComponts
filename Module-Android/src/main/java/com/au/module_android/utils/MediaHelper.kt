package com.au.module_android.utils

import android.content.Context
import android.content.Intent
import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.util.Log
import android.webkit.MimeTypeMap
import androidx.core.content.FileProvider
import java.io.File

/**
 * @author au
 * @date :2024/5/22 14:01
 * @description: 配合
file_paths.xml
<?xml version="1.0" encoding="utf-8"?>
<paths>
<external-path
name="external_storage_root"
path="." />

<cache-path
name="videoShare"
path="videoShare" />

<!--代表app 外部存储区域根目录下的文件 Context.getExternalCacheDir目录下的目录-->
<external-cache-path
name="videoShare"
path="videoShare" />
</paths>

androidManifest.xml
<provider
android:name="androidx.core.content.FileProvider"
android:authorities="${applicationId}.fileprovider"
android:exported="false"
android:grantUriPermissions="true">

<!-- 元数据 -->
<meta-data
android:name="android.support.FILE_PROVIDER_PATHS"
android:resource="@xml/file_paths" />
</provider>
一起使用
 */
class MediaHelper {
    enum class MediaType {
        Other,
        Image,
        Video
    }

    companion object {
        /**
         * 转变为 分钟：秒。如果超过99分钟，就是99分钟。
         */
        fun convertMillisToMMSS(ts: Long): String {
            var minutes = (ts / (1000 * 60)).toInt()
            val seconds = ((ts / 1000) % 60).toInt()

            if (minutes >= 99) {
                minutes = 99
            }
            return String.format("%02d:%02d", minutes, seconds)
        }

        private fun isImageFileSimple(extension: String): Boolean {
            val imageExtensions = listOf("jpg", "jpeg", "png", "bmp", "webp", "heic", "heif", "tiff") //no gif
            return extension in imageExtensions
        }

        private fun isVideoFileSimple(extension: String): Boolean {
            val videoExtensions = listOf("mp4", "mov", "flv", "mkv", "webm", "m4v", "avi", "wmv", "3gp",)
            return extension in videoExtensions
        }

        fun getMimeTypePath(filePath: String): String {
            val extension = filePath.substring(filePath.lastIndexOf(".") + 1).lowercase()
            return MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension) ?: "*/*"
        }

        fun getMimeTypeUrl(url: String): String {
            val extension = MimeTypeMap.getFileExtensionFromUrl(url)
            return MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension) ?: "*/*"
        }
    }

    fun getDurationNormally(context: Context, uri: Uri): Long {
        var duration: Long = 0
        val retriever = MediaMetadataRetriever()
        try {
            retriever.setDataSource(context, uri)
            val time = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
            duration = time!!.toLong()
        } catch (ex: java.lang.Exception) {
            ex.printStackTrace()
        } finally {
            retriever.release()
        }
        return duration
    }

    fun getDurationNormally(path: String?): Long {
        var duration: Long = 0
        val retriever = MediaMetadataRetriever()
        try {
            if (path != null) {
                retriever.setDataSource(path)
            }
            val time = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
            duration = time!!.toLong()
        } catch (ex: java.lang.Exception) {
            ex.printStackTrace()
        } finally {
            retriever.release()
        }
        return duration
    }

    /**
     * 用这个方法准确点
     * 获取视频/音频时长,这里获取的是毫秒
     */
    fun getDurationComplexly(path: String?): Long {
        val mediaPlayer = MediaPlayer()
        try {
            mediaPlayer.setDataSource(path)
            mediaPlayer.prepare()
            val duration = mediaPlayer.duration
            mediaPlayer.release()
            return duration.toLong()
        } catch (e: java.lang.Exception) {
            mediaPlayer.release()
            e.printStackTrace()
        }
        return 0
    }

    /**
     * 通过MediaMetadataRetriever来解析
     */
    fun mediaTypeOf(filePath:String) : MediaType {
        var time = System.currentTimeMillis()
        var mimeType:String? = null
        val retriever = MediaMetadataRetriever()
        try {
            retriever.setDataSource(filePath)
            mimeType = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_MIMETYPE)
        } catch (e:Exception) {
            e.printStackTrace()
        } finally {
            retriever.release()
        }

        if (mimeType == null) {
            val extension = filePath.substring(filePath.lastIndexOf(".") + 1).lowercase()
            if(isImageFileSimple(extension)) return MediaType.Image
            if(isVideoFileSimple(extension)) return MediaType.Video
            Log.d("allan", "is imageFile time: $time")
            return MediaType.Other
        }

        val r1 = mimeType.startsWith("image/")
        val r2 = mimeType.startsWith("video/")
        time = System.currentTimeMillis() - time
        Log.d("allan", "is imageFile time: $time")
        return if(r1) MediaType.Image else (if(r2) MediaType.Video else MediaType.Other)
    }

    /**
     *
     */
    fun mediaTypeOf2(filePath:String) : MediaType {
        val type = getMimeTypePath(filePath)
        if (type.contains("video")) {
            return MediaType.Video
        } else if (type.contains("image")) {
            return MediaType.Image
        }
        return MediaType.Other
    }

    fun shareFile(context: Context, file: File?) {
        if (file != null && file.exists()) {
            val share = Intent(Intent.ACTION_SEND)
            val uri: Uri?

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                // "项目包名.fileprovider"即是在清单文件中配置的authorities
                uri = FileProvider.getUriForFile(context, context.packageName + ".provider", file)
                // 给目标应用一个临时授权
                share.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            } else {
                uri = Uri.fromFile(file)
            }

            share.putExtra(Intent.EXTRA_STREAM, uri)
            share.type = getMimeTypePath(file.absolutePath) // 此处可发送多种文件
            share.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            share.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            context.startActivityFix(Intent.createChooser(share, "分享文件"))
        }
    }

}