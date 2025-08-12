package com.au.audiorecordplayer.cam2.bean
sealed class UiPictureBean {
    data class PictureToken(val path: String) : UiPictureBean()
    data class PictureFailed(val err: Int) : UiPictureBean()
}