package com.au.module_android.utils

import android.content.ContentValues
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import com.au.module_android.Globals
import java.io.File

sealed class SaveFileType(val path: String) {
    object Video : SaveFileType(Globals.app.packageName)
    object Music : SaveFileType(Globals.app.packageName)
    object Image : SaveFileType(Globals.app.packageName)
    object Download : SaveFileType(Globals.app.packageName)
}

/**
 * 保存文件
 */
fun saveFileToPublicDirectory(
    file: File,
    type: SaveFileType,
    mimeType: String,
    displayName: String = file.name,
    deleteOldFile: Boolean = true,
    path: String = type.path,
    setContentValues: Function1<ContentValues, Unit>? = null
) {
    if (file.isDirectory) {
        return
    }
    saveByteArrayToPublicDirectory(
        file.readBytes(),
        type,
        mimeType,
        displayName,
        path,
        setContentValues
    )
    if (deleteOldFile && file.exists()) {
        file.delete()
    }
}

/**
 * 需要读取存储权限
 * 将文件写入公共数据库
 * 将文件保存到公共的媒体文件夹
 * 这里的filepath不是绝对路径，而是某个媒体文件夹下的子路径，和沙盒子文件夹类似
 * 这里的filename单纯的指文件名，不包含路径
 */
fun saveByteArrayToPublicDirectory(
    byte: ByteArray,
    type: SaveFileType,
    mimeType: String,
    displayName: String,
    path: String = type.path,
    setContentValues: Function1<ContentValues, Unit>? = null
): Uri? {
    val uri = insetFileToContentResolver(type, mimeType, displayName, path, setContentValues)
    if (uri != null) {
        Globals.app.contentResolver.openOutputStream(uri)?.use {
            it.write(byte)
        }
    }
    return uri
}

fun deleteFromContentResolver(uri: Uri) {
    Globals.app.contentResolver.delete(uri, null, null)
}

/**
 * 直接将字节数组写入，适合小文件
 */
fun insetFileToContentResolver(
    type: SaveFileType,
    mimeType: String,
    displayName: String,
    path: String = type.path,
    setContentValues: Function1<ContentValues, Unit>? = null
): Uri? {
    return ignoreError {
        //设置保存参数到ContentValues中
        val contentValues = ContentValues()
        //执行insert操作，向系统文件夹中添加文件
        //EXTERNAL_CONTENT_URI代表外部存储器，该值不变
        val contentResolver = Globals.app.contentResolver
        val saveUri = when (type) {
            SaveFileType.Video -> {
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
                    contentValues.put(MediaStore.Video.Media.RELATIVE_PATH, "Movies/${path}")
                } else {
                    contentValues.put(
                        MediaStore.Video.Media.DATA,
                        getOldSdkPath(displayName, path, "Movies")
                    )
                }
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI
            }
            SaveFileType.Image -> {
                contentValues.put(MediaStore.Images.Media.DISPLAY_NAME, displayName)
                contentValues.put(MediaStore.Images.Media.TITLE, displayName)
                contentValues.put(MediaStore.Images.Media.MIME_TYPE, mimeType)
                if (!isExternalStorageLegacy) {
                    contentValues.put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/${path}")
                } else {
                    contentValues.put(
                        MediaStore.Images.Media.DATA,
                        getOldSdkPath(displayName, path, "Pictures")
                    )
                }
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            }
            SaveFileType.Music -> {
                contentValues.put(MediaStore.Audio.Media.DISPLAY_NAME, displayName)
                contentValues.put(MediaStore.Audio.Media.TITLE, displayName)
                contentValues.put(MediaStore.Audio.Media.MIME_TYPE, mimeType)
                if (!isExternalStorageLegacy) {
                    contentValues.put(MediaStore.Audio.Media.RELATIVE_PATH, "Music/${path}")
                } else {
                    contentValues.put(
                        MediaStore.Audio.Media.DATA,
                        getOldSdkPath(displayName, path, "Music")
                    )
                }
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
            }
            SaveFileType.Download -> {
                contentValues.put(MediaStore.Downloads.DISPLAY_NAME, displayName)
                contentValues.put(MediaStore.Downloads.TITLE, displayName)
                contentValues.put(MediaStore.Downloads.MIME_TYPE, mimeType)
                if (!isExternalStorageLegacy) {
                    contentValues.put(MediaStore.Downloads.RELATIVE_PATH, "Download/${path}")
                    MediaStore.Downloads.EXTERNAL_CONTENT_URI
                } else {
                    contentValues.put(
                        MediaStore.Files.FileColumns.DATA,
                        getOldSdkPath(displayName, path, "Download")
                    )
                    MediaStore.Files.getContentUri("external")
                }
            }
        }
        setContentValues?.invoke(contentValues)
        return contentResolver.insert(saveUri, contentValues)
    }
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