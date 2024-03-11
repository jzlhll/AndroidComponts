package com.au.jobstudy.home

import com.au.jobstudy.bean.Subject

/**
 * @author au
 * @date :2023/11/27 16:11
 * @description:
 */
sealed class ThisWeekUiData {
    class ThisWeekLayoutData(val num:Int) : ThisWeekUiData()
    class ThisWeekEachLayoutData(val eachStars:Array<Pair<Subject, Int>>) : ThisWeekUiData()
}