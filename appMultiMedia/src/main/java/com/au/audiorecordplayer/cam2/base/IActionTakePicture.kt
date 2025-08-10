package com.au.audiorecordplayer.cam2.base

import com.au.audiorecordplayer.cam2.bean.TakePictureCallbackWrap

interface IActionTakePicture {
    fun takePicture(bean: TakePictureCallbackWrap)
}