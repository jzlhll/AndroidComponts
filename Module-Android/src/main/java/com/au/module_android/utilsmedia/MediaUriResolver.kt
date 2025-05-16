package com.au.module_android.utilsmedia

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import android.util.Log

class MediaUriResolver {
    /**
     * 解析并验证传入的 contentUri，返回标准的 MediaStore URI（若存在）.
     * 若 URI 无效或权限不足，返回 null.
     *
     * @param context    上下文
     * @param contentUri 输入的 contentUri（如 content://media/external/images/media/11681）
     * @return 合法的 MediaStore URI 或 null
     */
    fun resolveMediaStoreUri(context: Context, contentUri: Uri): Uri? {
        try {
            // 1. 解析 URI 路径，判断媒体类型
            val pathSegments = contentUri.pathSegments
            if (pathSegments.size < 4 || "media" != pathSegments[2]) {
                Log.w("MediaUriResolver", "Invalid URI format: $contentUri")
                return null
            }

            val mediaType = pathSegments[1] // images, video, audio
            val id = ContentUris.parseId(contentUri)

            // 2. 根据媒体类型确定对应的 MediaStore URI
            val mediaStoreUri = getMediaStoreUriByType(mediaType)
            if (mediaStoreUri == null) {
                Log.w("MediaUriResolver", "Unsupported media type: $mediaType")
                return null
            }

            // 3. 查询 MediaStore 确认 ID 存在
            if (!isMediaIdExists(context, mediaStoreUri, id)) {
                Log.w("MediaUriResolver", "Media ID $id does not exist")
                return null
            }

            // 4. 构建并返回标准的 MediaStore URI
            return ContentUris.withAppendedId(mediaStoreUri, id)
        } catch (e: NumberFormatException) {
            Log.e("MediaUriResolver", "Invalid ID in URI: $contentUri", e)
            return null
        } catch (e: SecurityException) {
            Log.e("MediaUriResolver", "Permission denied: " + e.message)
            return null
        }
    }

    // 根据媒体类型返回对应的 MediaStore URI
    private fun getMediaStoreUriByType(mediaType: String): Uri? {
        return when (mediaType) {
            "images" -> MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            "video" -> MediaStore.Video.Media.EXTERNAL_CONTENT_URI
            "audio" -> MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
            else -> null
        }
    }

    // 查询 MediaStore 检查指定 ID 是否存在
    private fun isMediaIdExists(context: Context, mediaStoreUri: Uri, id: Long): Boolean {
        val projection = arrayOf<String?>(MediaStore.MediaColumns._ID)
        val selection = MediaStore.MediaColumns._ID + " = ?"
        val selectionArgs = arrayOf<String?>(id.toString())

        try {
            context.contentResolver.query(
                mediaStoreUri,
                projection,
                selection,
                selectionArgs,
                null
            ).use { cursor ->
                return cursor != null && cursor.getCount() > 0
            }
        } catch (e: Exception) {
            Log.e("MediaUriResolver", "Query failed: " + e.message)
            return false
        }
    }
}