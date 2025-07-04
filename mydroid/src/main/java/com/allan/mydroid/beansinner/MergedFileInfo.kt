package com.allan.mydroid.beansinner

import androidx.annotation.Keep
import java.io.File

@Keep
data class MergedFileInfo(val file: File, val md5:String, val fileSizeInfo:String)