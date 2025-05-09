package com.au.module_android.utilsmedia

import android.annotation.SuppressLint
import android.content.Context
import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import android.net.Uri
import android.util.Log
import android.webkit.MimeTypeMap

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
        Video,
        Audio,
    }

    companion object {
        /**
         * 转变为 分钟：秒。如果超过99分钟，就是99分钟。
         */
        @SuppressLint("DefaultLocale")
        fun convertMillisToMMSS(ts: Long): String {
            var minutes = (ts / (1000 * 60)).toInt()
            val seconds = ((ts / 1000) % 60).toInt()

            if (minutes >= 99) {
                minutes = 99
            }
            return String.format("%02d:%02d", minutes, seconds)
        }

        fun isImageFileSimple(extension: String): Boolean {
            val imageExtensions = listOf("jpg", "jpeg", "png", "bmp", "webp", "heic", "heif", "tiff", "gif", "svg")
            return extension in imageExtensions
        }

        fun isVideoFileSimple(extension: String): Boolean {
            val videoExtensions = listOf("mp4", "mov", "flv", "mkv", "webm", "m4v", "avi", "wmv", "3gp",)
            return extension in videoExtensions
        }

        fun isAudioFileSimple(extension:String) : Boolean {
            val audioExtensions = listOf("mp3", "wav", "ogg", "aac", "flac", "wma", "m4a", "amr", "aiff", "ape", "wv", "mid", "midi")
            return extension in audioExtensions
        }

        /**
         * 根据文件路径，其实就是后缀，获取MimeType
         */
        fun getMimeTypePath(filePath: String): String {
            val extension = filePath.substring(filePath.lastIndexOf(".") + 1).lowercase()
            return MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension) ?: "*/*"
        }

        /**
         * 根据url，获取MimeType
         */
        fun getMimeTypeUrl(url: String): String {
            val extension = MimeTypeMap.getFileExtensionFromUrl(url)
            return MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension) ?: "*/*"
        }

        fun mediaTypeOfMimeType(mimeType:String) : MediaType{
            if (mimeType.contains("video")) {
                return MediaType.Video
            } else if (mimeType.contains("image")) {
                return MediaType.Image
            } else if (mimeType.contains("audio")) {
                return MediaType.Audio
            }
            return MediaType.Other
        }

        /**
         * 直接根据后缀，来判断类型。推荐使用。
         * @param filePath 文件路径
         * @return MediaType 都会有
         */
        fun mediaTypeOfFilePath(filePath:String) : MediaType {
            return mediaTypeOfMimeType(getMimeTypePath(filePath))
        }
    }

    /**
     * 使用系统方法获取 video/audio Url时长
     * @return 时长，毫秒
     */
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

    /**
     * 使用系统方法获取video/audio 时长
     * @return 时长，毫秒
     */
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
     * 使用mediaPlayer准备的方式，来获取时长，据说更加精准。
     * @return 时长，毫秒
     */
    @Deprecated( "getDurationNormally(path)")
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
     * 通过系统retriever解析。获取具体的类型文件。不太推荐使用。
     * @param filePath 本函数预设filePath是一个媒体文件。只是不知道具体格式。
     * @return MediaType 只会是video/audio/other未知。
     */
    @Deprecated("mediaTypeOfFilePath / mediaTypeOfMimeType")
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
            Log.d("MediaHelper", "is imageFile time: $time")
            return MediaType.Other
        }

        val r1 = mimeType.startsWith("image/")
        val r2 = mimeType.startsWith("video/")
        time = System.currentTimeMillis() - time
        Log.d("MediaHelper", "is imageFile time: $time")
        return if(r1) MediaType.Image else (if(r2) MediaType.Video else MediaType.Other)
    }
}
