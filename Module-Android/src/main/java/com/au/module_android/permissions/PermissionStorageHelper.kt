package com.au.module_android.permissions

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import androidx.core.net.toUri
import com.au.module_android.utils.startActivityFix

/**
 * 兼容到android14+的权限申请策略
 */
class PermissionStorageHelper {
    enum class MediaType {
        IMAGE,
        VIDEO,
        AUDIO,
    }
    
    /**
     * 根据一个uri获取PermissionMediaType。
     */
    fun getPermissionMediaTypeFromUri(context: Context, uri: Uri): MediaType? {
        val pathSegments = uri.pathSegments
        if (pathSegments.size >= 2) {
            val typeSegment = pathSegments[1] // 路径如 external/images/media/...
            when (typeSegment) {
                "images" -> return MediaType.IMAGE
                "video" -> return MediaType.VIDEO
                "audio" -> return MediaType.AUDIO
            }
        }
        val projection = arrayOf<String?>(MediaStore.MediaColumns.MIME_TYPE)
        try {
            context.contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val index = cursor.getColumnIndex(MediaStore.MediaColumns.MIME_TYPE)
                    if (index >= 0) {
                        val mimeType: String? = cursor.getString(index)
                        if (mimeType != null) {
                            if (mimeType.startsWith("image/")) {
                                return MediaType.IMAGE
                            } else if (mimeType.startsWith("video/")) {
                                return MediaType.VIDEO
                            } else if (mimeType.startsWith("audio/")) {
                                return MediaType.AUDIO
                            }
                        }
                    }
                }
            }
        } catch (e: SecurityException) {
            e.printStackTrace()
            Log.d("MediaUtils", "Permission denied to access URI: $uri")
        }
        return null
    }

    /**
     * 获取指定媒体类型所需的运行时权限
     */
    fun getRequiredPermissions(mediaTypes: Array<MediaType>) : Array<String> {
        val permissions: MutableList<String> = ArrayList()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { // Android 14+
            // Android 14+需额外声明READ_MEDIA_VISUAL_USER_SELECTED
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                permissions.add(Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED)
            }
            mediaTypes.forEach {mediaType->
                when (mediaType) {
                    MediaType.IMAGE -> permissions.add(Manifest.permission.READ_MEDIA_IMAGES)
                    MediaType.VIDEO -> permissions.add(Manifest.permission.READ_MEDIA_VIDEO)
                    MediaType.AUDIO -> permissions.add(Manifest.permission.READ_MEDIA_AUDIO)
                }
            }
        } else { // Android 12及以下
            permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }

        return permissions.toTypedArray()
    }

    fun ifGotoMgrAll(showDialogBlock:()->Unit) : Boolean{
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val ex = Environment.isExternalStorageManager()
            if (!ex) {
                showDialogBlock()
            }
            return ex
        }

        return true
    }

    fun gotoMgrAll(context: Context) {
        val intent = Intent().apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                action = Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION
            }
            data = "package:${context.packageName}".toUri()
        }
        context.startActivityFix(intent)
    }
}