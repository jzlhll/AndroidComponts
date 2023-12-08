package com.au.jobstudy

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.au.jobstudy.bean.DataItem
import com.au.jobstudy.bean.Subject
import com.au.jobstudy.bean.nameToSubject
import com.au.jobstudy.bean.subjectToColorId
import com.au.jobstudy.home.HomeRcvBean
import com.au.jobstudy.home.HomeRcvItemBean
import com.au.jobstudy.home.HomeRcvTitleBean
import com.au.jobstudy.home.ThisWeekUiData
import com.au.jobstudy.saves.ISave
import com.au.jobstudy.saves.ISaveViewModel
import com.au.jobstudy.saves.SaveImpl
import com.au.module_android.simplelivedata.Bus
import com.au.module_android.simplelivedata.BusLiveData
import com.au.module_android.utils.ALog
import com.au.module_android.utils.asOrNull
import kotlinx.coroutines.launch
import java.util.Collections

/**
 * @author allan.jiang
 * @date :2023/11/27 15:36
 * @description:
 */
class GlobalDataViewModel : ViewModel(), ISaveViewModel {
    private val saved:ISave = SaveImpl()

    val busLiveData = BusLiveData()

    fun isBusGetTodayAndYesterday(bus:Bus) : Boolean {
        val shouldWork = arrayOf(0)
        bus.foreach { key, _ ->
            if (key == "getDay-Yesterday" || key == "getDay-Today") {
                shouldWork[0] = shouldWork[0] + 1
            }
            false
        }
        return shouldWork[0] == 2
    }

    fun busToAdapterData(bus:Bus) : List<HomeRcvBean>{
        val data:ArrayList<HomeRcvBean> = ArrayList()
        bus.foreach { key, content ->
            when (key) {
                "getDay-Yesterday" -> {
                    content.real.asOrNull<List<DataItem>>()?.let { list->
                        if (list.isNotEmpty()) {
                            data.add(HomeRcvTitleBean(1, "昨天还没有完成的任务:"))
                            data.addAll(list.map { HomeRcvItemBean(2, subjectToColorId(it.subject), it.subject, it.desc) })
                        }
                    }
                    true
                }
                "getDay-Today" -> {
                    content.real.asOrNull<List<DataItem>>()?.let { list->
                        if (list.isNotEmpty()) {
                            data.add(HomeRcvTitleBean(1, "今天的任务:"))
                            data.addAll(list.map { HomeRcvItemBean(2, subjectToColorId(it.subject), it.subject, it.desc) })
                        }
                    }
                    true
                }
                else -> {
                    false
                }
            }
        }

        return data
    }

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

    override fun getWeekData(day: String, notExistGenerate: Boolean, mask:String?) {
        viewModelScope.launch {
            if (!saved.isLoadedWeek(day)) {
                saved.loadWeekData(day, notExistGenerate)
            }
            val list = saved.getWeekData(day)
            ALog.d("get week data $list")
            busLiveData.publishSuccess(mask?:"getWeekData", list ?: Collections.emptyList<DataItem>())
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
            val r = saved.deleteOneDay(day)
            busLiveData.publishSuccess(mask?:"deleteOneDay", r)
        }
    }

    override fun deleteOneWeek(day: String, mask:String?) {
        viewModelScope.launch {
            val r = saved.deleteOneWeek(day)
            busLiveData.publishSuccess(mask?:"deleteOneWeek", r)
        }
    }

    override fun getDay(day: String, mask:String?) {
        viewModelScope.launch {
            if (saved.isLoadedWeek(day)) {
                val list = saved.getDay(day)
                ALog.d("getDay $day--< $list")
                busLiveData.publishSuccess(mask?:"getDay", list ?: Collections.emptyList<DataItem>())
            } else {
                busLiveData.publishError(mask?:"getDay", "do not call loadWeekData early.")
            }
        }
    }

}