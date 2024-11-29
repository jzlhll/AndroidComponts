package com.allan.androidlearning.crashtest

import com.au.module_android.Globals
import com.au.module_android.utils.launchOnThread
import com.au.module_android.utils.launchOnUi
import kotlinx.coroutines.delay

private const val debugIndex = ""

fun debugOtherActivityCreateCrash() {
    if (debugIndex == "debugOtherActivityCreateCrash") {
        makeACrash()
    }
}

fun debugEntryActivityCreateCrash() {
    if (debugIndex == "debugEntryActivityCreateCrash") {
        makeACrash()
    }
}

fun debugEntryActivityPostMainThreadCrash() {
    if (debugIndex == "debugEntryActivityPostMainThreadCrash") {
        Globals.mainHandler.post{
            makeACrash()
        }
    }
}

fun debugApplicationCreateCrash() {
    if (debugIndex == "debugApplicationCreateCrash") {
        makeACrash()
    }
}

fun debugApplicationPostMainThreadCrash() {
    if (debugIndex == "debugApplicationPostMainThreadCrash") {
        Globals.mainScope.launchOnUi {
            delay(100)
            makeACrash()
        }
    }
}

fun debugSubThreadCrash() {
    if (debugIndex == "debugSubThreadCrash") {
        Globals.mainScope.launchOnThread {
            delay(100)
            makeACrash()
        }
    }
}

private fun makeACrash() {
    val a = 10
    val b = 0
    val c = a / b
    println("$c")
}