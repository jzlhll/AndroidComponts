package com.au.module_imagecompressed

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.RelativeLayout
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import com.au.module_android.Globals
import com.au.module_android.Globals.getColor
import com.au.module_android.click.onClick
import com.au.module_android.permissions.activity.ActivityForResult
import com.au.module_android.ui.FragmentRootActivity
import com.au.module_android.ui.bindings.BindingFragment
import com.au.module_android.utils.getScreenFullSize
import com.au.module_android.utils.gone
import com.au.module_android.utils.logd
import com.au.module_android.utils.visible
import com.au.module_imagecompressed.databinding.CropCircleLayoutBinding
import com.yalantis.ucrop.UCrop
import com.yalantis.ucrop.UCrop.EXTRA_OUTPUT_URI
import com.yalantis.ucrop.UCropFragment
import com.yalantis.ucrop.UCropFragmentCallback
import java.io.File

class CropCircleImageFragment : BindingFragment<CropCircleLayoutBinding>(), UCropFragmentCallback {
    companion object {
        const val DIR_CROP = "ucrop"
        const val RESULT_KEY_CROPPED_IMAGE = EXTRA_OUTPUT_URI

        const val RESULT_OK = 0
        const val RESULT_ERROR = -1

        fun startCropForResult(
            context: Context,
            activityResult: ActivityForResult,
            srcUri: Uri,
            activityResultCallback: ActivityResultCallback<ActivityResult>
        ) {
            // 1、构造可用的裁剪数据源
            val inputUri: Uri = srcUri
            val bundle = Bundle().apply {
                this.putString("inputUri", inputUri.toString())
            }

            FragmentRootActivity.startForResult(context,
                CropCircleImageFragment::class.java,
                activityResult,
                arguments = bundle,
                activityResultCallback = activityResultCallback)
        }
    }

    private var uCropFragment:UCropFragment? = null

    override fun onBindingCreated(savedInstanceState: Bundle?) {
        binding.back.onClick {
            requireActivity().finishAfterTransition()
        }
        binding.saveBtn.onClick {
            uCropFragment?.cropAndSaveImage()
        }

        val screenSize = requireActivity().getScreenFullSize()
        if (screenSize.second.toFloat() / screenSize.first < 1.5f) {
            binding.saveBtn.gone()
            binding.save2Btn.visible()
        }

        binding.fcv.post {
            resizeScreen()
            replaceUcropFragment()
        }
    }

    private fun resizeScreen() {
        if (binding.fcvHost.width > binding.fcvHost.height) {
            val oneSideDelta = (binding.fcvHost.width - binding.fcvHost.height) / 2
            binding.fcvHost.layoutParams = binding.fcvHost.layoutParams.also {
                it as RelativeLayout.LayoutParams
                it.marginStart = oneSideDelta
                it.marginEnd = oneSideDelta
            }
        }
    }

    private fun replaceUcropFragment() {
        val dir = File(Globals.goodCacheDir.absolutePath + "/ucrop")
        if (!dir.exists()) {
            dir.mkdir()
        }
        val destFile = File(Globals.goodCacheDir.absolutePath + "/ucrop", "tmp" + System.currentTimeMillis() + ".png")
        if (!destFile.exists()) {
            destFile.createNewFile()
        }
        val destUri = Uri.fromFile(destFile)
        val uCrop = UCrop.of(
            Uri.parse(requireArguments().getString("inputUri")),
            destUri,
        )
        //uCrop.setImageEngine(MyUCropImageEngine())
        uCrop.withOptions(UCrop.Options().also {
            it.withAspectRatio(1f, 1f)
            it.setCircleDimmedLayer(true)
            it.setHideBottomControls(true)
            it.setCompressionQuality(85)
            it.setShowCropFrame(true)
            //it.setToolbarTitleSize(20)
            it.setShowCropGrid(true)
            it.setRootViewBackgroundColor(getColor(com.au.module_androidcolor.R.color.windowBackground))
            //it.setToolbarTitle("Crop") //later: String
        })
        val uCropFragment = uCrop.fragment
        this.uCropFragment = uCropFragment
        childFragmentManager.beginTransaction()
            .replace(binding.fcv.id, uCropFragment)
            .commit()
    }

    override fun loadingProgress(showLoader: Boolean) {
        logd { "loadingprogress $showLoader" }
    }

    override fun onCropFinish(result: UCropFragment.UCropResult) {
        logd { "on crop finish ${result.mResultCode} ${result.mResultData}" }
        if (result.mResultCode == UCrop.RESULT_ERROR) {
            requireActivity().setResult(RESULT_ERROR, Intent())
        } else {
            requireActivity().setResult(RESULT_OK, result.mResultData)
        }
        requireActivity().finishAfterTransition()
    }
}