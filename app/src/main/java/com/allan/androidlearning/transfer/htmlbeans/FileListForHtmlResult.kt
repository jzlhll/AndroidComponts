package com.allan.androidlearning.transfer.htmlbeans

import androidx.annotation.Keep
import com.allan.androidlearning.transfer.benas.UriRealInfoHtml

data class FileListForHtmlResult(@Keep val urlRealInfoHtmlList:List<UriRealInfoHtml>)