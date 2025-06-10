package com.allan.mydroid.beans

import androidx.annotation.Keep

/**
 * 转变成传输给前端的对象
 */
data class UriRealInfoHtml(@Keep val uriUuid:String, @Keep val name:String?, @Keep val fileSizeStr: String) {
    override fun toString(): String {
        return "uriUuid=$uriUuid, name=$name, fileSizeStr=$fileSizeStr"
    }
}