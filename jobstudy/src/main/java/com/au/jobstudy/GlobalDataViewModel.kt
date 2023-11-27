package com.au.jobstudy

import androidx.lifecycle.ViewModel
import com.au.jobstudy.bean.DataItem
import com.au.jobstudy.saves.ISave
import com.au.jobstudy.saves.SaveImpl

/**
 * @author allan.jiang
 * @date :2023/11/27 15:36
 * @description:
 */
class GlobalDataViewModel : ViewModel(), ISave {
    val saved = SaveImpl()

    override suspend fun getWeekData(day: String): List<DataItem> {
        return saved.getDay(day)
    }

    override suspend fun updateOneDay(item: DataItem): Boolean {
        return saved.updateOneDay(item)
    }

    override suspend fun deleteOneDay(day: String): Boolean {
        return saved.deleteOneDay(day)
    }

    override suspend fun deleteOneWeek(day: String): Boolean {
        return saved.deleteOneWeek(day)
    }

    override suspend fun getDay(day: String): List<DataItem> {
        return saved.getDay(day)
    }
}