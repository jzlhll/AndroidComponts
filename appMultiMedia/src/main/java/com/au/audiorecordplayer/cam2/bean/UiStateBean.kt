package com.au.audiorecordplayer.cam2.bean
data class UiStateBean(val cameraIdStr:String,
                       val currentMode:String,
                       val needSwitchToCamIdBean: UiNeedSwitchToCamIdBean? = null,
                       val recordBean: UiRecordBean? = null,
                       val pictureTokenBean: UiPictureBean? = null)