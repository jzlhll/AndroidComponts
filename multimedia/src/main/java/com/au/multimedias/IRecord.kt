package com.au.multimedias

import android.content.Context
import android.os.Environment
import java.io.File
import java.io.IOException
import java.lang.StringBuilder

interface ISimpleRecord {
    fun start(context:Context)
    fun stop()
    val isRecording: Boolean
    val isPaused:Boolean
}

interface IRecord : ISimpleRecord {
    fun resume(file: String)
    fun pause()

    val supportResume:Boolean
}

/**
 * 组合一个名字路径文件。并确保路径可用。
 */
@Throws(IOException::class)
fun createExternalFileName(vararg pathAndNames:String) : String {
    val sb = StringBuilder(Environment.getExternalStorageDirectory().toString())
    for (p in pathAndNames) {
        sb.append(File.separator).append(p)
    }

    val last = File.separator + pathAndNames.last()
    val file = sb.toString()
    val path = File(sb.substring(0, file.length - last.length))
    if (!path.exists()) {
        if(!path.mkdirs()) throw IOException("Cannot create $path")
    }

    return sb.toString()
}

fun createRecord(file: String): IRecord {
    return MediaRecordAudio(file)
}