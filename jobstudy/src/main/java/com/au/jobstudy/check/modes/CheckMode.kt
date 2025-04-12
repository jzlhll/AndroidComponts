package com.au.jobstudy.check.modes

import androidx.annotation.IntRange
import androidx.annotation.Keep

@Keep
data class CheckMode(
    @IntRange(from = MediaType.TYPE_VIDEO.toLong(), to = MediaType.TYPE_BELIEVE.toLong()) val mediaType: Int,
    val desc:String,
    val max: Int = 0,
    val min:Int = 0)