package com.au.jobstudy.record

import android.os.Bundle
import com.au.jobstudy.consts.Dayer
import com.au.jobstudy.consts.WeekDateUtil
import com.au.jobstudy.databinding.FragmentSoundRecordBinding
import com.au.module_android.Globals
import com.au.module_android.click.onClick
import com.au.module_android.toast.toastOnTop
import com.au.module_android.ui.bindings.BindingFragment
import com.au.multimedias.IRecord
import com.au.multimedias.createRecord

class SoundRecordFragment : BindingFragment<FragmentSoundRecordBinding>() {
    companion object {

    }

    private var mediaRecord:IRecord? = null

    override fun afterViewCreated(savedInstanceState: Bundle?, viewBinding: FragmentSoundRecordBinding) {
        viewBinding.recordBtn.onClick {
            if (mediaRecord == null) {
                mediaRecord = createRecord(
                    Dayer().currentDay,
                    ("record_" + WeekDateUtil.currentHHmmssSSS() + ".mp4"))
            }

            if (mediaRecord!!.isRecording) {
                toastOnTop("不要重复点击开始录制哟！")
                return@onClick
            }

            mediaRecord!!.start(Globals.app)
        }

        viewBinding.stopBtn.onClick {

        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mediaRecord?.stop()
    }
}