package com.au.jobstudy.check.bean

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "star")
data class StarEntity(
    @PrimaryKey val name:String,
    var dingDay:Int,
    var dingCount:Int,
    var starCount:Int
)