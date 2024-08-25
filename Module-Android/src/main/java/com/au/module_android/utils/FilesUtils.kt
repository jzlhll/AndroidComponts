package com.au.module_android.utils

import java.io.File


/**
 * 删除文件
 */
fun File?.deleteFile(): Boolean {
    this ?: return false
    return if (this.isFile && this.exists()) {
        this.delete()
    } else {
        false
    }
}

/**
 * 删除文件夹
 */
fun File?.deleteFileDir(): Boolean {
    this ?: return false
    return if (this.isDirectory && this.exists()) {
        this.listFiles()?.forEach {
            if (it.isFile) {
                it.deleteFile()
            } else {
                it.deleteFileDir()
            }
        }
        this.delete()
    } else {
        false
    }
}

/**
 * 删除文件或者文件夹
 */
fun File?.deleteAll(): Boolean {
    this ?: return false
    return when {
        isFile -> deleteFile()
        isDirectory -> deleteFileDir()
        else -> false
    }
}


fun File?.getDirSize(): Long {
    this ?: return 0
    if (this.isFile) {
        return this.length()
    }
    var size = 0L
    if (this.isDirectory) {
        this.listFiles()?.forEach {
            size += it.getDirSize()
        }
    }
    return size
}