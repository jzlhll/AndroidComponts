package com.au.audiorecordplayer.camx

import android.Manifest
import android.os.Bundle
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import com.au.audiorecordplayer.databinding.FragmentCameraxBinding
import com.au.audiorecordplayer.util.MainUIManager
import com.au.module_android.Globals
import com.au.module_android.click.onClick
import com.au.module_android.permissions.createPermissionForResult
import com.au.module_android.ui.bindings.BindingFragment
import com.au.module_android.utils.unsafeLazy
import java.io.File

class CameraXFragment : BindingFragment<FragmentCameraxBinding>() {
    override fun isPaddingStatusBar() = false

    private val permissionHelper = createPermissionForResult(Manifest.permission.CAMERA)
    private val outputDirectory by unsafeLazy {
        val dir = Globals.goodFilesDir.absolutePath + File.separator + "CameraX"
        File(dir).apply {
            if (!exists()) mkdirs()
        }
    }

    override fun onBindingCreated(savedInstanceState: Bundle?) {
        permissionHelper.safeRun({
            startCamera()
        }, {
            MainUIManager.get().toastSnackbar(binding.root, "请允许相机权限")
        })

        binding.cameraBtn.onClick {
            takePhoto()
        }

    }

    private var imageCapture: ImageCapture? = null

    private fun takePhoto() {}

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())

        cameraProviderFuture.addListener({
            // Used to bind the lifecycle of cameras to the lifecycle owner
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            // Preview
            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(binding.viewFinder.surfaceProvider)
                }

            // Select back camera as a default
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                // Unbind use cases before rebinding
                cameraProvider.unbindAll()

                // Bind use cases to camera
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview)

            } catch(exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(requireContext()))
    }

    companion object {
        private const val TAG = "CameraXBasic"
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
        private const val REQUEST_CODE_PERMISSIONS = 10
    }
}