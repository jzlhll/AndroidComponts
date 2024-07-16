package com.au.jobstudy.checkwith

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.View
import com.au.jobstudy.check.bean.WorkEntity
import com.au.jobstudy.check.modes.MediaType
import com.au.jobstudy.databinding.FragmentCheckInBinding
import com.au.module_android.ui.FragmentRootActivity
import com.au.module_android.ui.bindings.BindingParamsFragment
import com.au.module_android.utils.visible

/**
 * @author au
 * @date :2023/12/29 17:04
 * @description:
 */
class CheckWithFragment : BindingParamsFragment<FragmentCheckInBinding>() {
    companion object {
        fun start(context: Context, dataItem: WorkEntity) {
            putTempParams(CheckWithFragment::class.java, "dataItem" to dataItem)
            FragmentRootActivity.start(context, CheckWithFragment::class.java)
        }
    }

    private val dataItem = getTempParams<WorkEntity>("dataItem")!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.subjectText.text = dataItem.subject
        binding.descText.text = dataItem.desc
        binding.subjectColor.setBackgroundColor(Color.parseColor(dataItem.colorStr))

        if (dataItem.checkModes.size > 1) {
            binding.checkupText.text = "请选择一种方式上传："
        }

        dataItem.checkModes.forEach {
            if (it.mediaType == MediaType.TYPE_PIC) {
                binding.checkUpModePicture.visible()
            } else if (it.mediaType == MediaType.TYPE_VIDEO) {
                binding.checkupModeVideo.visible()
            } else if (it.mediaType == MediaType.TYPE_PARENT) {
                binding.checkupModeParent.visible()
            } else if (it.mediaType == MediaType.TYPE_VOICE) {
                binding.checkupModeVoice.visible()
            }
        }
    }
}