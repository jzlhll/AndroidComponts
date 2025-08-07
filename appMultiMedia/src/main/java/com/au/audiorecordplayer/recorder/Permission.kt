package com.au.audiorecordplayer.recorder

fun ISimpleRecord.safeRun(block: () -> Unit) {


    try {
        block()
    } catch (e: Exception) {
        e.printStackTrace()
    }
}