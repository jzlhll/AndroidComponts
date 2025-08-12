package com.au.audiorecordplayer.cam2

import android.Manifest
import android.content.Context
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.SurfaceTexture
import android.hardware.camera2.CameraManager
import android.os.Bundle
import android.os.SystemClock
import android.util.Size
import android.view.Surface
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.TextureView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.au.audiorecordplayer.cam2.bean.UiPictureBean
import com.au.audiorecordplayer.cam2.bean.UiRecordBean
import com.au.audiorecordplayer.cam2.bean.UiStateBean
import com.au.audiorecordplayer.cam2.impl.MyCamManager
import com.au.audiorecordplayer.cam2.impl.MyCamManager.Companion.TRANSMIT_TO_MODE_PICTURE_PREVIEW
import com.au.audiorecordplayer.cam2.impl.MyCamManager.Companion.TRANSMIT_TO_MODE_PREVIEW
import com.au.audiorecordplayer.cam2.impl.MyCamViewModel
import com.au.audiorecordplayer.cam2.impl.PreviewSizeUtil
import com.au.audiorecordplayer.cam2.view.Cam2PreviewView
import com.au.audiorecordplayer.cam2.view.IViewStatusChangeCallback
import com.au.audiorecordplayer.databinding.FragmentCamera2Binding
import com.au.audiorecordplayer.util.FileUtil
import com.au.audiorecordplayer.util.MainUIManager
import com.au.audiorecordplayer.util.MyLog
import com.au.module_android.Globals
import com.au.module_android.click.onClick
import com.au.module_android.permissions.createMultiPermissionForResult
import com.au.module_android.simpleflow.StatusState
import com.au.module_android.simpleflow.collectStatusState
import com.au.module_android.ui.bindings.BindingFragment
import com.au.module_android.utils.asOrNull
import com.au.module_android.utils.currentStatusBarAndNavBarHeight
import com.au.module_android.utils.dp
import com.au.module_android.utils.getScreenFullSize
import com.au.module_android.utils.gone
import com.au.module_android.utils.unsafeLazy
import com.au.module_android.utils.visible
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class Camera2Fragment : BindingFragment<FragmentCamera2Binding>() {
    override fun isPaddingStatusBar(): Boolean {
        return false
    }

    val permissions = arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO)
    private val permissionHelper = createMultiPermissionForResult(permissions)

    private val viewModel by unsafeLazy { ViewModelProvider(requireActivity())[MyCamViewModel::class.java] }
    private var orientation = -1
    var previewNeedSize = Size(0, 0)

    fun openCameraSafety(surface: Surface) {
        permissionHelper.safeRun({
            viewModel.camManager.openCamera(surface)
        }, notGivePermissionBlock = {
            MainUIManager.get().toastSnackbar(view, "请授予相机和录音权限。")
        })
    }

    val previewViewCallback = object : IViewStatusChangeCallback {
        override fun onSurfaceCreated() {
            MyLog.d("onSurface Created")
            onSurfaceCreatedInit()
        }

        override fun onSurfaceDestroyed() {
            viewModel.camManager.closeSession()
        }

        override fun onSurfaceChanged() {
        }
    }

    private fun onSurfaceCreatedInit() {
        val needSize = previewNeedSize
        binding.previewView.realView?.asOrNull<SurfaceView>()?.holder?.setFixedSize(needSize.width, needSize.height)
        binding.previewView.realView?.asOrNull<TextureView>()?.surfaceTexture?.setDefaultBufferSize(needSize.width, needSize.height)

        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            binding.previewView.setAspectRatio(needSize.width, needSize.height)
        } else {
            binding.previewView.setAspectRatio(needSize.height, needSize.width)
        }
        openCameraSafety(binding.previewView.surface)
    }

    /**
     * 因为是ViewModel中，不得将ac持有和用在lambda和回调中。
     */
    fun changePreviewNeedSize(ac: FragmentActivity) {
        orientation = ac.resources.configuration.orientation
        val clz = if (Cam2PreviewView.isSurfaceView) SurfaceHolder::class.java else SurfaceTexture::class.java
        val pair = ac.getScreenFullSize()
        var wishW: Int = pair.first
        var wishH: Int = pair.second
        if (wishW < wishH) {
            val h = wishW
            wishW = wishH
            wishH = h
        }
        MyLog.d("wishSize $wishW*$wishH")
        val systemCameraManager = Globals.app.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        previewNeedSize = PreviewSizeUtil().needSize("State Preview", clz, systemCameraManager, "" + viewModel.camManager.cameraId, wishW, wishH)
        MyLog.d("needSize " + previewNeedSize.width + " * " + previewNeedSize.height)
    }

    override fun onBindingCreated(savedInstanceState: Bundle?) {
        changePreviewNeedSize(requireActivity())
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                //2. 收集到了用户的 数据
                viewModel.camManager.uiState.collectStatusState(
                    success = { bean->
                        MyLog.d("uiState collected! $bean")
                        val picture = bean.pictureTokenBean
                        val record = bean.recordBean
                        val needSwitchToCamIdBean = bean.needSwitchToCamIdBean

                        binding.modeTv.text = bean.currentMode

                        if (picture != null) {
                            when (picture) {
                                is UiPictureBean.PictureFailed -> {
                                    toastOnText("拍照失败：errorCode${picture.err}")
                                }
                                is UiPictureBean.PictureToken -> {
                                    toastOnText("拍照成功：${picture.path}", 6000)
                                }
                            }
                        } else if (record != null) {
                            when (record) {
                                is UiRecordBean.RecordEnd -> {
                                    isRecording = false
                                    toastOnText("视频保存在：${record.path}", 6000)
                                    binding.timeTv.handler.removeCallbacks(mTimeUpdateRunnable)
                                    mTimeSec = 0
                                    binding.recordBtn.text = "start record"
                                    binding.recordBtn.setTextColor(Color.WHITE)
                                    binding.timeTv.gone()
                                }
                                is UiRecordBean.RecordStart -> {
                                    toastOnText("录制开始...")
                                    if (record.suc) {
                                        isRecording = true
                                        mRunnableLastTime = 0
                                        mRunnableIndex = 0
                                        binding.recordBtn.text = "stop record"
                                        binding.recordBtn.setTextColor(Color.RED)
                                        binding.timeTv.visible()
                                        binding.timeTv.handler.post(mTimeUpdateRunnable)
                                    } else {
                                        isRecording = false
                                        toastOnText("录制出现异常")
                                    }
                                }
                                is UiRecordBean.RecordFailed -> {
                                    toastOnText("录制失败：errorCode${record.err}")
                                }
                            }
                        } else if (needSwitchToCamIdBean != null) {
                            toastOnText("切换摄像头...")
                            onSurfaceCreatedInit()
                        }
                    },
                    error = { exMsg->
                    }
                )
            }
        }

        lifecycleScope.launch {
            MyLog.d("toast collect start...")
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.camManager.toastState.collect {
                    MyLog.d("toast state collected! $it")
                    toastOnText(it.msg)
                }
            }
        }

        binding.previewView.setCallback(previewViewCallback)

        binding.takePicBtn.onClick {
            viewModel.camManager.takePicture(
                Globals.goodFilesDir.absolutePath + "/pictures", "PIC_" + FileUtil.longTimeToStr(System.currentTimeMillis()) + ".jpg")
        }

        binding.recordBtn.onClick {
            if (!isRecording) {
                isRecording = true
                viewModel.camManager.startRecord()
            } else {
                viewModel.camManager.stopRecord()
            }
        }
        binding.switchCamBtn.onClick {
            changePreviewNeedSize(requireActivity())

            val currentMode = viewModel.camManager.uiState.value.asOrNull<StatusState.Success<UiStateBean>>()?.data
            when (currentMode?.currentMode) {
                MyCamManager.constStatePictureAndPreview,
                MyCamManager.constStatePreview -> {
                    viewModel.camManager.switchFontBackCam()
                }
                else -> {
                    toastOnText("当前模式不支持切换")
                }
            }
        }

        binding.modeTv.onClick {
            val currentMode = viewModel.camManager.uiState.value.asOrNull<StatusState.Success<UiStateBean>>()?.data
            when (currentMode?.currentMode) {
                 MyCamManager.constStatePreview -> {
                     viewModel.camManager.sendEmptyMessage(TRANSMIT_TO_MODE_PICTURE_PREVIEW)
                }
                MyCamManager.constStatePictureAndPreview -> {
                    viewModel.camManager.sendEmptyMessage(TRANSMIT_TO_MODE_PREVIEW)
                }
                else -> {
                    toastOnText("当前模式不支持切换")
                }
            }
        }
        binding.modeTv.post {
            requireActivity().currentStatusBarAndNavBarHeight().also { bars->
                binding.modeTv.layoutParams = (binding.modeTv.layoutParams as ConstraintLayout.LayoutParams).also {
                    it.topMargin = (bars?.first ?: 32.dp) + 4.dp
                }
            }
        }
    }

    /////////////////////////recording

    var isRecording: Boolean = false
    private var mTimeSec = 0
    private var mRunnableIndex = 0
    private var mRunnableLastTime = 0L
    private val mTimeUpdateRunnable: Runnable = object : Runnable {
        override fun run() {
            if (!isResumed) {
                return
            }

            if (mRunnableLastTime == 0L) {
                mRunnableLastTime = SystemClock.elapsedRealtime()
            }
            binding.timeTv.text = String.format("· %d", mTimeSec++)
            var delayTime: Long = 1000
            if (mRunnableIndex++ == 10) {
                mRunnableIndex = 0 //每10s 修正delay的时间
                val cur = SystemClock.elapsedRealtime()
                delayTime = 1000 - (cur - 10000 - mRunnableLastTime)
                if (delayTime < 0) {
                    delayTime = 0
                }
                mRunnableLastTime = cur
            }
            binding.timeTv.postDelayed(mTimeUpdateRunnable, delayTime)
        }
    }

    private var mToastJob : Job? = null
    private fun toastOnText(string:String, duration:Long = 3000) {
        val job = mToastJob
        if (job != null) {
            binding.toastInfo.text = binding.toastInfo.text.toString() + "\n" + string
            binding.toastInfo.visible()
            job.cancel()
        } else {
            binding.toastInfo.text = string
            binding.toastInfo.visible()
        }

        mToastJob = lifecycleScope.launch {
            delay(duration)
            binding.toastInfo.text = ""
            binding.toastInfo.gone()
            mToastJob = null
        }
    }
}