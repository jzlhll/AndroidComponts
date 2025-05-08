package com.au.module_android.utils

import android.os.Environment
import com.au.module_android.permissions.hasPermission
import kotlinx.coroutines.delay
import java.io.File
import java.io.IOException

fun generateUniqueFileName(directory: File, originalName: String): File {
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

/**
 * todo：使用MediaStore.createWriteRequest
 * https://baijiahao.baidu.com/s?id=1782274042874763632&wfr=spider&for=pc
 *
 * this is only for Q
 */
suspend fun exportFileToDownload(outputFileName: String, sourceFile: File): String {
    // 验证源文件是否存在
    if (!sourceFile.exists()) {
        return "error: Source file does not exist."
    }

    // 获取下载目录
    val has = hasPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
    logt { "has permission $has" }
    val directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
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
