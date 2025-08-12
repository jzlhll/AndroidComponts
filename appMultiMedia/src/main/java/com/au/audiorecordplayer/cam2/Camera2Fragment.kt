package com.au.audiorecordplayer.cam2

import android.Manifest
import android.os.Bundle
import android.os.HandlerThread
import android.os.SystemClock
import androidx.lifecycle.lifecycleScope
import com.au.audiorecordplayer.cam2.base.IRecordCallback
import com.au.audiorecordplayer.cam2.base.ITakePictureCallback
import com.au.audiorecordplayer.cam2.bean.TakePictureCallbackWrap
import com.au.audiorecordplayer.cam2.impl.MyCamManager
import com.au.audiorecordplayer.cam2.impl.MyCamManager.Companion.TRANSMIT_TO_MODE_PICTURE_PREVIEW
import com.au.audiorecordplayer.cam2.impl.MyCamManager.Companion.TRANSMIT_TO_MODE_PREVIEW
import com.au.audiorecordplayer.databinding.FragmentCamera2Binding
import com.au.audiorecordplayer.util.FileUtil
import com.au.audiorecordplayer.util.MainUIManager
import com.au.module_android.Globals
import com.au.module_android.click.onClick
import com.au.module_android.permissions.createMultiPermissionForResult
import com.au.module_android.ui.bindings.BindingFragment
import com.au.module_android.utils.gone
import com.au.module_android.utils.visible
import com.au.module_androidui.toast.ToastUtil.toastOnTop
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class Camera2Fragment : BindingFragment<FragmentCamera2Binding>() {
    override fun isPaddingStatusBar(): Boolean {
        return false
    }

    lateinit var camManager:MyCamManager
    private var mSubThread: HandlerThread? = null

    val permissions = arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO)
    private val permissionHelper = createMultiPermissionForResult(permissions)

    fun openCameraSafety() {
        permissionHelper.safeRun({
            camManager.openCamera()
        }, notGivePermissionBlock = {
            MainUIManager.get().toastSnackbar(view, "请授予相机和录音权限。")
        })
    }

    override fun onBindingCreated(savedInstanceState: Bundle?) {
        val subThread = HandlerThread("Camera-thread")
        mSubThread = subThread
        subThread.start()
        camManager = MyCamManager(requireActivity(),
            binding.previewView,
            MyCamManager.TRANSMIT_TO_MODE_PREVIEW,
            subThread.looper)

        camManager.openCameraSafety = {openCameraSafety()}
        camManager.modeLiveData.observe(viewLifecycleOwner) {
            binding.modeTv.text = it
        }
        camManager.toastCallback = { str->
            lifecycleScope.launch {
                binding.modeTv.text = camManager.modeLiveData.realValue + "\n" + str
                delay(3000)
                binding.modeTv.text = camManager.modeLiveData.realValue
            }
        }

        binding.previewView.setCallback(camManager.previewViewCallback)

        binding.takePicBtn.onClick {
            camManager.takePicture(TakePictureCallbackWrap(
                Globals.goodFilesDir.absolutePath + "/pictures", "PIC_" + FileUtil.longTimeToStr(System.currentTimeMillis()) + ".jpg",
                object : ITakePictureCallback {
                    override fun onPictureToken(path: String) {
                        MainUIManager.get().toastSnackbar(view, "图片保存在：$path")
                    }
                }))
        }

        binding.recordBtn.onClick {
            if (!isRecording) {
                isRecording = true
                camManager.startRecord(object : IRecordCallback {
                    override fun onRecordStart(suc: Boolean) {
                        isRecording = true
                        lifecycleScope.launch {
                            toastOnTop("录制开始")
                            mRunnableLastTime = 0
                            mRunnableIndex = 0
                            binding.recordBtn.text = "stop record"
                            binding.timeTv.visible()
                            binding.timeTv.handler.post(mTimeUpdateRunnable)
                        }
                    }

                    override fun onRecordEnd(path: String) {
                        isRecording = false
                        lifecycleScope.launch {
                            toastOnTop("视频保存在：$path", duration = 6000)
                            binding.timeTv.handler.removeCallbacks(mTimeUpdateRunnable)
                            mTimeSec = 0
                            binding.recordBtn.text = "start record"
                            binding.timeTv.gone()
                        }
                    }

                    override fun onRecordFailed(err: Int) {
                        toastOnTop("录制视频失败!", icon = "error")
                    }
                })
            } else {
                camManager.stopRecord()
            }
        }

        binding.modeTv.onClick {
            val currentMode = camManager.modeLiveData.realValue
            when (currentMode) {
                camManager.constStatePreview -> {
                    camManager.sendEmptyMessage(TRANSMIT_TO_MODE_PICTURE_PREVIEW)
                }
                camManager.constStatePictureAndPreview -> {
                    camManager.sendEmptyMessage(TRANSMIT_TO_MODE_PREVIEW)
                }
                else -> {
                    camManager.toastCallback?.invoke("当前模式不支持切换")
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        camManager.closeCameraDirectly()
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
            binding.timeTv.text = String.format("rec: %d", mTimeSec++)
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
}