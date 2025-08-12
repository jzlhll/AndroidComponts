package com.au.audiorecordplayer.cam2.impl

import android.content.Context
import android.graphics.SurfaceTexture
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.view.Surface
import android.view.SurfaceHolder
import com.au.audiorecordplayer.cam2.base.IActionRecord
import com.au.audiorecordplayer.cam2.base.IActionTakePicture
import com.au.audiorecordplayer.cam2.base.ICameraMgr
import com.au.audiorecordplayer.cam2.base.ITakePictureCallback
import com.au.audiorecordplayer.cam2.bean.TakePictureCallbackWrap
import com.au.audiorecordplayer.cam2.bean.UiNeedSwitchToCamIdBean
import com.au.audiorecordplayer.cam2.bean.UiPictureBean
import com.au.audiorecordplayer.cam2.bean.UiRecordBean
import com.au.audiorecordplayer.cam2.bean.UiStateBean
import com.au.audiorecordplayer.cam2.bean.UiToastBean
import com.au.audiorecordplayer.cam2.impl.states.StateDied
import com.au.audiorecordplayer.cam2.impl.states.StatePictureAndPreview
import com.au.audiorecordplayer.cam2.impl.states.StatePictureAndRecordAndPreview
import com.au.audiorecordplayer.cam2.impl.states.StatePreview
import com.au.audiorecordplayer.util.MyLog
import com.au.module_android.Globals
import com.au.module_android.simpleflow.StatusState
import com.au.module_android.utils.asOrNull
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow

