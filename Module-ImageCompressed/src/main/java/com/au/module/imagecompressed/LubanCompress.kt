package com.au.module.imagecompressed

import android.content.Context
import android.text.TextUtils
import top.zibin.luban.Luban
import top.zibin.luban.OnNewCompressListener
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale

class LubanCompress(private val ignoreSizeKb:Int = 250) {
    /**
     * 结果的回调
     */
    var resultCallback: ((srcPath: String?, resultPath: String?)->Unit)? = null

    /**
     * loadSourceBlock 只需要做一个步骤，就是load(Uri，File，String, List<X>)函数
     */
    var loadSourceBlock:((Luban.Builder)->Unit)? = null

    private fun isUrlHasImage(url: String): Boolean {
        val lowUrl = url.lowercase()
        return (lowUrl.endsWith(".jpg")
                || lowUrl.endsWith(".jpeg")
                || lowUrl.endsWith(".png")
                || lowUrl.endsWith(".heic"))
    }

    private fun isHasHttp(path: String): Boolean {
        if (TextUtils.isEmpty(path)) {
            return false
        }
        return path.startsWith("http") || path.startsWith("https")
    }

    private fun getCreateFileName(prefix: String): String {
        val millis = System.currentTimeMillis()
        return prefix + SimpleDateFormat("yyyyMMddHHmmssSSS", Locale.US).format(millis)
    }

    fun clearCache(cxt:Context) {
        try {
            val cmpImagesPath = File(cxt.externalCacheDir?.absolutePath + "/luban_disk_cache")
            cmpImagesPath.listFiles()?.forEach {
                if (it.name.startsWith("CMP_")) {
                    it.delete()
                }
            }

        } catch (e:Exception) {
            e.printStackTrace()
        }
    }

    fun compress(context:Context) {
        // 1、调用Luban压缩
        val builder = Luban.with(context)
        loadSourceBlock?.invoke(builder)

        builder
            .ignoreBy(ignoreSizeKb) //250kb不做压缩
            .filter { path -> //过滤掉http图片；能支持的图片。
                isUrlHasImage(path) && !isHasHttp(path)
            }
            .setRenameListener { filePath ->
                val indexOf = filePath.lastIndexOf(".")
                val postfix = if (indexOf != -1) filePath.substring(indexOf) else ".jpg"
                getCreateFileName("CMP_") + postfix
            }
            .setCompressListener(object : OnNewCompressListener {
                override fun onStart() {}
                override fun onSuccess(source: String?, compressFile: File?) {
                    if (compressFile != null) {
                        resultCallback?.invoke(source, compressFile.absolutePath)
                    }
                }

                override fun onError(source: String?, e: Throwable?) {
                    resultCallback?.invoke(source, null)
                }
            }).launch()
    }
}