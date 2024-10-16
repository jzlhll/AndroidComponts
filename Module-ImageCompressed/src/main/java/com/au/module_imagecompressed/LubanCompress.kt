package com.au.module_imagecompressed

import android.content.Context
import android.net.Uri
import android.text.TextUtils
import com.au.module_imagecompressed.CropCircleImageFragment.Companion.DIR_CROP
import com.au.module_android.Globals
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

    fun clearCache() {
        try {
            val cmpImagesPath = File(Globals.goodCacheDir.absolutePath + "/luban_disk_cache")
            cmpImagesPath.listFiles()?.forEach {
                it.delete()
            }
        } catch (e:Exception) {
            e.printStackTrace()
        }

        try {
            val cmpImagesPath = File(Globals.goodCacheDir.absolutePath + "/$DIR_CROP")
            cmpImagesPath.listFiles()?.forEach {
                it.delete()
            }
        } catch (e:Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 参考Luban.load(xxx)的类型
     * 包括String, Uri, File， List<String>, List<Uri>, List<File>
     */
    fun compress(context:Context, source:Any?) {
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
                if (source.isNotEmpty()) {
                    val item = source[0]
                    if (item is String || item is Uri || item is File) {
                        builder.load(source)
                    }
                }
            }
        }

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