package com.au.jobstudy.completed

import com.au.jobstudy.check.bean.CompletedEntity
import com.au.jobstudy.check.bean.WorkEntity

interface ICompletedBean
data class CompletedBean(val workEntity: WorkEntity, var completedEntity:CompletedEntity?) : ICompletedBean
data class CompletedDateBean(val day:Int, val isWeek:Boolean) : ICompletedBean