package com.au.audiorecordplayer.cam2.base

import com.au.audiorecordplayer.cam2.bean.TakePictureCallbackWrap

/**
 * MyCameraManager的方法抽象
 *
 * @code MyCamera
 */
interface ICameraMgr {
    fun openCamera()

    fun showPreview()

    /**
     * 关闭预览，关闭录像等。但是没有关闭Camera
     */
    fun closeSession()

    fun closeCamera()

    fun startRecord(callback: IRecordCallback)

    fun stopRecord()

    fun takePicture(bean: TakePictureCallbackWrap)
}
