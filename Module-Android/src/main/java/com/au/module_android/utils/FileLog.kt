package com.au.module_android.utils

import android.os.Handler
import android.os.HandlerThread
import android.os.Process
import android.util.Log
import androidx.annotation.WorkerThread
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

internal class FileItem(val fileName:String, val log: String)
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

    private val memCachedFileItemsLock = Any()
    private const val CAPACITY_OF_FIXED_FILE_ITEMS = 1000
    private val memCachedFileItems by unsafeLazy {
        FixedSizeLinkedList<FileItem>(CAPACITY_OF_FIXED_FILE_ITEMS)
    }

    private var _ignoreWrite = false

    /**
     * 暂时停止写入
     */
    var ignoreWrite: Boolean
        get() = _ignoreWrite
        set(value) {
            //证明是开启。
            if (_ignoreWrite && !value) {
                writeMemCachedLogs()
            }

            _ignoreWrite = value
        }

    private fun writeMemCachedLogs() {
        val sb = StringBuilder()
        synchronized(memCachedFileItemsLock) {
            while (memCachedFileItems.isNotEmpty()) {
                val item = memCachedFileItems.removeFirst()
                sb.append(item.log).append("\n")
            }
        }
        val str = sb.toString()
        if (str.isNotEmpty()) {
            logHandler.post {
                writeToDisk(FileItem(getCurrentFileName(), str))
            }
        }
    }

    @WorkerThread
    private fun writeToDisk(item: FileItem) {
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
                        out.write(item.log)
                        out.write("\n")
                        out.flush()
                    }
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private val timestampFmt by unsafeLazy { SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault()) }

    data class LogParserInfo(val time:String, val threadProcess:String, val level:String, val tag:String, val msg:String)

    fun logParser(s:String) : LogParserInfo? {
        return ignoreError {
            val ss = s.split(" ")
            val tag = ss[3]
            val cutTag = tag.substring(0, tag.length - 1)
            val index = s.indexOf(tag)
            LogParserInfo(ss[0], ss[1], ss[2], cutTag, s.substring(index + tag.length + 1))
        }
    }

    /**时间戳转日期*/
    private fun longTimeToStr(time: Long): String {
        return timestampFmt.format(time).toString()
    }

    fun write(log: String?) {
        val logTimeStr = longTimeToStr(System.currentTimeMillis())
        val writeStr = "$logTimeStr ${Process.myPid()}-${Thread.currentThread().id} $log"
        //排队写日志
        logHandler.post {
            val fileItem = FileItem(getCurrentFileName(), writeStr)
            if (ignoreWrite) {
                synchronized(memCachedFileItemsLock) {
                    memCachedFileItems.add(fileItem)
                }
            } else {
                writeToDisk(fileItem)
            }
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