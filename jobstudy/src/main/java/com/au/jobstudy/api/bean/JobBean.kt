package com.au.jobstudy.api.bean

import androidx.annotation.Keep

@Keep
data class JobBean(val title:String, val jobItems:List<JobItemBean>)