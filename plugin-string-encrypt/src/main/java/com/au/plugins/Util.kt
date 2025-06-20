package com.au.plugins

fun Any.pluginPrintln(str:String) {
    val className = this::class.simpleName
    println("AUPlg-[$className]: $str")
}