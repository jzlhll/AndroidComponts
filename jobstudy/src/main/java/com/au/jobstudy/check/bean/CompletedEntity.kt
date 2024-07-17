package com.au.jobstudy.check.bean

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "completed")
data class CompletedEntity(
    val dayWorkId:Long,
    val workDay:Int,
    val workWeekStartDay:Int,
    val files:String?,
    @PrimaryKey(autoGenerate = true) var id : Long
)