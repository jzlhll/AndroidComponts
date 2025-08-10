package com.au.audiorecordplayer.cam1

import android.Manifest
import android.graphics.SurfaceTexture
import android.hardware.Camera
import android.hardware.Camera.PictureCallback
import android.media.CamcorderProfile
import android.media.MediaRecorder
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Surface
import android.view.TextureView
import android.widget.Toast
import com.au.audiorecordplayer.databinding.FragmentCamera1TextureviewBinding
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


class Camera1TextureViewFragment : BindingFragment<FragmentCamera1TextureviewBinding>(), TextureView.SurfaceTextureListener {
    private val TAG = "camera1"

    val permissions = arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO)
    private val permissionHelper = createMultiPermissionForResult(permissions)

    private var mCamera: Camera? = null
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
        binding.textureView.surfaceTextureListener = this

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

    override fun onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int) {
        // SurfaceTexture可用时打开相机
        openCamera()
    }

    override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, width: Int, height: Int) {
        // 尺寸变化时调整相机预览
        configureCamera(width, height)
    }

    override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
        // 释放相机资源
        releaseCamera()
        releaseMediaRecorder()
        return true
    }

    override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {
        // 每一帧更新时调用，可用于实时图像处理
    }

    private fun openCamera() {
        try {
            mCamera = Camera.open(Camera.CameraInfo.CAMERA_FACING_BACK)
            val surface: SurfaceTexture? = binding.textureView.getSurfaceTexture()

            if (surface != null) {
                // 设置预览尺寸
                val params = mCamera!!.getParameters()
                val previewSize = getOptimalPreviewSize(
                    params.getSupportedPreviewSizes(),
                    binding.textureView.getWidth(),
                    binding.textureView.getHeight()
                )

                if (previewSize != null) {
                    params.setPreviewSize(previewSize.width, previewSize.height)
                }

                // 设置图片尺寸
                val pictureSize = getLargestPictureSize(params.getSupportedPictureSizes())
                if (pictureSize != null) {
                    params.setPictureSize(pictureSize.width, pictureSize.height)
                }

                // 设置对焦模式
                if (params.getSupportedFocusModes().contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
                    params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)
                }
                mCamera!!.setParameters(params)

                // 设置预览方向
                mCamera!!.setDisplayOrientation(getCameraDisplayOrientation())

                // 设置预览纹理
                mCamera!!.setPreviewTexture(surface)
                mCamera!!.startPreview()
                isPreviewing = true
            }
        } catch (e: IOException) {
            Log.e(TAG, "设置预览纹理失败: " + e.message)
        } catch (e: java.lang.RuntimeException) {
            Log.e(TAG, "相机打开失败: " + e.message)
        }
    }

    private fun configureCamera(width: Int, height: Int) {
        if (mCamera == null) return

        try {
            mCamera!!.stopPreview()
            val params = mCamera!!.getParameters()

            // 重新计算最佳预览尺寸
            val previewSize = getOptimalPreviewSize(
                params.getSupportedPreviewSizes(),
                width,
                height
            )

            if (previewSize != null) {
                params.setPreviewSize(previewSize.width, previewSize.height)
                mCamera!!.setParameters(params)
            }

            mCamera!!.setPreviewTexture(binding.textureView.getSurfaceTexture())
            mCamera!!.startPreview()
        } catch (e: IOException) {
            Log.e(TAG, "重新配置相机失败: " + e.message)
        }
    }

    private fun getCameraDisplayOrientation(): Int {
        val info = Camera.CameraInfo()
        Camera.getCameraInfo(Camera.CameraInfo.CAMERA_FACING_BACK, info)

        val rotation: Int = requireActivity().windowManager.getDefaultDisplay().getRotation()
        var degrees = 0

        when (rotation) {
            Surface.ROTATION_0 -> degrees = 0
            Surface.ROTATION_90 -> degrees = 90
            Surface.ROTATION_180 -> degrees = 180
            Surface.ROTATION_270 -> degrees = 270
        }

        var result: Int
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360
            result = (360 - result) % 360 // 补偿镜像
        } else {  // 后置摄像头
            result = (info.orientation - degrees + 360) % 360
        }

        return result
    }

    private fun getOptimalPreviewSize(sizes: MutableList<Camera.Size>?, width: Int, height: Int): Camera.Size? {
        val ASPECT_TOLERANCE = 0.1
        val targetRatio = width.toDouble() / height

        if (sizes == null) return null

        var optimalSize: Camera.Size? = null
        var minDiff = Double.Companion.MAX_VALUE

        for (size in sizes) {
            val ratio = size.width.toDouble() / size.height
            if (abs(ratio - targetRatio) > ASPECT_TOLERANCE) continue

            if (abs(size.height - height) < minDiff) {
                optimalSize = size
                minDiff = abs(size.height - height).toDouble()
            }
        }

        if (optimalSize == null) {
            minDiff = Double.Companion.MAX_VALUE
            for (size in sizes) {
                if (abs(size.height - height) < minDiff) {
                    optimalSize = size
                    minDiff = abs(size.height - height).toDouble()
                }
            }
        }
        return optimalSize
    }

    private fun getLargestPictureSize(sizes: MutableList<Camera.Size>?): Camera.Size? {
        if (sizes == null || sizes.isEmpty()) return null

        var largest = sizes.get(0)
        for (size in sizes) {
            if (size.width * size.height > largest.width * largest.height) {
                largest = size
            }
        }
        return largest
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
        mMediaRecorder.setPreviewDisplay(Surface(binding.textureView.surfaceTexture));

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