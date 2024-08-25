package com.au.jobstudy.check.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.au.jobstudy.check.bean.WorkEntity

@Dao
interface WorkDao {
    @Insert
    fun insert(beans:List<WorkEntity>) : LongArray

    @Update
    fun update(bean: WorkEntity)

    @Query("select * from work WHERE day = :day")
    fun queryADay(day:Int) : List<WorkEntity>

    @Query("select * from work WHERE weekStartDay = :weekStartDay")
    fun queryAWeek(weekStartDay:Int) : List<WorkEntity>

    @Query("select * from work WHERE weekStartDay = :weekStartDay AND day = 0")
    fun queryAWeekNotDayWork(weekStartDay:Int) : List<WorkEntity>
}