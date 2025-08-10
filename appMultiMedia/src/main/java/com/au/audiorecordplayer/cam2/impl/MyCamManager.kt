package com.au.audiorecordplayer.cam2.impl

import android.content.Context
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import android.hardware.camera2.CaptureRequest
import android.hardware.camera2.params.StreamConfigurationMap
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.view.Surface
import com.au.audiorecordplayer.cam2.base.FeatureUtil
import com.au.audiorecordplayer.cam2.base.IActionRecord
import com.au.audiorecordplayer.cam2.base.IActionTakePicture
import com.au.audiorecordplayer.cam2.bean.RecordCallbackWrap
import com.au.audiorecordplayer.cam2.bean.TakePictureCallbackWrap
import com.au.audiorecordplayer.cam2.impl.states.StateDied
import com.au.audiorecordplayer.cam2.impl.states.StatePictureAndPreview
import com.au.audiorecordplayer.cam2.impl.states.StatePictureAndRecordAndPreview
import com.au.audiorecordplayer.cam2.impl.states.StatePictureAndRecordAndPreview.IStateTakePictureRecordCallback
import com.au.audiorecordplayer.cam2.impl.states.StatePreview
import com.au.audiorecordplayer.cam2.impl.states.StatePreview.IStatePreviewCallback
import com.au.audiorecordplayer.cam2.view.Cam2PreviewView
import com.au.audiorecordplayer.util.CamLog
import com.au.audiorecordplayer.util.MyLog
import java.io.File

