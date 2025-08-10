package com.au.audiorecordplayer.cam1

import android.Manifest
import android.hardware.Camera
import android.hardware.Camera.PictureCallback
import android.media.CamcorderProfile
import android.media.MediaRecorder
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.SurfaceHolder
import android.widget.Toast
import com.au.audiorecordplayer.databinding.FragmentCamera1Binding
import com.au.audiorecordplayer.util.MainUIManager
import com.au.module_android.Globals
import com.au.module_android.permissions.createMultiPermissionForResult
import com.au.module_android.ui.bindings.BindingFragment
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import kotlin.math.abs

class Camera1Fragment : BindingFragment<FragmentCamera1Binding>(), SurfaceHolder.Callback {
    private val TAG = "camera1"

    val permissions = arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO)
    private val permissionHelper = createMultiPermissionForResult(permissions)

    private var mCamera: Camera? = null
    private var mHolder: SurfaceHolder? = null
    private var mMediaRecorder: MediaRecorder? = null
    private var isRecording = false
    private var isPreviewing = false

    override fun isPaddingStatusBar(): Boolean {
        return false
    }

    override fun onBindingCreated(savedInstanceState: Bundle?) {
        permissionHelper.safeRun({
            initCamera()
        },
            {
                MainUIManager.get().toastSnackbar(binding.root, "请授予相机和录音权限")
            })
    }

    private fun initCamera() {
        Log.w(TAG, "init camera")
        mHolder = binding.surfaceView.holder.apply {
            addCallback(this@Camera1Fragment)
        }

        binding.takePicBtn.setOnClickListener { v ->
            if (isPreviewing) {
                capturePhoto()
            }
        }

        binding.recordBtn.setOnClickListener { v ->
            if (isRecording) {
                stopRecording()
                binding.recordBtn.text = "开始录像"
            } else {
                if (prepareMediaRecorder()) {
                    startRecording()
                    binding.recordBtn.text = "停止录像"
                }
            }
        }
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        if (mHolder?.surface == null) return
        Log.w(TAG, "surface changed ")
        mCamera?.let { camera->
            try {
                camera.stopPreview()
                setupCameraParameters()
                camera.setPreviewDisplay(mHolder)
                camera.startPreview()
            } catch (e: Exception) {
                Log.e(TAG, "重启预览失败: " + e.message)
            }
        }
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        Log.w(TAG, "surface Created $holder")
        try {
            mCamera = Camera.open(Camera.CameraInfo.CAMERA_FACING_BACK).apply {
                setDisplayOrientation(90)
            }
            Log.w(TAG, "open camera $mCamera")
            isPreviewing = true
        } catch (e: IOException) {
            Log.e(TAG, "设置预览失败: " + e.message)
        }
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        releaseCamera()
        releaseMediaRecorder()
    }

    private fun setupCameraParameters() {
        val cam = mCamera!!

        val params = cam.getParameters()
        // 设置对焦模式
        val focusModes = params.getSupportedFocusModes()
        if (focusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
            params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)
        }

//        val sizes = params.getSupportedPreviewSizes()
//        getOptimalPreviewSize(sizes, binding.surfaceView.width, binding.surfaceView.height)?.let { optimalSize->
//            params.setPreviewSize(optimalSize.width, optimalSize.height)
//        }

        // 设置图片尺寸
