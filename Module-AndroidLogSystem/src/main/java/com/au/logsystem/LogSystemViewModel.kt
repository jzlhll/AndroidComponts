package com.au.logsystem

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.au.module_android.Globals
import com.au.module_android.simplelivedata.NoStickLiveData
import com.au.module_android.utils.FileLog
import com.au.module_android.utils.launchOnThread
import com.au.module_android.utils.logdNoFile
import kotlinx.coroutines.delay
import okio.IOException
import java.io.File

data class CompressProgressInfo(val info: String, val isFinished: Boolean, val progress:Int, val file: File? = null)

class LogSystemViewModel : ViewModel() {
    /**
     * 外层List是按天分开。List<File>是一天内的文件
     */
    val scannedList = NoStickLiveData<List<LogBean>>()

    /**
     * info to 是否结束
     */
    val compressProgress = NoStickLiveData<CompressProgressInfo>()

    fun scanLogs() {
        viewModelScope.launchOnThread {
            try {
                val dir = File(FileLog.logDir)
                if (dir.exists() && dir.isDirectory) {
                    val sortedList = scan(dir)
                    val cvtList = sortedList?.let { convertMapToList(it) }
                    scannedList.setValueSafe(cvtList ?: listOf())
                } else {
                    scannedList.setValueSafe(listOf())
                }
            } catch (_ : Exception) {
                scannedList.setValueSafe(listOf())
            }
        }
    }

    private fun convertMapToList(map: Map<FileSorter.Group, List<File>>): List<LogBean> {
        val list = mutableListOf<LogBean>()
        map.forEach { kv->
            val day = FileSorter.groupToName(kv.key)
            list.add(generateHead(day))

            kv.value.forEach { file->
                list.add(
                    generateNormal(
                        file.name,
                        ZipUtil.Companion.formatSize(file.length()),
                        file
                    )
                )
            }
        }
        return list
    }

    private fun scan(dir: File): Map<FileSorter.Group, List<File>>? {
        val files = HashSet<File>()
        ZipUtil.getAllFilesInDir(files, dir)
        val sortedList = FileSorter().groupFiles(files)
        return sortedList
    }

    fun compressAndShare(selectedData:List<File?>?) {
        if (selectedData == null) {
            compressProgress.setValueSafe(CompressProgressInfo("文件选择失败！", false, 0))
            return
        }

        val nonNullData = ArrayList<File>()
        for (file in selectedData) {
            if (file != null) {
                nonNullData.add(file)
            }
        }

        //使用全局scope避免失败
        Globals.mainScope.launchOnThread {
            logdNoFile { "prepared file start..." }
            val info = StringBuilder("1. 开始压缩...")

            compressProgress.setValueSafe(CompressProgressInfo(info.toString(), false, 20))

            var zip: File?
            try {
                zip = ZipUtil.compressFilesToZip(nonNullData, Globals.goodCacheDir.absolutePath + "/logShare")
            } catch (e: IOException) {
                e.printStackTrace()
                zip = null
            }

            delay(500)
            val length = zip?.length() ?: -1
            if (zip != null && length > 0) {
                info.append("\n2. 压缩完成(${ZipUtil.formatSize(length)})!")
                compressProgress.setValueSafe(CompressProgressInfo(info.toString(), false, 100, zip))
            } else {
                info.append("\n2. 压缩失败!")
                compressProgress.setValueSafe(CompressProgressInfo(info.toString(), false, 0))
            }

        }
    }
}