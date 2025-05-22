package com.allan.androidlearning.transfer

import com.allan.androidlearning.transfer.benas.MergedFileInfo
import com.au.module_android.utils.getFileMD5
import com.au.module_cached.AppDataStore
import kotlinx.coroutines.delay
import java.io.File
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class MyDroidMess {
    private val keyMyDroidExportHistory = "my_droid_export_history_list"

    fun formatSize(bytes: Long): String {
        val units = listOf("B", "KB", "MB", "GB")
        var size = bytes.toDouble()
        var unitIndex = 0

        while (size >= 1024 && unitIndex < units.size - 1) {
            size /= 1024
            unitIndex++
        }
        return "%.2f %s".format(size, units[unitIndex])
    }

    suspend fun loadFileList() : List<MergedFileInfo>{
        delay(0)
        val nanoMergedDir = File(nanoTempCacheMergedDir())
        val fileList = ArrayList<MergedFileInfo>()
        if (nanoMergedDir.exists()) {
            nanoMergedDir.listFiles()?.forEach {
                fileList.add(MergedFileInfo(it, getFileMD5(it.absolutePath), formatSize(it.length())))
            }
        }
        fileList.sortByDescending { it.file.lastModified() }
        return fileList
    }

    suspend fun loadExportHistory() : String {
        return AppDataStore.read(keyMyDroidExportHistory, "")
    }

    suspend fun writeNewExportHistory(info:String) {
        val old = loadExportHistory()
        val splits = old.split("\n")
        val fixOld = if (splits.size > 100) {
            val cutList = splits.subList(0, 80)
            cutList.joinToString("\n")
        } else {
            old
        }

        // 获取当前时间戳
        val currentTimeMillis = System.currentTimeMillis()
        // 定义时间格式（例如：2023年10月05日 14:30）
        val formatter = DateTimeFormatter
            .ofPattern("yyyyMMdd HH:mm")
            .withZone(ZoneId.systemDefault()) // 使用系统默认时区
        // 格式化为字符串
        val formattedTime = formatter.format(Instant.ofEpochMilli(currentTimeMillis))

        AppDataStore.save(keyMyDroidExportHistory, "($formattedTime) $info\n\n$fixOld")
    }
}