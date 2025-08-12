package com.au.audiorecordplayer.cam2.base

import android.view.Surface

/**
 * MyCameraManager的方法抽象
 *
 * @code MyCamera
 */
interface ICameraMgr {
    fun openCamera(surface: Surface)

    fun showPreview()

    /**
     * 关闭预览，关闭录像等。但是没有关闭Camera
     */
    fun closeSession()

    fun closeCamera()

    fun startRecord()

    fun stopRecord()

    fun takePicture(dir:String, name:String)

    fun switchFontBackCam()
}
