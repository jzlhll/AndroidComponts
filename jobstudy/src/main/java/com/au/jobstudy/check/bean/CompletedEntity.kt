package com.au.jobstudy.check.bean

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.au.jobstudy.check.modes.CheckMode

@Entity(tableName = "completed")
data class CompletedEntity(
    val dayWorkId:Int,
    val day:Int,
    @Embedded val checkMode:CheckMode,
    @PrimaryKey(autoGenerate = true) val id : Int = 0
)