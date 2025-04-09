package com.au.module_android.utils

import android.os.Handler
import android.os.HandlerThread
import android.os.Process
import android.util.Log
import com.au.module_android.Globals
import java.io.BufferedWriter
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStreamWriter
import java.nio.charset.StandardCharsets
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

internal class FileItem(val fileName:String, val log: String, val stace: String?)
object FileLog {
    private val logFileCreateType = LogFileCreateType.OneFileEveryDay //必须放在前面

    enum class LogFileCreateType(val nameFmt:String) {
        OneFileEveryDay("%02d_%02d.log"),
        OneFileAnHour("%02d_%02d_%02d.log")
    }

    @Volatile
    private var _mHandler:Handler? = null

    val logHandler:Handler
        get() {
            if (_mHandler == null) {
                synchronized(FileLog::class.java) {
                    if (_mHandler == null) {
                        val handlerThread = HandlerThread("FileLog_thread")
                        handlerThread.start()
                        _mHandler = Handler(handlerThread.getLooper()).also {
                            it.postDelayed({
                                //简化逻辑。第一次。20秒后尝试清理一次即可。不做每次写入的时候，判断。
                                clearLog()
                            }, 20 * 1000)
                        }
                    }
                }
            }

            return _mHandler!!
        }

    private fun getCurrentFileName(): String {
        val c = Calendar.getInstance()
        return when (logFileCreateType) {
            LogFileCreateType.OneFileEveryDay
                -> String.format(Locale.US, LogFileCreateType.OneFileEveryDay.nameFmt, c[Calendar.MONTH] + 1, c[Calendar.DAY_OF_MONTH])
            LogFileCreateType.OneFileAnHour
                -> String.format(Locale.US, LogFileCreateType.OneFileEveryDay.nameFmt, c[Calendar.MONTH] + 1, c[Calendar.DAY_OF_MONTH], c[Calendar.HOUR])
        }
    }

    private fun getRootPath() = Globals.goodFilesDir.absolutePath + File.separator + "Log"
    val logDir by unsafeLazy { getRootPath() + File.separatorChar }

    /**
     * 暂时停止写入
     */
    var ignoreWrite = false

    private fun writeToDisk2(item: FileItem) {
        val dirPath = logDir
        if (dirPath.length <= 1) {
            return
        }
        var file = File(dirPath)
        if (!file.exists()) {
            file.mkdirs()
        }

        if (ignoreWrite) {
            return
        }

        file = File(logDir, item.fileName)

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
                        if (item.stace == null) {
                            out.write(item.log)
                        } else {
                            out.write(
                                """
                                $item.stace
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

    private val timestampFmt by unsafeLazy { SimpleDateFormat("MM-dd HH:mm:ss.ms", Locale.getDefault()) }

    /**时间戳转日期*/
    private fun longTimeToStr(time: Long?): String {
        if (time == null) {
            return ""
        }
        return timestampFmt.format(time).toString()
    }

    fun write(log: String, needStace: Boolean = false, throwable: Throwable? = null) {
        if (ignoreWrite) {
            return
        }

        val logTimeStr = longTimeToStr(System.currentTimeMillis())
        val writeStr = "$logTimeStr ${Process.myPid()} $log"
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
            logHandler.post {
                writeToDisk2(FileItem(getCurrentFileName(), writeStr, stace))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun clearLog() {
        var count = 0
        do {
            val file = File(getRootPath())
            if (!file.exists()) {
                break
            }
            val files = file.listFiles() ?: break
            for (f in files) {
                if (f.exists()) {
                    try {
                        val time = f.lastModified()
                        if (System.currentTimeMillis() - time > 15L * 3600 * 24 * 1000) { //N天前的日志删除。
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
        Log.d("FileLog", "clear old log file over! $count")
    }
}