class MyCamManager(var mDefaultTransmitIndex:Int = TRANSMIT_TO_MODE_PREVIEW,
                   looper: Looper) : Handler(looper), ICameraMgr, ITakePictureCallback {

    companion object {
        const val constStateNone = "StateCameraClosed"
        const val constStateDied = "StateCameraOpened"
        const val constStatePreview = "StatePreview"
        const val constStatePictureAndPreview = "StatePictureAndPreview"
        const val constStatePictureAndRecordAndPreview = "StatePictureAndRecordAndPreview"

        const val ACTION_CAMERA_OPEN: Int = 11
        const val ACTION_CAMERA_CLOSE: Int = 12

        const val ACTION_CLOSE_SESSION: Int = 13

        const val TRANSMIT_TO_MODE_PREVIEW: Int = 101
        const val TRANSMIT_TO_MODE_PICTURE_PREVIEW: Int = 102
        const val TRANSMIT_TO_MODE_RECORD_PICTURE_PREVIEW: Int = 103 //其实就是将其他状态升级到录制状态去
    }

    private val _uiState = MutableStateFlow<StatusState<UiStateBean>>(StatusState.Loading)
    val uiState: StateFlow<StatusState<UiStateBean>> = _uiState.asStateFlow()

    private val _toastState = MutableSharedFlow<UiToastBean>(extraBufferCapacity = 1)
    val toastState = _toastState.asSharedFlow()

    var currentState: AbstractStateBase? = null //当前preview的状态
    var cameraDevice: CameraDevice? = null //camera device
    var cameraId = CameraCharacteristics.LENS_FACING_BACK

    var surface : Surface? = null

    override fun openCamera(surface: Surface) {
        MyLog.d("open Camera in manage!");
        this.surface = surface
        sendEmptyMessage(ACTION_CAMERA_OPEN)
    }

    override fun showPreview() {
        sendEmptyMessage(TRANSMIT_TO_MODE_PREVIEW)
    }

    override fun closeSession() {
        MyLog.d("close Session in manage!");
        sendEmptyMessage(ACTION_CLOSE_SESSION)
    }

    override fun closeCamera() {
        removeCallbacksAndMessages(null)
        sendEmptyMessage(ACTION_CAMERA_CLOSE)
    }

    fun closeCameraDirectly(removeAll: Boolean) {
        MyLog.d("close Camera directly in manage!")
        if(removeAll) removeCallbacksAndMessages(null)
        currentState?.closeSession()
        currentState = null
        cameraDevice?.close()
        cameraDevice = null
    }

    override fun startRecord() {
        sendMessage(obtainMessage(TRANSMIT_TO_MODE_RECORD_PICTURE_PREVIEW))
    }

    override fun stopRecord() {
        sendEmptyMessage(TRANSMIT_TO_MODE_PICTURE_PREVIEW)
    }

    override fun takePicture(dir: String, name: String) {
        val curState = currentState
        if (curState is IActionTakePicture) {
            curState.takePicture(TakePictureCallbackWrap(dir, name, this))
        } else {
            MyLog.d("current mode not support take picture" + (_toastState.tryEmit(UiToastBean("当前模式不支持拍照", "info"))))

        }
    }

    override fun switchFontBackCam() {
        sendMessage(obtainMessage(ACTION_CAMERA_CLOSE, "switchFontBackCam"))
        cameraId = if (cameraId == CameraCharacteristics.LENS_FACING_FRONT) CameraCharacteristics.LENS_FACING_BACK else CameraCharacteristics.LENS_FACING_FRONT
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
        sendMessageAtFrontOfQueue(thisMsg) //拷贝一次 发出去
        sendMessageAtFrontOfQueue(obtainMessage(ACTION_CAMERA_OPEN)) //再将开启消息传递到前面去。这样的话，就优先开启
    }

    override fun handleMessage(msg: Message) {
        val curSt = currentState

        when (msg.what) {
            ACTION_CAMERA_OPEN -> {
                val systemCameraManager = Globals.app.getSystemService(Context.CAMERA_SERVICE) as CameraManager

                try {
                    val list = systemCameraManager.cameraIdList
                    list.forEach { cameraId->
                        MyLog.d("systemCameraManger list : $cameraId")
                    }
                } catch (e: CameraAccessException) {
                    e.printStackTrace()
                }
                val cameraIdStr = "$cameraId"
                MyLog.d("ACTION Camera OPEN $cameraIdStr")
                currentState = StateDied(this)

                try {
                    systemCameraManager.openCamera(cameraIdStr, mCameraStateCallback, this)
                    _uiState.value = StatusState.Success(UiStateBean(cameraIdStr, constStateDied))
                } catch (e: CameraAccessException) {
                    e.printStackTrace()
                } catch (e: NullPointerException) {
                    e.printStackTrace()
                } catch (e: SecurityException) {
                    e.printStackTrace()
                }
            }

            ACTION_CAMERA_CLOSE -> {
                val isSwitchFrontAndBack = msg.obj == "switchFontBackCam"
                closeCameraDirectly(!isSwitchFrontAndBack)
                if (isSwitchFrontAndBack) {
                    _uiState.value = StatusState.Success(UiStateBean("$cameraId", constStateNone,
                        needSwitchToCamIdBean = UiNeedSwitchToCamIdBean(cameraIdStr = "$cameraId")))
                }
            }

            ACTION_CLOSE_SESSION -> {
                MyLog.d("close Camera in ACTION_CAMERA _CLOSE!")
                currentState?.closeSession()
                currentState = StateDied(this)
                _uiState.value = StatusState.Success(UiStateBean("$cameraId", constStateDied))
            }

            TRANSMIT_TO_MODE_PREVIEW -> {
                if (curSt == null) {
                    MyLog.d("Goto TRANSMIT_2_MODE_PREVIEW mode error cause it's deed")
                    ifCurrentStNullOpenCameraFirst(msg)
                    return
                }

                if (curSt is IActionRecord) {
                    curSt.stopRecord()
                }

                if (curSt.javaClass.simpleName == constStatePreview) {
                    _toastState.tryEmit(UiToastBean("当前模式已处于预览模式"))
                    return
                }

                curSt.closeSession() //关闭session
                val newState = StatePreview(this)
                currentState = newState
                try {
                    newState.createSession(object : IStatePreviewCallback {
                        override fun onPreviewSucceeded() {
                            MyLog.d("onPreview Succeeded in myCam Manager")
                        }

                        override fun onPreviewFailed() {
                            MyLog.d("onPreview Failed in myCam Manager")
                        }
                    })
                    _uiState.value = StatusState.Success(UiStateBean("$cameraId", constStatePreview))
                } catch (e: Exception) {
                    MyLog.e("start preview err0")
                    e.printStackTrace()
                }
            }

            TRANSMIT_TO_MODE_PICTURE_PREVIEW -> {
                if (curSt == null) {
                    MyLog.d("Goto TRANSMIT_ TO_MODE_PICTURE_PREVIEW mode error cause it's deed")
                    ifCurrentStNullOpenCameraFirst(msg)
                    return
                }

                if (curSt.javaClass.simpleName == constStatePictureAndPreview) {
                    _toastState.tryEmit(UiToastBean("已处于拍照预览模式"))
                    return
                }

                if (curSt is IActionRecord) {
                    curSt.stopRecord()
                }

                curSt.closeSession() //关闭session
                val newState = StatePictureAndPreview(this)
                currentState = newState
                try {
                    newState.createSession(object : IStatePreviewCallback {
                        override fun onPreviewSucceeded() {
                            MyLog.d("onPreview Succeeded in myCam Manager")
                        }

                        override fun onPreviewFailed() {
                            MyLog.d("onPreview Failed in myCam Manager")
                        }
                    })
                    _uiState.value = StatusState.Success(UiStateBean("$cameraId", constStatePictureAndPreview))
                } catch (e: Exception) {
                    MyLog.e("start preview err0")
                    e.printStackTrace()
                }
            }

            TRANSMIT_TO_MODE_RECORD_PICTURE_PREVIEW -> {
                //其实就是将其他状态升级到录制状态去
                if (curSt == null) {
                    MyLog.d("Goto (ACTION_START_REC / transmit to RECORD) mode error cause it's deed")
                    ifCurrentStNullOpenCameraFirst(msg)
                    return
                }
                if (curSt.javaClass.simpleName == constStatePictureAndRecordAndPreview) {
                    _toastState.tryEmit(UiToastBean("当前模式已处于录制模式"))
                    return
                }

                curSt.closeSession() //关闭session

                MyLog.d("setRecordPath ")
                val newState = StatePictureAndRecordAndPreview(this)
                currentState = newState
                try {
                    newState.createSession(object : IStateTakePictureRecordCallback {
                        override fun onRecordStart(suc: Boolean) {
                            val curValue = _uiState.value.asOrNull<StatusState.Success<UiStateBean>>()
                            if (curValue != null) {
                                _uiState.value = StatusState.Success(UiStateBean(curValue.data.cameraIdStr, curValue.data.currentMode,
                                    recordBean = UiRecordBean.RecordStart(suc)
                                ))
                            }
                        }

                        override fun onRecordError(err: Int) {
                            val curValue = _uiState.value.asOrNull<StatusState.Success<UiStateBean>>()
                            if (curValue != null) {
                                _uiState.value = StatusState.Success(UiStateBean(curValue.data.cameraIdStr, curValue.data.currentMode,
                                    recordBean = UiRecordBean.RecordFailed(err)
                                ))
                            }

                            sendEmptyMessage(TRANSMIT_TO_MODE_PICTURE_PREVIEW)
                        }

                        override fun onRecordEnd(path: String) {
                            val curValue = _uiState.value.asOrNull<StatusState.Success<UiStateBean>>()
                            if (curValue != null) {
                                _uiState.value = StatusState.Success(UiStateBean(curValue.data.cameraIdStr, curValue.data.currentMode,
                                    recordBean = UiRecordBean.RecordEnd(path)
                                ))
                            }
                            sendEmptyMessage(TRANSMIT_TO_MODE_PICTURE_PREVIEW)
                        }

                        override fun onPreviewSucceeded() {
                            MyLog.d("rec:onPreviewSucceeded in myacmera")
                        }

                        override fun onPreviewFailed() {
                            MyLog.d("rec:onPreviewFailed in myacmera")
                        }
                    })
                    _uiState.value = StatusState.Success(UiStateBean("$cameraId", constStatePictureAndRecordAndPreview))
                } catch (e: Exception) {
                    MyLog.e("rec:start preview err0")
                    e.printStackTrace()
                }
            }
        }
    }

    override fun onPictureToken(path: String) {
        val curValue = _uiState.value.asOrNull<StatusState.Success<UiStateBean>>()
        if (curValue != null) {
            _uiState.value = StatusState.Success(UiStateBean(curValue.data.cameraIdStr, curValue.data.currentMode,
                pictureTokenBean = UiPictureBean.PictureToken(path)
            ))
        }
    }

    override fun onPictureTokenFail(err: Int) {
        val curValue = _uiState.value.asOrNull<StatusState.Success<UiStateBean>>()
        if (curValue != null) {
            _uiState.value = StatusState.Success(UiStateBean(curValue.data.cameraIdStr, curValue.data.currentMode,
                pictureTokenBean = UiPictureBean.PictureFailed(err)
            ))
        }
    }
}