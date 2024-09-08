package com.au.jobstudy.api.bean

import androidx.annotation.Keep
import com.au.jobstudy.check.modes.CheckMode

@Keep
data class JobItemBean(val subject:String,
                       val desc:String,
                       val checkModes: List<CheckMode>)