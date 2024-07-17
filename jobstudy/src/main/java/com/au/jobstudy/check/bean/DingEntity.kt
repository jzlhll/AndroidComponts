package com.au.jobstudy.check.bean

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ding")
data class DingEntity(
    @PrimaryKey val name:String,
    val day:Int,
)