package com.allan.androidlearning.coroutine

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.SupervisorJob

/**
 * @author allan
 * @date :2024/7/29 11:51
 * @description:
 */
object CoroutineConst {
    val scope = MainScope()
    val bgScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
}