package com.au.jobstudy.check.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.au.jobstudy.check.bean.DingEntity

@Dao
interface DingDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(bean: DingEntity)

    @Query("select * from ding WHERE name = :name")
    fun queryDingByName(name:String) : List<DingEntity>

    @Query("select * from ding WHERE day = :day")
    fun queryDingsByDay(day:Int) : List<DingEntity>
}