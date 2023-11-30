package com.au.jobstudy

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.au.jobstudy.bean.DataItem
import com.au.jobstudy.bean.Subject
import com.au.jobstudy.bean.nameToSubject
import com.au.jobstudy.home.ThisWeekUiData
import com.au.jobstudy.saves.ISave
import com.au.jobstudy.saves.ISaveViewModel
import com.au.jobstudy.saves.SaveImpl
import com.au.module_android.simplelivedata.BusLiveData
import com.au.module_android.utils.ALog
import kotlinx.coroutines.launch

/**
 * @author allan.jiang
 * @date :2023/11/27 15:36
 * @description:
 */
class GlobalDataViewModel : ViewModel(), ISaveViewModel {
    private val saved:ISave = SaveImpl()

    val busLiveData = BusLiveData()

    fun dataListToCompletedCount(list:List<DataItem>) : Array<Any> {
        var totalCount = 0
        val allCompletedSubjectCount = HashMap<Subject, Int>()
        list.forEach { dataItem->
            if (dataItem.complete) {
                val subj = nameToSubject(dataItem.subject)
                totalCount++
                if (allCompletedSubjectCount.containsKey(subj)) {
                    allCompletedSubjectCount[subj] = allCompletedSubjectCount[subj]!! + 1
                } else {
                    allCompletedSubjectCount[subj] = 1
                }
            }
        }

        //总计的星星个数
        val returnList = mutableListOf<Any>(ThisWeekUiData.ThisWeekLayoutData(totalCount))
        val keySet = allCompletedSubjectCount.keys.toList()
        val first3Set = HashSet<Subject>()
        val second3Set = HashSet<Subject>()

        val count = keySet.size
        var startCount = 0
        while (startCount < 3 && startCount < count) {
            first3Set.add(keySet[startCount])
            startCount++
        }
        while (startCount < count) {
            second3Set.add(keySet[startCount])
            startCount++
        }

        //两行3颗星星
        if (first3Set.size > 0) {
            val map = first3Set.map {
                Pair(it, allCompletedSubjectCount[it]!!)
            }
            returnList.add(ThisWeekUiData.ThisWeekEachLayoutData(map.toTypedArray()))
        }
        if (second3Set.size > 0) {
            val map = second3Set.map {
                Pair(it, allCompletedSubjectCount[it]!!)
            }
            returnList.add(ThisWeekUiData.ThisWeekEachLayoutData(map.toTypedArray()))
        }
        return returnList.toTypedArray()
    }

    override fun getWeekData(day: String, mask:String?) {
        viewModelScope.launch {
            val list = saved.getWeekData(day)
            ALog.d("get week data $list")
            busLiveData.publishSuccess(mask?:"getWeekData", list)
        }
    }

    override fun updateOneDay(item: DataItem, mask:String?) {
        viewModelScope.launch {
            val r = saved.updateOneDay(item)
            busLiveData.publishSuccess(mask?:"updateOneDay", r)
        }
    }

    override fun deleteOneDay(day: String, mask:String?) {
        viewModelScope.launch {
            val r = saved.getWeekData(day)
            busLiveData.publishSuccess(mask?:"deleteOneDay", r)
        }
    }

    override fun deleteOneWeek(day: String, mask:String?) {
        viewModelScope.launch {
            val r = saved.getWeekData(day)
            busLiveData.publishSuccess(mask?:"deleteOneWeek", r)
        }
    }

    override fun getDay(day: String, mask:String?) {
        viewModelScope.launch {
            val list = saved.getWeekData(day)
            ALog.d("getDay $day--< $list")
            busLiveData.publishSuccess(mask?:"getDay", list)
        }
    }

}