//        val pictureSizes = params.getSupportedPictureSizes()
//        val largestSize = pictureSizes[0]
//        params.setPictureSize(largestSize.width, largestSize.height)

        cam.setParameters(params)
    }

    private fun getOptimalPreviewSize(sizes: MutableList<Camera.Size>?, w: Int, h: Int): Camera.Size? {
        val ASPECT_TOLERANCE = 0.1
        val targetRatio = w.toDouble() / h

        if (sizes == null) return null

        var optimalSize: Camera.Size? = null
        var minDiff = Double.Companion.MAX_VALUE

        for (size in sizes) {
            val ratio = size.width.toDouble() / size.height
            if (abs(ratio - targetRatio) > ASPECT_TOLERANCE) continue

            if (abs(size.height - h) < minDiff) {
                optimalSize = size
                minDiff = abs(size.height - h).toDouble()
            }
        }

        if (optimalSize == null) {
            minDiff = Double.Companion.MAX_VALUE
            for (size in sizes) {
                if (abs(size.height - h) < minDiff) {
                    optimalSize = size
                    minDiff = abs(size.height - h).toDouble()
                }
            }
        }
        return optimalSize!!
    }

    private fun capturePhoto() {
        mCamera!!.takePicture(null, null, PictureCallback { data: ByteArray?, camera: Camera? ->
            val pictureFile: File? = getOutputMediaFile(MEDIA_TYPE_IMAGE)
            if (pictureFile == null) {
                Log.e(TAG, "创建图片文件失败")
                return@PictureCallback
            }

            try {
                FileOutputStream(pictureFile).use { fos ->
                    fos.write(data)
                    Toast.makeText(requireContext(), "照片已保存: " + pictureFile.getPath(), Toast.LENGTH_SHORT).show()
                }
            } catch (e: IOException) {
                Log.e(TAG, "保存照片失败: " + e.message)
            }
            camera!!.startPreview()
        })
    }

    private fun prepareMediaRecorder(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            mMediaRecorder = MediaRecorder(requireContext())
        } else {
            mMediaRecorder = MediaRecorder()
        }
        // Step 1: 解锁相机并设置给MediaRecorder
        mCamera!!.unlock()

        val mMediaRecorder = this.mMediaRecorder!!

        mMediaRecorder.setCamera(mCamera)

        // Step 2: 设置音视频源
        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER)
        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA)

        // Step 3: 设置配置文件
        mMediaRecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH))

        // Step 4: 设置输出文件
        mMediaRecorder.setOutputFile(getOutputMediaFile(MEDIA_TYPE_VIDEO).toString())

        // Step 5: 设置预览显示
        mMediaRecorder.setPreviewDisplay(mHolder!!.surface)

        try {
            mMediaRecorder.prepare()
            return true
        } catch (e: IOException) {
            Log.e(TAG, "MediaRecorder准备失败: " + e.message)
            releaseMediaRecorder()
            return false
        }
    }

    private fun startRecording() {
        try {
            mMediaRecorder!!.start()
            isRecording = true
        } catch (e: RuntimeException) {
            Log.e(TAG, "开始录像失败: " + e.message)
            releaseMediaRecorder()
        }
    }

    private fun stopRecording() {
        if (isRecording) {
            try {
                mMediaRecorder!!.stop()
                Toast.makeText(requireContext(), "视频已保存", Toast.LENGTH_SHORT).show()
            } catch (e: RuntimeException) {
                Log.e(TAG, "停止录像失败: " + e.message)
            }
            releaseMediaRecorder()
            mCamera!!.lock()
            isRecording = false
        }
    }

    private fun releaseCamera() {
        if (mCamera != null) {
            mCamera!!.release()
            mCamera = null
            isPreviewing = false
        }
    }

    private fun releaseMediaRecorder() {
        if (mMediaRecorder != null) {
            mMediaRecorder!!.reset()
            mMediaRecorder!!.release()
            mMediaRecorder = null
        }
    }

    val MEDIA_TYPE_IMAGE: Int = 1
    val MEDIA_TYPE_VIDEO: Int = 2

    private fun getOutputMediaFile(type: Int): File? {
        val timeStamp: String? = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val mediaFile: File?

        val mediaStorageDir = Globals.goodFilesDir
        if (type == MEDIA_TYPE_IMAGE) {
            mediaFile = File(
                mediaStorageDir.getPath() + File.separator +
                        "IMG_" + timeStamp + ".jpg"
            )
        } else if (type == MEDIA_TYPE_VIDEO) {
            mediaFile = File(
                mediaStorageDir.getPath() + File.separator +
                        "VID_" + timeStamp + ".mp4"
            )
        } else {
            return null
        }

        return mediaFile
    }
}