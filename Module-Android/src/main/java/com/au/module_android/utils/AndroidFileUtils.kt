package com.au.module_android.utils

import android.content.Context
import android.net.Uri
import android.os.Environment
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException


/**
 * 是否有外部存储。即我们是否可以往/sdcard/Android/data/xxxx/存放
 */
fun hasExternal() = Environment.MEDIA_MOUNTED == Environment.getExternalStorageState()

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

fun getFileMD5(filePath: String): String {
    return try {
        FileInputStream(filePath).use { fis ->
            val messageDigest = MessageDigest.getInstance("MD5")
            val buffer = ByteArray(4096) // 4KB 缓冲区
            var bytesRead: Int
            while (fis.read(buffer).also { bytesRead = it } != -1) {
                messageDigest.update(buffer, 0, bytesRead)
            }
            bytesToHex(messageDigest.digest())
        }
    } catch (e: Exception) {
        when (e) {
            is NoSuchAlgorithmException,
            is IOException -> e.printStackTrace()
        }
        ""
    }
}

// 更简洁的 bytesToHex 实现（Kotlin 风格）
private fun bytesToHex(bytes: ByteArray): String {
    return bytes.joinToString("") { "%02x".format(it) }
}