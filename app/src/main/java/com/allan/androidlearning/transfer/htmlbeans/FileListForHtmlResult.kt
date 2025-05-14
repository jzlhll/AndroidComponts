package com.allan.androidlearning.transfer.htmlbeans

import androidx.annotation.Keep
import com.allan.androidlearning.transfer.benas.UriRealInfoHtml

@Keep
data class FileListForHtmlResult(val urlRealInfoHtmlList:List<UriRealInfoHtml>)