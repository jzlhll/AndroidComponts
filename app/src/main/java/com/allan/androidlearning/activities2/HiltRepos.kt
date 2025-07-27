package com.allan.androidlearning.activities2

import com.au.module_android.utils.logd
import javax.inject.Inject

class HiltRepos @Inject constructor() {
    fun test() {
        logd { "HiltRepo test run." }
    }
}