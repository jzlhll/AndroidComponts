package com.au.module_android.utilsmedia

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import com.au.module_android.Globals
import com.au.module_android.permissions.hasPermission
import com.au.module_android.utils.ignoreError
import com.au.module_android.utils.logt
import com.au.module_android.utilsmedia.MediaHelper.MediaType
import kotlinx.coroutines.delay
import java.io.File
import java.io.IOException


/**
 * 导出到download目录。
 * 似乎是能支持。而且不要权限。
 */
suspend fun exportFileToDownload(outputFileName: String, sourceFile: File): String {
    // 验证源文件是否存在
    if (!sourceFile.exists()) {
        return "error: Source file does not exist."
    }

    // 获取下载目录
    val has = hasPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    logt { "has permission $has" }
    val directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)

    val generateUniqueFileName = fun(directory: File, originalName: String): File {
        // 分离文件名和扩展名
        val (name, ext) = if (originalName.contains('.')) {
            val lastDotIndex = originalName.lastIndexOf('.')
            originalName.substring(0, lastDotIndex) to originalName.substring(lastDotIndex)
        } else {
            originalName to ""
        }

        var counter = 0
        var uniqueFile = File(directory, originalName)

        // 循环查找可用的文件名
        while (uniqueFile.exists()) {
            counter++
            val newName = if (ext.isNotEmpty()) {
                "${name}-${counter}${ext}"
            } else {
                "${name}-${counter}"
            }
            uniqueFile = File(directory, newName)
        }

        return uniqueFile
    }

    // 创建目标文件
    val destFile = generateUniqueFileName(directory, outputFileName)
    delay(0)
    try {
        // 使用文件流拷贝
        sourceFile.inputStream().use { input ->
            destFile.outputStream().use { output ->
                input.copyTo(output)
            }
        }
        return "Success! " + destFile.absolutePath + "\n" + destFile.absolutePath
    } catch (e: IOException) {
        e.printStackTrace()
        return "error: ${e.localizedMessage}"
    }
}

/**
 * 保存文件（支持大文件）
 */
fun saveFileToPublicDirectory(
    context: Context,
    origFile: File,
    deleteOldFile: Boolean = true,
    path: String,
    setContentValues: Function1<ContentValues, Unit>? = null
) : Uri?{
    if (origFile.isDirectory) {
        return null
    }

    val uri = insertFileToContentResolverFile(context, origFile, origFile.name, path, setContentValues)
    logt { "insert FileToContent ResolverFile $uri" }
    var isSuc = false
    if (uri != null) {
        context.contentResolver.openOutputStream(uri)?.use { outputStream ->
            origFile.inputStream().buffered().use { inputStream ->
                val buffer = ByteArray(1024 * 32)
                var bytesRead: Int
                while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                    outputStream.write(buffer, 0, bytesRead)
                    isSuc = true
                }
            }
        }

        ignoreError {
            if (isSuc && deleteOldFile) {
                origFile.delete()
            }
        }
    }
    return if(isSuc) uri else null
}

fun deleteFromContentResolver(mediaUri: Uri) {
    Globals.app.contentResolver.delete(mediaUri, null, null)
}

/**
 * 获取想要插入到的目标位置的uri
 *
 * @param mediaType 文件类型 使用函数：mediaTypeOf2 来解析。
 * @param mimeType 文件类型
 * @param displayName 目标文件名
 * @param subPath 目标在媒体文件夹下的子目录
 * @param contentValuesAction 给你额外操作的可能。
 * @return 返回文件uri
 */
