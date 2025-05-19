package com.au.module_android.permissions

import android.Manifest
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log

class PermissionStorageHelper {
    /**
     * 根据一个uri获取PermissionMediaType。
     */
    fun getPermissionMediaTypeFromUri(context: Context, uri: Uri): PermissionMediaType? {
        val pathSegments = uri.pathSegments
        if (pathSegments.size >= 2) {
            val typeSegment = pathSegments[1] // 路径如 external/images/media/...
            when (typeSegment) {
                "images" -> return PermissionMediaType.IMAGE
                "video" -> return PermissionMediaType.VIDEO
                "audio" -> return PermissionMediaType.AUDIO
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
                                return PermissionMediaType.IMAGE
                            } else if (mimeType.startsWith("video/")) {
                                return PermissionMediaType.VIDEO
                            } else if (mimeType.startsWith("audio/")) {
                                return PermissionMediaType.AUDIO
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
    fun getRequiredPermissions(mediaTypes: Array<PermissionMediaType>) : Array<String> {
        val permissions: MutableList<String> = ArrayList()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { // Android 14+
            // Android 14+需额外声明READ_MEDIA_VISUAL_USER_SELECTED
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                permissions.add(Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED)
            }
            mediaTypes.forEach {mediaType->
                when (mediaType) {
                    PermissionMediaType.IMAGE -> permissions.add(Manifest.permission.READ_MEDIA_IMAGES)
                    PermissionMediaType.VIDEO -> permissions.add(Manifest.permission.READ_MEDIA_VIDEO)
                    PermissionMediaType.AUDIO -> permissions.add(Manifest.permission.READ_MEDIA_AUDIO)
                    PermissionMediaType.STORAGE -> permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE)
                }
            }
        } else { // Android 12及以下
            permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }

        return permissions.toTypedArray()
    }
}