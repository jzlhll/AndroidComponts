package com.au.audiorecordplayer.cam2

import android.Manifest
import androidx.lifecycle.lifecycleScope
import com.au.audiorecordplayer.cam2.base.FeatureUtil
import com.au.audiorecordplayer.cam2.view.IViewStatusChangeCallback
import com.au.audiorecordplayer.util.CamLog
import com.au.audiorecordplayer.util.MainUIManager
import com.au.module_android.Globals
import com.au.module_android.permissions.createMultiPermissionForResult
import com.au.module_cached.delegate.SharedPrefStringCache
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class Camera2PreviewHelper(private val f : Camera2Fragment) : IViewStatusChangeCallback {
    private var mIsRealSurfaceCreated = false
    private val lastSaveMode by SharedPrefStringCache("lastSaveMode", "")

    val permissions = arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO)
    private val permissionHelper = f.createMultiPermissionForResult(permissions)

    init {
        f.binding.previewView.setCallback(this)

        f.lifecycleScope.launch {
            delay(100)
            transmitToWordsState(lastSaveMode)
        }
    }

    private fun convertWordsToTransmitId(words: String?): Int {
        if (words == null || words == FeatureUtil.MODE_PICTURE_NO_PREVIEW) {
            return MyCameraManager.TRANSMIT_TO_MODE_PREVIEW
        }

        //TODO 这个方法主要是给默认加载使用；录像一上来就当做预览即可
        if (words == FeatureUtil.MODE_PicturePreviewVideo
            || words == FeatureUtil.MODE_PREVIEW_PICTURE
        ) {
            return MyCameraManager.TRANSMIT_TO_MODE_PICTURE_PREVIEW
        }

        if (words == FeatureUtil.MODE_PREVIEW) {
            return MyCameraManager.TRANSMIT_TO_MODE_PREVIEW
        }

        return MyCameraManager.TRANSMIT_TO_MODE_PREVIEW //不存在或者修改了都复原
    }

    fun openCamera() {
        permissionHelper.safeRun({
            MyCameraManager.instance().openCamera()
        }, notGivePermissionBlock = {
            MainUIManager.get().toastSnackbar(f.view, "请授予相机和录音权限。")
        })
    }

    override fun onSurfaceCreated() {
        mIsRealSurfaceCreated = true
        val myCameraManager = MyCameraManager.instance() as MyCameraManager
        //这里进行强转。不应该使用。
        CamLog.d("Inited!!!!")
        myCameraManager.create(
            Globals.app, f.binding.previewView,
            MyCameraManager.TRANSMIT_TO_MODE_PICTURE_PREVIEW
        )
        myCameraManager.addModChanged(mainActivity)
        myCameraManager.addModChanged(this@FirstActivityCameraViewPresent)
        openCamera()
    }

    override fun onSurfaceDestroyed() {
    }

    override fun onSurfaceChanged() {
    }
}