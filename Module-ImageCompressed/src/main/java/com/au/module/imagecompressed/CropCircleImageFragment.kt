package com.au.module.imagecompressed

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import com.au.module_android.Globals
import com.au.module_android.Globals.getColor
import com.au.module_android.click.onClick
import com.au.module_android.ui.FragmentRootActivity
import com.au.module_android.ui.bindings.BindingFragment
import com.au.module_android.utils.logd
import com.au.module_imagecompressed.databinding.CropCircleLayoutBinding
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.yalantis.ucrop.UCrop
import com.yalantis.ucrop.UCropFragment
import com.yalantis.ucrop.UCropFragmentCallback
import com.yalantis.ucrop.UCropImageEngine

class CropCircleImageFragment : BindingFragment<CropCircleLayoutBinding>(), UCropFragmentCallback {
    companion object {
        fun startCrop(
            context: Context,
            srcUri: Uri,
            DestUri: Uri,
            dataSource: java.util.ArrayList<String>,
            activityResultCallback: ActivityResultCallback<ActivityResult>
        ) {
            // 1、构造可用的裁剪数据源
            val inputUri: Uri = srcUri
            val bundle = Bundle().apply {
                this.putString("inputUri", inputUri.toString())
                this.putString("destinationUri", DestUri.toString())
                this.putStringArrayList("dataCropSource", dataSource)
            }

            FragmentRootActivity.start(context,
                CropCircleImageFragment::class.java,
                arguments = bundle,
                activityResultCallback = activityResultCallback)
        }
    }

    private var uCropFragment:UCropFragment? = null
    private var dataCropSource:ArrayList<String>? = null

    override fun onBindingCreated(savedInstanceState: Bundle?) {
        dataCropSource = requireArguments().getStringArrayList("dataCropSource")
        if (dataCropSource.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "Error resource.", Toast.LENGTH_SHORT).show() //later: String
            requireActivity().finish()
            return
        }

        binding.saveBtn.onClick {
            uCropFragment?.cropAndSaveImage()
        }

        binding.fcv.post {
            resizeScreen()
            //replaceUcropFragment()
        }
    }

    private fun resizeScreen() {
        val fcv = binding.fcv
        logd { "rcv ${fcv.width} * ${fcv.height}" }
    }

    private fun replaceUcropFragment() {
        dataCropSource ?: return
        val outDir = Globals.cacheDir.absolutePath
        val uCrop = UCrop.of(
            Uri.parse(requireArguments().getString("inputUri")),
            Uri.parse(requireArguments().getString("destinationUri")),
            dataCropSource
        )
        uCrop.setImageEngine(MyUCropImageEngine())
        uCrop.withOptions(UCrop.Options().also {
            it.setCircleDimmedLayer(true)
            it.setHideBottomControls(true)
            it.setCompressionQuality(80)
            it.setShowCropFrame(false)
            it.withAspectRatio(1f, 1f)
            //it.setToolbarTitleSize(20)
            it.setCropOutputPathDir("$outDir/crop/")
            it.setShowCropGrid(false)
            it.setRootViewBackgroundColor(getColor(com.au.module_androidcolor.R.color.color_text_normal))
            it.setStatusBarColor(getColor(com.au.module_androidcolor.R.color.color_text_normal))
            it.isDarkStatusBarBlack(false)
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
        requireActivity().setResult(result.mResultCode, result.mResultData)
        requireActivity().finishAfterTransition()
    }

    class MyUCropImageEngine : UCropImageEngine {
        override fun loadImage(context: Context, url: String?, imageView: ImageView) {
            Glide.with(context).load(url).into(imageView);
        }

        override fun loadImage(
            context: Context,
            url: Uri?,
            maxWidth: Int,
            maxHeight: Int,
            call: UCropImageEngine.OnCallbackListener<Bitmap>?
        ) {
            Glide.with(context).asBitmap().override(android.R.attr.maxWidth, android.R.attr.maxHeight)
                .load(url).into(object : CustomTarget<Bitmap?>() {
                    override fun onResourceReady(
                        resource: Bitmap,
                        transition: Transition<in Bitmap?>?
                    ) {
                        call?.onCall(resource)
                    }

                    override fun onLoadFailed(errorDrawable: Drawable?) {
                        call?.onCall(null)
                    }

                    override fun onLoadCleared(placeholder: Drawable?) {}
                })
        }

    }
}