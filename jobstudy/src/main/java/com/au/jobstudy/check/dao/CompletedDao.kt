package com.au.jobstudy.check.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.au.jobstudy.check.bean.CompletedEntity

@Dao
interface CompletedDao {
    @Insert
    fun insert(bean: CompletedEntity)

    @Query("select * from completed WHERE day = :day")
    fun queryCompletedByDay(day:Int) : List<CompletedEntity>
//
//    @Query("select * from completed WHERE dayWorkId = :dayWorkId")
//    fun queryCompletedByWorkId(dayWorkId:Int) : List<CompletedEntity>
//
    @Query("select * from completed WHERE dayWorkId IN (:dayWorkIds)")
    fun queryCompletedListByWorkIds(dayWorkIds:List<Int>) : List<CompletedEntity>

}