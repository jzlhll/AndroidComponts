package com.au.audiorecordplayer.cam2.impl

import android.content.Context
import android.content.res.Configuration
import android.graphics.SurfaceTexture
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.view.Surface
import android.view.SurfaceHolder
import androidx.activity.ComponentActivity
import com.au.audiorecordplayer.cam2.base.IActionRecord
import com.au.audiorecordplayer.cam2.base.IActionTakePicture
import com.au.audiorecordplayer.cam2.base.ICameraMgr
import com.au.audiorecordplayer.cam2.base.IRecordCallback
import com.au.audiorecordplayer.cam2.bean.TakePictureCallbackWrap
import com.au.audiorecordplayer.cam2.impl.states.StateDied
import com.au.audiorecordplayer.cam2.impl.states.StatePictureAndPreview
import com.au.audiorecordplayer.cam2.impl.states.StatePictureAndRecordAndPreview
import com.au.audiorecordplayer.cam2.impl.states.StatePreview
import com.au.audiorecordplayer.cam2.view.Cam2PreviewView
import com.au.audiorecordplayer.cam2.view.IViewStatusChangeCallback
import com.au.audiorecordplayer.util.MyLog
import com.au.module_android.simplelivedata.NoStickLiveData
import com.au.module_android.utils.getScreenFullSize

