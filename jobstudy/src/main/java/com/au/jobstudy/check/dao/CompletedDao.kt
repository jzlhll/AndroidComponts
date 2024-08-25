package com.au.jobstudy.check.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.au.jobstudy.check.bean.CompletedEntity

@Dao
interface CompletedDao {
    @Insert
    fun insert(bean: CompletedEntity)
    @Update
    fun update(bean: CompletedEntity)

    @Query("select * from completed WHERE workDay = :day")
    fun queryCompletedByDay(day:Int) : List<CompletedEntity>
//
    @Query("select * from completed WHERE dayWorkId = :dayWorkId")
    fun queryCompletedByWorkId(dayWorkId:Long) : List<CompletedEntity>
//
    @Query("select * from completed WHERE dayWorkId IN (:dayWorkIds)")
    fun queryCompletedListByWorkIds(dayWorkIds:List<Long>) : List<CompletedEntity>

}