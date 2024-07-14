package com.au.jobstudy.check

import com.au.jobstudy.utils.Dayer
import com.au.jobstudy.utils.WeekDateUtil
import com.au.jobstudy.check.bean.CompletedEntity
import com.au.jobstudy.check.bean.WorkEntity
import com.au.jobstudy.check.generator.IDbGenerator
import com.au.jobstudy.check.generator.SummerDbGenerator
import com.au.module_android.simplelivedata.SafeLiveData
import kotlinx.coroutines.delay

object CheckConsts {
    var currentDayer = Dayer()
        private set

    val generator : IDbGenerator = SummerDbGenerator()

    val curWeekWorks = SafeLiveData<List<WorkEntity>>()

    val lastWeekWorks = SafeLiveData<List<WorkEntity>>()

    val todayCompletedWorks = SafeLiveData<List<CompletedEntity>>()

    val yesterdayCompletedWorks = SafeLiveData<List<CompletedEntity>>()

    suspend fun whenEnterForeground() {
        val newDayer = Dayer()
        if (newDayer != currentDayer) {
            val dao = AppDatabase.db.getDao()
            val newWeekStartDay = WeekDateUtil.anyDayToWeekStartDay(newDayer.weekStartDay)
            if (newWeekStartDay == newDayer.weekStartDay) {
            //只是变了一周内的一天

            } else {
            //变周了

            }
            currentDayer = newDayer
        }

        delay(1)
    }
}