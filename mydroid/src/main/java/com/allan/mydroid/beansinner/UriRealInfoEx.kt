package com.allan.mydroid.beansinner

import android.net.Uri
import androidx.annotation.Keep
import com.allan.mydroid.R
import com.allan.mydroid.utils.JsonUriAdapter
import com.au.module_android.Globals
import com.au.module_android.utilsmedia.UriRealInfo
import com.au.module_android.utilsmedia.formatBytes
import com.google.gson.annotations.JsonAdapter
import java.io.File
import java.util.UUID

@Keep
data class UriRealInfoEx(
                         val uriUuid:String,
                         @JsonAdapter(JsonUriAdapter::class)
                         val uri: Uri,
                         val name:String? = null,
                         val realPath:String? = null,
                         val relativePath:String? = null,
                         val fileSize:Long?,
                         val fileSizeStr:String) {

    @Transient var isChecked: Boolean = true

    companion object {
        fun copyFrom(info: UriRealInfo) : UriRealInfoEx {
            val goodPath = info.realPath ?: info.relativePath
            val fileSize = if(goodPath != null) File(goodPath).length() else null
            val fileLen = if(goodPath != null) formatBytes(File(goodPath).length()) else Globals.getString(R.string.unknown_size)
            val uriUuid = UUID.randomUUID().toString().replace("-", "")
            return UriRealInfoEx(uriUuid, info.uri, info.name, info.realPath, info.relativePath, fileSize, fileLen)
        }

        fun copyFrom(info: UriRealInfoEx) : UriRealInfoEx {
            val uriUuid = UUID.randomUUID().toString().replace("-", "")
            return UriRealInfoEx(uriUuid, info.uri, info.name, info.realPath, info.relativePath, info.fileSize, info.fileSizeStr)
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