private fun insertFileToContentResolver(
    context: Context,
    mediaType: MediaType,
    mimeType: String,
    displayName: String,
    subPath: String,
    contentValuesAction: Function1<ContentValues, Unit>? = null
) : Uri? {
    return ignoreError {
        //设置保存参数到ContentValues中
        val contentValues = ContentValues()
        //执行insert操作，向系统文件夹中添加文件
        //EXTERNAL_CONTENT_URI代表外部存储器，该值不变
        val contentResolver = context.contentResolver
        val saveUri = when (mediaType) {
            MediaType.Video -> {
                //设置文件名
                contentValues.put(MediaStore.Video.Media.DISPLAY_NAME, displayName)
                contentValues.put(MediaStore.Video.Media.TITLE, displayName)
                //设置文件类型
                contentValues.put(MediaStore.Video.Media.MIME_TYPE, mimeType)
                //兼容Android Q和以下版本
                if (!isExternalStorageLegacy) {
                    //android Q中不再使用DATA字段，而用RELATIVE_PATH代替
                    //RELATIVE_PATH是相对路径不是绝对路径
                    //DCIM是系统文件夹，关于系统文件夹可以到系统自带的文件管理器中查看，不可以写没存在的名字
                    contentValues.put(MediaStore.Video.Media.RELATIVE_PATH, "Movies/${subPath}")
                } else {
                    contentValues.put(MediaStore.Video.Media.DATA, getOldSdkPath(displayName, subPath, "Movies"))
                }
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI
            }

            MediaType.Image -> {
                contentValues.put(MediaStore.Images.Media.DISPLAY_NAME, displayName)
                contentValues.put(MediaStore.Images.Media.TITLE, displayName)
                contentValues.put(MediaStore.Images.Media.MIME_TYPE, mimeType)
                if (!isExternalStorageLegacy) {
                    contentValues.put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/${subPath}")
                } else {
                    contentValues.put(MediaStore.Images.Media.DATA, getOldSdkPath(displayName, subPath, "Pictures"))
                }
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            }

            MediaType.Audio -> {
                contentValues.put(MediaStore.Audio.Media.DISPLAY_NAME, displayName)
                contentValues.put(MediaStore.Audio.Media.TITLE, displayName)
                contentValues.put(MediaStore.Audio.Media.MIME_TYPE, mimeType)
                if (!isExternalStorageLegacy) {
                    contentValues.put(MediaStore.Audio.Media.RELATIVE_PATH, "Music/${subPath}")
                } else {
                    contentValues.put(MediaStore.Audio.Media.DATA, getOldSdkPath(displayName, subPath, "Music"))
                }
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
            }

            MediaType.Other -> {
                contentValues.put(MediaStore.Downloads.DISPLAY_NAME, displayName)
                contentValues.put(MediaStore.Downloads.TITLE, displayName)
                contentValues.put(MediaStore.Downloads.MIME_TYPE, mimeType)
                if (!isExternalStorageLegacy) {
                    contentValues.put(MediaStore.Downloads.RELATIVE_PATH, "Download/${subPath}")
                    MediaStore.Downloads.EXTERNAL_CONTENT_URI
                } else {
                    contentValues.put(MediaStore.Files.FileColumns.DATA, getOldSdkPath(displayName, subPath, "Download"))
                    MediaStore.Files.getContentUri("external")
                }
            }
        }
        contentValuesAction?.invoke(contentValues)
        return contentResolver.insert(saveUri, contentValues)
    }
}

/**
 * 获取想要插入到的目标位置的uri
 *
 * 将原来的origUri，转成mediaType和mimeType
 * 再调用insertFileToContentResolver
 */
fun insertFileToContentResolverUri(
    context: Context,
    origUri:Uri,
    displayName: String,
    subPath: String,
    contentValuesAction: Function1<ContentValues, Unit>? = null
) : Uri? {
    val mimeType = origUri.getUriMimeType(context.contentResolver)
    val mediaType = MediaHelper.mediaTypeOfMimeType(mimeType)
    return insertFileToContentResolver(context, mediaType, mimeType, displayName, subPath, contentValuesAction)
}

/**
 * 获取想要插入到的目标位置的uri
 *
 * 将原来的origFile，转成mediaType和mimeType
 * 再调用insertFileToContentResolver
 */
fun insertFileToContentResolverFile(
    context: Context,
    origFile: File,
    displayName: String,
    subPath: String,
    contentValuesAction: Function1<ContentValues, Unit>? = null
) : Uri? {
    val origUri = Uri.fromFile(origFile)
    return insertFileToContentResolverUri(context, origUri, displayName, subPath, contentValuesAction)
}

/**
 * 是否是用原来的存储
 * true:没有启用分区存储
 * false:当前是分区存储
 */
val isExternalStorageLegacy: Boolean
    get() = Build.VERSION.SDK_INT < Build.VERSION_CODES.Q || Environment.isExternalStorageLegacy()

private fun getOldSdkPath(
    displayName: String,
    path: String,
    type: String,
): String {
    val oldSdkPathDir =
        "${Environment.getExternalStorageDirectory().absolutePath + "/$type/"}${path}/"
    val dir = File(oldSdkPathDir)
    if (!dir.exists()) {
        dir.mkdirs()
    }
    val file = File("$oldSdkPathDir$displayName")
    if (!file.exists()) {
        file.createNewFile()
    }
    return file.absolutePath
}
