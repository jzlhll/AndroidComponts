package com.au.jobstudy.check.modes

import androidx.annotation.IntRange

data class CheckMode(
    @IntRange(from = MediaType.TYPE_VIDEO.toLong(), to = MediaType.TYPE_PIC.toLong()) val mediaType: Int,
    val maxSec: Int,
    val minSec:Int)