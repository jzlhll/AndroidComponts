package com.au.audiorecordplayer.cam2.base

interface IActionRecord {
    fun stopRecord()
    //startRecord(); //由于Camera2加状态机，所以在切换RecordState的时候，会直接开始录制；因此，不用处理startRecord
}