class MyCamManager(context: Context,
                   private val cameraView: Cam2PreviewView,
                   var mDefaultTransmitIndex:Int = TRANSMIT_TO_MODE_PREVIEW,
                   looper: Looper) : Handler(looper) {

    val systemCameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager

    companion object {
        const val ACTION_CAMERA_OPEN: Int = 11
        const val ACTION_CAMERA_CLOSE: Int = 12

        const val ACTION_CLOSE_SESSION: Int = 13

        const val TRANSMIT_TO_MODE_PREVIEW: Int = 101
        const val TRANSMIT_TO_MODE_PICTURE_PREVIEW: Int = 102
        const val TRANSMIT_TO_MODE_RECORD_PICTURE_PREVIEW: Int = 103 //其实就是将其他状态升级到录制状态去
    }

    /**
     * 可以设置的参数：
     * UI提醒的问题
     */
    var toastCallback:((String)->Unit)? = null

    /**
     * 拿到当前View的Surface
     */
    fun getRealViewSurface(): Surface {
        return cameraView.surface
    }

    var currentState: AbstractStateBase? = null //当前preview的状态
    var camCharacters: CameraCharacteristics? = null //特性
    var cameraDevice: CameraDevice? = null //camera device
    var cameraId = 0
    var previewBuilder: CaptureRequest.Builder? = null
    var camSession: CameraCaptureSession? = null

    fun destroy() {
        cameraDevice?.close()
        camSession?.close()
    }

    fun takePicture(bean : TakePictureCallbackWrap) {
        val curState = currentState
        if (curState is IActionTakePicture) {
            curState.takePicture(bean)
        } else {
            toastCallback?.invoke("当前模式不支持拍照")
        }
    }

    fun setPreviewSize(width: Int, height: Int) {
        cameraView.setAspectRatio(width * 1.0 / height)
    }

    //Camera打开回调
    private val mCameraStateCallback: CameraDevice.StateCallback = object : CameraDevice.StateCallback() {
        override fun onOpened(camera: CameraDevice) {
            cameraDevice = camera
            sendEmptyMessage(mDefaultTransmitIndex)
        }

        override fun onDisconnected(camera: CameraDevice) {
            camera.close()
            cameraDevice = null
        }

        override fun onError(camera: CameraDevice, error: Int) {
            camera.close()
            cameraDevice = null
        }
    }

    private fun ifCurrentStNullOpenCameraFirst(msg: Message) {
        val thisMsg = obtainMessage()
        thisMsg.copyFrom(msg)
        sendMessageAtFrontOfQueue(msg) //拷贝一次 发出去
        sendMessageAtFrontOfQueue(obtainMessage(ACTION_CAMERA_OPEN)) //再将开启消息传递到前面去。这样的话，就优先开启
    }

    override fun handleMessage(msg: Message) {
        var curSt: AbstractStateBase? = currentState

        when (msg.what) {
            ACTION_CAMERA_OPEN -> {
                if (curSt != null) {
                    CamLog.e("Error if state machine is not null!")
                    return
                }

                try {
                    val list = systemCameraManager.cameraIdList
                    MyLog.d("systemCameraManger list : $list")
                } catch (e: CameraAccessException) {
                    e.printStackTrace()
                }
                MyLog.d("ACTION Camera OPEN")
                //TODO 其实如果这里不设置，系统会camera显示的区域更多。我认为谷歌在camera的显示区域这块这些不够好
                //setPreviewSize(1920, 1080)

                cameraId = CameraCharacteristics.LENS_FACING_FRONT
                val cameraIdStr = cameraId.toString()
                currentState = StateDied(this)
                try {
                    camCharacters = systemCameraManager.getCameraCharacteristics(cameraIdStr)
                    val map: StreamConfigurationMap? = camCharacters?.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
                    if (map == null) {
                        CamLog.e("map = null")
                        return
                    }
                    systemCameraManager.openCamera(cameraIdStr, mCameraStateCallback, this)
                } catch (e: CameraAccessException) {
                    e.printStackTrace()
                } catch (e: NullPointerException) {
                    e.printStackTrace()
                } catch (e: SecurityException) {
                    e.printStackTrace()
                }
            }

            ACTION_CAMERA_CLOSE -> {
                CamLog.d("close Camera in ACTION_CAMERA _CLOSE!")
                currentState?.closeSession()
                currentState = null

                cameraDevice?.close()
                cameraDevice = null
            }

            ACTION_CLOSE_SESSION -> {
                CamLog.d("close Camera in ACTION_CAMERA _CLOSE!")
                currentState?.closeSession()
                currentState = StateDied(this)
            }

            TRANSMIT_TO_MODE_PREVIEW -> {
                if (curSt == null) {
                    CamLog.d("Goto TRANSMIT_TO_MODE_PREVIEW mode error cause it's deed")
                    ifCurrentStNullOpenCameraFirst(msg)
                    return
                }

                if (curSt is IActionRecord) {
                    curSt.stopRecord()
                }

                if (curSt.javaClass.simpleName == "StatePreview") {
                    toastCallback?.invoke("Already in this mod")
                    return
                }

                curSt.closeSession() //关闭session
                currentState = StatePreview(this)

                try {
                    curSt.createSession(object : IStatePreviewCallback {
                        override fun onPreviewSucceeded() {
                            CamLog.d("onPreview Succeeded in myCam Manager")
                        }

                        override fun onPreviewFailed() {
                            CamLog.d("onPreview Failed in myCam Manager")
                        }
                    })
                } catch (e: Exception) {
                    CamLog.e("start preview err0")
                    e.printStackTrace()
                }
            }

            TRANSMIT_TO_MODE_PICTURE_PREVIEW -> {
                if (curSt == null) {
                    CamLog.d("Goto TRANSMIT_TO_MODE_PICTURE_PREVIEW mode error cause it's deed")
                    ifCurrentStNullOpenCameraFirst(msg)
                    return
                }

                if (curSt.javaClass.simpleName == "StatePictureAndPreview") {
                    toastCallback?.invoke("已经在拍照预览模式下")
                    return
                }

                if (curSt is IActionRecord) {
                    curSt.stopRecord()
                }

                curSt.closeSession() //关闭session
                curSt = StatePictureAndPreview(this)

                try {
                    curSt.createSession(object : IStatePreviewCallback {
                        override fun onPreviewSucceeded() {
                            CamLog.d("onPreview Succeeded in myCam Manager")
                        }

                        override fun onPreviewFailed() {
                            CamLog.d("onPreview Failed in myCam Manager")
                        }
                    })
                } catch (e: Exception) {
                    CamLog.e("start preview err0")
                    e.printStackTrace()
                }
            }

            TRANSMIT_TO_MODE_RECORD_PICTURE_PREVIEW -> {
                //其实就是将其他状态升级到录制状态去
                if (curSt == null) {
                    CamLog.d("Goto (ACTION_START_REC / transmit to RECORD) mode error cause it's deed")
                    ifCurrentStNullOpenCameraFirst(msg)
                    return
                }
                if (curSt.javaClass.simpleName == "StatePictureAndRecordAndPreview") {
                    toastCallback?.invoke("Already in this mode")
                    return
                }

                curSt.closeSession() //关闭session

                val func = msg.obj as RecordCallbackWrap
                val callback = func.callback
                CamLog.d("setRecordPath ")
                setRecordFilePath(func.path + File.separator + func.name) //TODO 由于createSurfaces是在构造函数中调用，没法直接传递参数
                curSt = StatePictureAndRecordAndPreview(mManager)
                mManager.setCurrentState(curSt)
                try {
                    curSt.createSession(object : IStateTakePictureRecordCallback {
                        override fun onRecordStart(suc: Boolean) {
                            mManager.notifyModChange(FirstActivity.MODE_PicturePreviewVideo)
                            callback.onRecordStart(suc)
                        }

                        override fun onRecordError(err: Int) {
                            //TODO 完成后，退回之前的state，这里直接回到PreviewAndPicture
                            callback.onRecordFailed(err)
                            camHandler.sendEmptyMessage(MyCameraManager.TRANSMIT_TO_MODE_PICTURE_PREVIEW)
                        }

                        override fun onRecordEnd(path: String) {
                            //TODO 完成后，退回之前的state，这里直接回到PreviewAndPicture
                            callback.onRecordEnd(path)
                            camHandler.sendEmptyMessage(MyCameraManager.TRANSMIT_TO_MODE_PICTURE_PREVIEW)
                        }

                        override fun onPreviewSucceeded() {
                            CamLog.d("rec:onPreviewSucceeded in myacmera")
                        }

                        override fun onPreviewFailed() {
                            CamLog.d("rec:onPreviewFailed in myacmera")
                        }
                    })
                } catch (e: Exception) {
                    CamLog.e("rec:start preview err0")
                    e.printStackTrace()
                }
            }
        }
    }
}