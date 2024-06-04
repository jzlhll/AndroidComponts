package com.au.jobstudy.pass

import com.au.jobstudy.consts.WeekDateUtil.timeToWeekStartDay
import com.au.module_android.Globals
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * @author allan
 * @date :2024/3/11 10:28
 * @description:
 */
class Pass {
    private val oneWeekPassCount = 1

    fun isThisWeekCanUse(scope: CoroutineScope = Globals.mainScope, block:(Boolean)->Unit) {
        val weekStartDay = timeToWeekStartDay()
        scope.launch {
            val r = com.au.module.cached.AppDataStore.read("use_pass_week_start_day_$weekStartDay", 0)
            block(r < oneWeekPassCount)
        }
    }

    fun useOnePassCount(leftCountBlock: (Int) -> Unit) {
        val weekStartDay = timeToWeekStartDay()
        Globals.mainScope.launch {
            val r = com.au.module.cached.AppDataStore.read("use_pass_week_start_day_$weekStartDay", 0)
            com.au.module.cached.AppDataStore.save("use_pass_week_start_day_$weekStartDay", r + 1)
            leftCountBlock(oneWeekPassCount - (r + 1))
        }
    }
}