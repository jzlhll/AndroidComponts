package com.au.jobstudy.saves

import com.au.jobstudy.bean.DataItem
import com.au.jobstudy.bean.DataOneWeek
import com.au.jobstudy.bean.fillDataItemMode
import com.au.jobstudy.bean.randomGetTwoSubjects

class SaveImpl : ISave {
    override fun generateWeekData(weekStartDay: String, weekData: DataOneWeek) {
        val list = ArrayList<DataItem>()
        for (i in 0 until  7) {
            val twoSubjects = randomGetTwoSubjects()
            val firstCheck = twoSubjects[0].randomOneAction()
            val secondCheck = twoSubjects[0].randomOneAction()

            val dataItem1 = DataItem().apply {
                day = weekStartDay + i
                this.weekStartDay = weekStartDay
                index = 0
                subject = twoSubjects[0].name
                fillDataItemMode(this, firstCheck)
            }
            val dataItem2 = DataItem().apply {
                day = weekStartDay + i
                this.weekStartDay = weekStartDay
                index = 1
                subject = twoSubjects[1].name
                fillDataItemMode(this, secondCheck)
            }
            list.add(dataItem1)
            list.add(dataItem2)
        }
    }

    override fun updateOneDay(day: String, index: Int, item: DataItem): Boolean {
        TODO("Not yet implemented")
    }

    override fun deleteOneDay(day: String): Boolean {
        TODO("Not yet implemented")
    }

    override fun deleteOneWeek(week: String): Boolean {
        TODO("Not yet implemented")
    }

    override fun readWeekData(week: String): DataOneWeek? {
        TODO("Not yet implemented")
    }

    override fun getDay(dayInWeek: Int, weekData: DataOneWeek) {
        TODO("Not yet implemented")
    }

    override fun getWeek(backWeek: Int): DataOneWeek {
        TODO("Not yet implemented")
    }
}