package com.au.jobstudy.api

import com.au.jobstudy.api.bean.JobBean
import com.au.module_android.Globals
import com.au.module_android.json.fromJson
import com.au.module_android.utils.awaitOnIoThread
import com.au.module_android.utils.logd
import com.au.module_okhttp.OkhttpGlobal
import com.au.module_okhttp.creator.downloadFile
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths

object Api {
    suspend fun requestJobData(yearMonth:Int, day:Int) : JobBean? {
        val file = requestJob(yearMonth, day)
        return file?.let { fileToJobBean(it) }
    }

    private suspend fun requestJob(yearMonth:Int, day:Int) : File? {
        val dayStr = if (day < 10) {
            "0$day"
        } else {
            "$day"
        }
        val url = "https://gitee.com/allan001/JobStudyDispatcher/blob/master/jobs/$yearMonth/job$dayStr.txt"
        return OkhttpGlobal.okHttpClient().downloadFile(url, Globals.goodCacheDir.absolutePath, "job" + System.currentTimeMillis() + ".txt")
    }

    private val parseStartText = """
<textarea name="blob_raw" id="blob_raw" style="display:none;">
    """.trimIndent()

    private val parseEndText = """
</textarea>
    """.trimIndent()

    private fun xmlStrConvert(orig:String) : String {
        return orig.replace("&#x000A;", "\n") //换行
            .replace("&#x0020;", " ") //空格
            .replace("&#x0009;", "") //tab
            .replace("&#x000D;", "") //回车
    }

    private suspend fun fileToJobBean(file:File) : JobBean? {
        val path = Paths.get(file.absolutePath)
        return awaitOnIoThread {
            val bs = Files.readAllBytes(path)
            val str = String(bs)
            val startIndex = str.indexOf(parseStartText)
            val parsedText = str.substring(startIndex + parseStartText.length, str.indexOf(parseEndText, startIndex))
            val parsedXmlText = xmlStrConvert(parsedText)
            logd { "parsed Text $parsedXmlText" }
            parsedXmlText.fromJson<JobBean>()
        }
    }
}