package com.au.jobstudy.check

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.au.jobstudy.check.bean.CompletedEntity
import com.au.jobstudy.check.bean.WorkEntity

@Dao
interface CheckDao {
    @Insert
    fun insert(bean:WorkEntity)
    @Insert
    fun insert(beans:List<WorkEntity>)

    @Update
    fun update(bean:WorkEntity)

    @Query("select * from work WHERE day = :day")
    fun queryADay(day:Int) : List<WorkEntity>

    @Query("select * from work WHERE weekStartDay = :weekStartDay")
    fun queryAWeek(weekStartDay:Int) : List<WorkEntity>

    @Query("select * from completed WHERE dayWorkId = :dayWorkId")
    fun queryCompleted(dayWorkId:Int) : List<CompletedEntity>

    @Query("select * from completed WHERE dayWorkId IN (:dayWorkIds)")
    fun queryCompletedList(dayWorkIds:List<Int>) : List<CompletedEntity>

}