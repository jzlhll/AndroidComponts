package com.au.module_android.utils

import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import com.au.module_android.Globals
import java.io.*
import java.nio.charset.StandardCharsets
import java.text.SimpleDateFormat
import java.util.*

internal class FileItem(val filePath: String?, val log: String)
object FileLog {
    private val timeFmt = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())

    private fun logRoot() = Globals.app.getExternalFilesDir(null)?.absolutePath + File.separator + "Log"

    ///更新
/*    fun updateDay() {
        currentFilePath = calDayInner()
    }*/

    private val logFileCreateType = LogFileCreateType.OneFileEveryDay //必须放在前面
//    private var currentFilePath: String? = null
    private fun getCurrentFilePath(): String? {
/*        if (currentFilePath == null) {
            currentFilePath = calDayInner()
        }*/
        return calDayInner()
    }

    private fun calDayInner(): String {
        val c = Calendar.getInstance()
        var path = logRoot() + File.separatorChar
        if (logFileCreateType == LogFileCreateType.OneFileEveryDay) { //把这个放在最前面吧。
            path += String.format("%02d", c[Calendar.MONTH] + 1) + "_" + String.format(
                "%02d", c[Calendar.DAY_OF_MONTH]
            ) + ".log"
        } else if (logFileCreateType == LogFileCreateType.OneFileAnHour) {
            path += String.format(
                "%02d", c[Calendar.MONTH] + 1
            ) + "_" + String.format(
                "%02d", c[Calendar.DAY_OF_MONTH]
            ) + "_" + String.format("%02d", c[Calendar.HOUR]) + ".log"
        }
        return path
    }

    private fun writeToDisk2(item: FileItem?, stace: String?) {
        if (item?.filePath == null) {
            return
        }
        val lastIndex = item.filePath.lastIndexOf("/")
        if (lastIndex == -1) {
            return
        }
        val dirPath = item.filePath.substring(0, lastIndex)
        if (dirPath.length <= 1) {
            return
        }
        var file = File(dirPath)
        if (!file.exists()) {
            file.mkdirs()
        }
        file = File(item.filePath)
        if (!file.exists()) {
            try {
                file.createNewFile()
            } catch (e: IOException) {
                e.printStackTrace()
                return
            }
        }
        try {
            FileOutputStream(file, true).use { fis ->
                OutputStreamWriter(fis, StandardCharsets.UTF_8).use { osw ->
                    BufferedWriter(osw).use { out ->
                        if (stace == null) {
                            out.write(item.log)
                        } else {
                            out.write(
                                """
                                $stace
                                ${item.log}
                                """.trimIndent()
                            )
                        }
                        out.write("\n")
                        out.flush()
                    }
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun longTimeToStr(time: Long?): String {
        if (time == null) {
            return ""
        }
        return timeFmt.format(time).toString()
    }

    fun write(log: String, needStace: Boolean = false, throwable: Throwable? = null) {
        val logTimeStr = longTimeToStr(System.currentTimeMillis())
        val writeStr = "$logTimeStr>>$log"
        val stace: String? = if (needStace) {
            var exception = throwable
            if (exception == null){
                exception = Exception()
            }
            val sb = StringBuilder()
            sb.append(exception.message).append("\n").append(exception.cause).append("\n")
            for (element in exception.stackTrace) {
                sb.append(element.toString()).append(System.lineSeparator())
            }
            sb.toString()
        } else {
            null
        }
        try {
            //排队写日志
            HolderHandler.mHandler!!.post {
                val curFile = getCurrentFilePath()
                val item = FileItem(curFile, writeStr)
                val cur = System.currentTimeMillis()
                if (cur - lastClearTime > CLEAR_DELTA_TIME) {
                    lastClearTime = cur
                    clearLog()
                }
                writeToDisk2(item, stace)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private const val CLEAR_LOG_TIME = 15L * 3600 * 24 * 1000 //N天前的日志删除。
    private const val CLEAR_DELTA_TIME = 5L * 3600 * 24 * 1000 //M天清理一次。
    private var lastClearTime = 0L
    private fun clearLog() {
        var count = 0
        do {
            val file = File(logRoot())
            if (!file.exists()) {
                break
            }
            val files = file.listFiles() ?: break
            for (f in files) {
                if (f.exists()) {
                    try {
                        val time = f.lastModified()
                        if (System.currentTimeMillis() - time > CLEAR_LOG_TIME) {
                            if (f.delete()) {
                                count++
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        } while (false)
        if (count > 0) {
            Log.e("FileLog", "clear old log file over! $count")
        }
    }

    private object HolderHandler {
        @Volatile
        var mHandler: Handler? = null

        init {
            val handlerThread = HandlerThread("FileLog_thread")
            handlerThread.start()
            mHandler = Handler(handlerThread.getLooper())
        }
    }

    enum class LogFileCreateType {
        OneFileEveryDay, OneFileAnHour
    }
}