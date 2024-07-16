package com.au.jobstudy.check.bean

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "completed")
data class CompletedEntity(
    val dayWorkId:Int,
    val day:Int,
    val files:String?,
    @PrimaryKey(autoGenerate = true) val id : Int = 0
)