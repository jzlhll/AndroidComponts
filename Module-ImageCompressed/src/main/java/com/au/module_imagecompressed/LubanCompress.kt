package com.au.module_imagecompressed

import android.content.Context
import android.net.Uri
import com.au.module_imagecompressed.util.isPicNeedCompress
import top.zibin.luban.Luban
import top.zibin.luban.OnNewCompressListener
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale

class LubanCompress {
    /**
     * 结果的回调。
     */
    private var mResultCallback: ((srcPath: String?, resultPath: String?, isSuc:Boolean)->Unit)? = null

    /**
     * luban的回调。不论是否需要处理都会回调。
     * 而且工作在主线程。
     */
    fun setResultCallback(cb:(srcPath: String?, resultPath: String?, isSuc:Boolean)->Unit) : LubanCompress {
        mResultCallback = cb
        return this
    }

    private fun getCreateFileName(prefix: String): String {
        val millis = System.currentTimeMillis()
        return prefix + SimpleDateFormat("yyyyMMddHHmmssSSS", Locale.US).format(millis)
    }

    /**
     * 参考Luban.load(xxx)的类型
     * 包括String, Uri, File， List<String>, List<Uri>, List<File>
     */
    fun compress(context: Context, source:Any, ignoreSizeKb:Int = 100) {
        // 1、调用Luban压缩
        val builder = Luban.with(context)
        when (source) {
            is String -> {
                builder.load(source)
            }

            is Uri -> {
                builder.load(source)
            }

            is File -> {
                builder.load(source)
            }
            is List<*> -> {
                if (source.isEmpty()) {
                    return
                }

                val componentType = source[0]!!.javaClass
                if (String::class.java.isAssignableFrom(componentType)
                    || Uri::class.java.isAssignableFrom(componentType)
                    || File::class.java.isAssignableFrom(componentType)) {
                    builder.load(source)
                }
            }
        }

        builder
            .ignoreBy(ignoreSizeKb) //250kb不做压缩
            //不再过滤。避免无法回调。
            .filter { path -> //过滤掉http图片；能支持的图片。
                isPicNeedCompress(path)
            }
            .setRenameListener { filePath ->
                val indexOf = filePath.lastIndexOf(".")
                val postfix = if (indexOf != -1) filePath.substring(indexOf) else ".jpg"
                getCreateFileName("compress_") + postfix
            }
            .setCompressListener(object : OnNewCompressListener {
                override fun onStart() {}
                override fun onSuccess(source: String?, compressFile: File?) {
                    if (compressFile != null) {
                        mResultCallback?.invoke(source, compressFile.absolutePath, true)
                    }
                }

                override fun onError(source: String?, e: Throwable?) {
                    mResultCallback?.invoke(source, null, false)
                }
            }).launch()
    }
}