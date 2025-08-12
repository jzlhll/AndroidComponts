package com.au.audiorecordplayer.cam2.bean

sealed class UiRecordBean {
    data class RecordStart(val suc: Boolean) : UiRecordBean()
    data class RecordEnd(val path: String) : UiRecordBean()
    data class RecordFailed(val err: Int) : UiRecordBean()
}