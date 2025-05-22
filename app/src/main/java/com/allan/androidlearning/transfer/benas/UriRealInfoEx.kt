package com.allan.androidlearning.transfer.benas

import android.net.Uri
import androidx.annotation.Keep
import com.au.module_android.Globals
import com.au.module_android.utilsmedia.UriRealInfo
import com.au.module_android.utilsmedia.formatBytes
import java.io.File
import java.util.UUID

data class UriRealInfoEx(val uri: Uri,
                         val name:String? = null,
                         val realPath:String? = null,
                         val relativePath:String? = null,
                         val fileSize:Long?,
                         val fileSizeStr:String) {
    val uriUuid:String = UUID.randomUUID().toString().replace("-", "")

    @Transient var isChecked: Boolean = true

    companion object {
        fun copyFrom(info: UriRealInfo) : UriRealInfoEx {
            val goodPath = info.realPath ?: info.relativePath
            val fileSize = if(goodPath != null) File(goodPath).length() else null
            val fileLen = if(goodPath != null) formatBytes(File(goodPath).length()) else Globals.getString(com.allan.androidlearning.R.string.unknown_size)
            return UriRealInfoEx(info.uri, info.name, info.realPath, info.relativePath, fileSize, fileLen)
        }

        fun copyFrom(info: UriRealInfoEx) : UriRealInfoEx {
            return UriRealInfoEx(info.uri, info.name, info.realPath, info.relativePath, info.fileSize, info.fileSizeStr)
        }
    }

    fun goodPath() = realPath ?: relativePath

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
data class UriRealInfoHtml(@Keep val uriUuid:String, @Keep val name:String?, @Keep val fileSizeStr: String)