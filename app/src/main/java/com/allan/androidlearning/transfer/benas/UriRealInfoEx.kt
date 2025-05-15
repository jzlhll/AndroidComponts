package com.allan.androidlearning.transfer.benas

import android.net.Uri
import androidx.annotation.Keep
import com.au.module_android.utilsmedia.UriRealInfo
import com.au.module_android.utilsmedia.formatBytes
import java.io.File
import java.util.UUID

@Keep
data class UriRealInfoEx(val uri: Uri,
                         val name:String? = null,
                         val realPath:String? = null,
                         val relativePath:String? = null,
                         val fileSizeStr:String,
                         @Transient var isChecked: Boolean = false) {
    val uriUuid:String = UUID.randomUUID().toString().replace("-", "")

    companion object {
        fun copyFrom(info: UriRealInfo, isChecked: Boolean) : UriRealInfoEx {
            val goodPath = info.realPath ?: info.relativePath
            val fileLen = if(goodPath != null) formatBytes(File(goodPath).length()) else "大小未知"
            return UriRealInfoEx(info.uri, info.name, info.realPath, info.relativePath, fileLen, isChecked)
        }

        fun copyFrom(info: UriRealInfoEx) : UriRealInfoEx {
            return UriRealInfoEx(info.uri, info.name, info.realPath, info.relativePath, info.fileSizeStr, info.isChecked)
        }
    }

    fun goodName() : String? {
        val n = realPath ?: (relativePath ?: name)
        return n?.substring(n.lastIndexOf("/") + 1)
    }

    fun copyToHtml() : UriRealInfoHtml {
        return UriRealInfoHtml(uriUuid, goodName(), fileSizeStr)
    }
}

/**
 * 转变成传输给前端的对象
 */
@Keep
data class UriRealInfoHtml(val uriUuid:String, val name:String?, val fileSizeStr: String)