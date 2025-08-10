package com.au.audiorecordplayer.cam2.base

import com.au.audiorecordplayer.cam2.bean.RecordCallbackWrap
import com.au.audiorecordplayer.cam2.bean.TakePictureCallbackWrap

/**
 * MyCameraManager的方法抽象
 *
 * @code MyCamera
 */
interface ICameraManager {
    fun openCamera()

    fun closeSession()

    fun closeCamera()

    fun transmitModById(transmitId: Int)

    fun startRecord(wrap: RecordCallbackWrap)

    fun stopRecord()

    fun takePicture(wrap: TakePictureCallbackWrap)
}