class MyCamManager(context: ComponentActivity,
                   private val cameraView: Cam2PreviewView,
                   var mDefaultTransmitIndex:Int = TRANSMIT_TO_MODE_PREVIEW,
                   looper: Looper) : Handler(looper), ICameraMgr {
    val constStateDied = "StateDied"
    val constStatePreview = "StatePreview"
    val constStatePictureAndPreview = "StatePictureAndPreview"
    val constStatePictureAndRecordAndPreview = "StatePictureAndRecordAndPreview"

    val systemCameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager

    private val previewNeedSize : android.util.Size
    init {
        val clz = if (isSurfaceView()) SurfaceHolder::class.java else SurfaceTexture::class.java
        val pair = context.getScreenFullSize()
        var wishW: Int = pair.first
        var wishH: Int = pair.second
        if (wishW < wishH) {
            val h = wishW
            wishW = wishH
            wishH = h
        }
        MyLog.d("StatePreview: wishSize $wishW*$wishH")
        previewNeedSize = PreviewSizeUtil().needSize("State Preview", clz, this@MyCamManager, wishW, wishH)
        MyLog.d("StatePreview: needSize " + previewNeedSize.width + " * " + previewNeedSize.height)
    }

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

    val modeLiveData = NoStickLiveData<String>()

    /**
     * 必须设置的参数
     */
    var openCameraSafety:()->Unit = {}

    val previewViewCallback = object : IViewStatusChangeCallback {
        override fun onSurfaceCreated(holder: SurfaceHolder?, surfaceTexture: SurfaceTexture?) {
            MyLog.d("onSurface Created")
            val needSize = previewNeedSize
            holder?.setFixedSize(needSize.width, needSize.height)
            surfaceTexture?.setDefaultBufferSize(needSize.width, needSize.height)

            val orientation = context.resources.configuration.orientation
            if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                cameraView.setAspectRatio(needSize.width, needSize.height)
            } else {
                cameraView.setAspectRatio(needSize.height, needSize.width)
            }
            openCameraSafety()
        }

        override fun onSurfaceDestroyed() {
            closeSession()
        }

        override fun onSurfaceChanged() {
        }
    }

    /**
     * 拿到当前View的Surface
     */
    fun getRealViewSurface(): Surface {
        return cameraView.surface
    }

    fun isSurfaceView() : Boolean {
        return Cam2PreviewView.isSurfaceView
    }

    var currentState: AbstractStateBase? = null //当前preview的状态
    var cameraDevice: CameraDevice? = null //camera device
    var cameraId = 0

    //todo 删除
    var camSession: CameraCaptureSession? = null

    override fun openCamera() {
        MyLog.d("open Camera in manage!");
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

    fun closeCameraDirectly() {
        MyLog.d("close Camera directly in manage!")
        removeCallbacksAndMessages(null)
        currentState = null
        cameraDevice?.close()
        cameraDevice = null
        camSession?.close()
        camSession = null
        modeLiveData.setValueSafe("Camera Closed")
    }

    override fun startRecord(callback: IRecordCallback) {
        sendMessage(obtainMessage(TRANSMIT_TO_MODE_RECORD_PICTURE_PREVIEW, callback))
    }

    override fun stopRecord() {
        sendEmptyMessage(TRANSMIT_TO_MODE_PICTURE_PREVIEW)
    }

    override fun takePicture(bean : TakePictureCallbackWrap) {
        val curState = currentState
        if (curState is IActionTakePicture) {
            curState.takePicture(bean)
        } else {
            toastCallback?.invoke("当前模式不支持拍照")
        }
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
        val curSt = currentState

        when (msg.what) {
            ACTION_CAMERA_OPEN -> {
                try {
                    val list = systemCameraManager.cameraIdList
                    list.forEach { cameraId->
                        MyLog.d("systemCameraManger list : $cameraId")
                    }
                } catch (e: CameraAccessException) {
                    e.printStackTrace()
                }
                MyLog.d("ACTION Camera OPEN")
                cameraId = CameraCharacteristics.LENS_FACING_FRONT
                val cameraIdStr = cameraId.toString()
                currentState = StateDied(this)
                modeLiveData.setValueSafe(constStateDied)
                try {
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
                closeCameraDirectly()
            }

            ACTION_CLOSE_SESSION -> {
                MyLog.d("close Camera in ACTION_CAMERA _CLOSE!")
                currentState?.closeSession()
                currentState = StateDied(this)
                modeLiveData.setValueSafe(constStateDied)
            }

            TRANSMIT_TO_MODE_PREVIEW -> {
                if (curSt == null) {
                    MyLog.d("Goto TRANSMIT_TO_MODE_PREVIEW mode error cause it's deed")
                    ifCurrentStNullOpenCameraFirst(msg)
                    return
                }

                if (curSt is IActionRecord) {
                    curSt.stopRecord()
                }

                if (curSt.javaClass.simpleName == constStatePreview) {
                    toastCallback?.invoke("Already in this mod")
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
                    modeLiveData.setValueSafe(constStatePreview)
                } catch (e: Exception) {
                    MyLog.e("start preview err0")
                    e.printStackTrace()
                }
            }

            TRANSMIT_TO_MODE_PICTURE_PREVIEW -> {
                if (curSt == null) {
                    MyLog.d("Goto TRANSMIT_TO_MODE_PICTURE_PREVIEW mode error cause it's deed")
                    ifCurrentStNullOpenCameraFirst(msg)
                    return
                }

                if (curSt.javaClass.simpleName == constStatePictureAndPreview) {
                    toastCallback?.invoke("已经在拍照预览模式下")
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
                    modeLiveData.setValueSafe(constStatePictureAndPreview)
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
                    toastCallback?.invoke("Already in this mode")
                    return
                }

                curSt.closeSession() //关闭session

                val callback = msg.obj as IRecordCallback
                MyLog.d("setRecordPath ")
                val newState = StatePictureAndRecordAndPreview(this)
                currentState = newState
                try {
                    newState.createSession(object : IStateTakePictureRecordCallback {
                        override fun onRecordStart(suc: Boolean) {
                            callback.onRecordStart(suc)
                        }

                        override fun onRecordError(err: Int) {
                            //TODO 完成后，退回之前的state，这里直接回到PreviewAndPicture
                            callback.onRecordFailed(err)
                            sendEmptyMessage(TRANSMIT_TO_MODE_PICTURE_PREVIEW)
                        }

                        override fun onRecordEnd(path: String) {
                            //TODO 完成后，退回之前的state，这里直接回到PreviewAndPicture
                            callback.onRecordEnd(path)
                            sendEmptyMessage(TRANSMIT_TO_MODE_PICTURE_PREVIEW)
                        }

                        override fun onPreviewSucceeded() {
                            MyLog.d("rec:onPreviewSucceeded in myacmera")
                        }

                        override fun onPreviewFailed() {
                            MyLog.d("rec:onPreviewFailed in myacmera")
                        }
                    })
                    modeLiveData.setValueSafe(constStatePictureAndRecordAndPreview)
                } catch (e: Exception) {
                    MyLog.e("rec:start preview err0")
                    e.printStackTrace()
                }
            }
        }
    }
}