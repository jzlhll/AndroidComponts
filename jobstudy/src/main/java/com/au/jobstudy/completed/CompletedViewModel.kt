package com.au.jobstudy.completed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.au.jobstudy.check.AppDatabase
import com.au.jobstudy.check.bean.CompletedEntity
import com.au.jobstudy.utils.WeekDateUtil
import com.au.module_android.simplelivedata.SafeLiveData
import com.au.module_android.utils.launchOnThread

class CompletedViewModel : ViewModel() {
    /**
     * 日期 to 任务
     */
    val completedBeans = SafeLiveData<ArrayList<ICompletedBean>>()

    fun fetchWeek(weekStartDays:IntArray) {
        viewModelScope.launchOnThread {
            val list = ArrayList<ICompletedBean>()

            AppDatabase.db.runInTransaction {
                weekStartDays.forEach { weekStartDay->
                    val works = AppDatabase.db.getWorkDao().queryAWeekNotDayWork(weekStartDay)
                    if (works.isNotEmpty()) {
                        list.add(CompletedDateBean(weekStartDay, true))
                    }
                    val completed = AppDatabase.db.getCompletedDao().queryCompletedListByWorkIds(works.map { it.id })
                    works.forEach { work->
                        list.add(CompletedBean(work, completed.find { it.dayWorkId == work.id }))
                    }
                }

            }

            completedBeans.setValueSafe(list)
        }
    }

    fun fetch(days:IntArray) {
        viewModelScope.launchOnThread {
            val list = ArrayList<ICompletedBean>()

            AppDatabase.db.runInTransaction {
                for (day in days) {
                    val works = AppDatabase.db.getWorkDao().queryADay(day)
                    if (works.isNotEmpty()) {
                        list.add(CompletedDateBean(day, false))
                    }
                    val completed = AppDatabase.db.getCompletedDao().queryCompletedListByWorkIds(works.map { it.id })
                    works.forEach { work->
                        list.add(CompletedBean(work, completed.find { it.dayWorkId == work.id }))
                    }
                }
            }

            completedBeans.setValueSafe(list)
        }
    }
}