package com.allan.autoclickfloat.screen

import android.app.Activity.RESULT_OK
import android.content.Context
import android.media.projection.MediaProjectionManager
import android.os.Bundle
import android.view.View
import com.allan.autoclickfloat.databinding.FragmentHongbaoScreenBinding
import com.au.module_android.click.onClick
import com.au.module_android.permissions.createActivityForResult
import com.au.module_android.ui.bindings.BindingFragment
import com.au.module_androidui.toast.toastOnTop

class HongbaoScreenDetectFragment : BindingFragment<FragmentHongbaoScreenBinding>() {
    private var mediaProjectionManager:MediaProjectionManager? = null

    private val activityForResult = createActivityForResult()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.startScreenDetectBtn.onClick {
            // 创建一个用于请求录屏权限的Intent
            val projectionManager = requireContext().getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
            mediaProjectionManager = projectionManager
            val captureIntent = projectionManager.createScreenCaptureIntent()

            // 请求录屏权限
            activityForResult.start(captureIntent, null) {result->
                if (result.resultCode == RESULT_OK) {
                    // 用户已授权，可以开始录屏
                    val data = result.data
                    // 获取录屏权限的结果数据
                    val mgr = mediaProjectionManager
                    if (data != null && mgr != null) {
                        val projection = mgr.getMediaProjection(result.resultCode, data)
                        ScreenDetectService.start(requireContext(), projection)
                    }
                } else {
                    // 用户未授权
                    toastOnTop("没有授予录屏权限。")
                }
            }
        }
    }
}