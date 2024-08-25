package com.au.module_android.utils

import android.content.Context
import android.net.Uri
import android.os.Environment
import androidx.core.content.FileProvider
import java.io.File
import java.lang.Exception

/**
 * 是否有外部存储。即我们是否可以往/sdcard/Android/data/xxxx/存放
 */
fun hasExternal() = Environment.MEDIA_MOUNTED == Environment.getExternalStorageState()

fun cacheDir(context:Context) = if(hasExternal()) context.externalCacheDir else context.cacheDir

fun filesDir(context: Context, type:String? = null) : File? {
    if (hasExternal()) {
        return context.getExternalFilesDir(type)
    }
    val dir = if (type.isNullOrEmpty()) {
        context.filesDir
    } else {
        val abPath = context.filesDir.absolutePath
        val subTypePath = abPath + (if (!abPath.endsWith("/")) "/$type" else "$type")
        File(subTypePath)
    }
    dir.mkdirs()
    return dir
}

/**
 * 创建一个图片的临时的uri。存在的话，就会删除再新建给出。
 * dir就是目录；fileName就是文件名；extension后缀名（记得自行带点，比如.png）
 */
fun getPictureFileUri(context: Context, dir:String, fileName:String, extension:String) : Uri? {
    return getPictureFileUri(context, File(dir), fileName, extension)
}

/**
 * 创建一个图片的临时的uri。存在的话，就会删除再新建给出。
 * dir就是目录；fileName就是文件名；extension后缀名（记得自行带点，比如.png）
 */
fun getPictureFileUri(context: Context, dir:File, fileName:String, extension:String) : Uri? {
    val photoFile = File.createTempFile(fileName, extension, dir).apply {
        createNewFile()
        deleteOnExit()
    }
    val appPkgName = context.applicationInfo.packageName //因为我这里配置的androidmanifest。
    try {
        return FileProvider.getUriForFile(context, "$appPkgName.provider", photoFile)
    } catch (e:Exception) {e.printStackTrace()}
    return null
}