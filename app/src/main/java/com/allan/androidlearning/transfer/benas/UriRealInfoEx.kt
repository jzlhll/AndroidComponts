package com.allan.androidlearning.transfer.benas

import android.net.Uri
import com.au.module_android.utilsmedia.UriRealInfo
import com.au.module_android.utilsmedia.formatBytes
import java.io.File

data class UriRealInfoEx(val uri: Uri, val name:String? = null,
                         val realPath:String? = null, val relativePath:String? = null,
                         val fileSizeStr:String,
                         var isChecked: Boolean = false) {
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
}