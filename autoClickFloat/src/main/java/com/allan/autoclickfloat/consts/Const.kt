package com.allan.autoclickfloat.consts

object Const {
    const val TAG = "autoclickfloat"

    val autoOnePoint = OnePointAuto()

    init {
        autoOnePoint.loadAutoOnePoint()
        autoOnePoint.loadAutoOnePointMs()
    }
}