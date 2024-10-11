package com.au.module.imagecompressed

import android.content.Context
import android.text.TextUtils
import top.zibin.luban.Luban
import top.zibin.luban.OnNewCompressListener
import java.io.File
import java.text.SimpleDateFormat

class LubanCompress {
    interface OnKeyValueResultCallbackListener {
        /**
         * @param srcPath
         * @param resultPath
         */
        fun onCallback(srcPath: String?, resultPath: String?)
    }

    fun isUrlHasImage(url: String): Boolean {
        val lowUrl = url.lowercase()
        return (lowUrl.endsWith(".jpg")
                || lowUrl.endsWith(".jpeg")
                || lowUrl.endsWith(".png")
                || lowUrl.endsWith(".heic"))
    }

    fun isHasHttp(path: String): Boolean {
        if (TextUtils.isEmpty(path)) {
            return false
        }
        return path.startsWith("http") || path.startsWith("https")
    }

    private val SF = SimpleDateFormat("yyyyMMddHHmmssSSS")

    fun getCreateFileName(prefix: String): String {
        val millis = System.currentTimeMillis()
        return prefix + SF.format(millis)
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

    /**
     * loadSourceBlock 只需要做一个步骤，就是load()函数
     * 支持Uri，File，String以及他们的List
     */
    fun compress(context:Context,
                 loadSourceBlock:(Luban.Builder)->Unit,
                 call: OnKeyValueResultCallbackListener) {
        // 1、调用Luban压缩
        val builder = Luban.with(context)
        loadSourceBlock(builder)

        builder
            .ignoreBy(250) //250kb不做压缩
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
                        call.onCallback(source, compressFile.absolutePath)
                    }
                }

                override fun onError(source: String?, e: Throwable?) {
                    call.onCallback(source, null)
                }
            }).launch()
    }
}