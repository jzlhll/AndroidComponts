package com.allan.androidlearning.transfer.benas

import androidx.annotation.Keep

@Keep
data class MyDroidModeResult(val myDroidMode:String, val ip:String?, val clientName:String)

data class FileListForHtmlResult(val urlRealInfoHtmlList:List<UriRealInfoHtml>)