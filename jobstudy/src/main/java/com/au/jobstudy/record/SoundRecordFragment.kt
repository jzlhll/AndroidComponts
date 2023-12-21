package com.au.jobstudy.record

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import androidx.lifecycle.lifecycleScope
import com.au.jobstudy.consts.Dayer
import com.au.jobstudy.consts.WeekDateUtil
import com.au.jobstudy.databinding.FragmentSoundRecordBinding
import com.au.jobstudy.utils.secondToTimeMS
import com.au.module_android.Globals
import com.au.module_android.click.onClick
import com.au.module_android.permissions.activity.IActivityResult
import com.au.module_android.toast.toastOnTop
import com.au.module_android.ui.FragmentRootActivity
import com.au.module_android.ui.bindings.BindingFragment
import com.au.module_android.ui.bindings.BindingParamsFragment
import com.au.module_android.utils.launchOnUi
import com.au.multimedias.IRecord
import com.au.multimedias.MediaRecordAudio
import com.au.multimedias.createExternalFileName
import com.au.multimedias.createRecord
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.lang.ref.WeakReference
import kotlin.math.max
import kotlin.math.min

class SoundRecordFragment : BindingParamsFragment<FragmentSoundRecordBinding>() {
    companion object {
        private const val MSG_EVERY_SECOND = 100

        fun start(context: Context, activityResult: IActivityResult, minTs:Long, maxTs:Long) {
            putTempParams(SoundRecordFragment::class.java, "minTs" to minTs, "maxTs" to maxTs)
            activityResult.start()
            FragmentRootActivity.start(context, SoundRecordFragment::class.java)
        }
    }

    private var mediaRecord:IRecord? = null
    private var mFile:String? = null

    private var mStartTs = 0L
    private var mRecordSeconds = -1

    private val handler:MyHandler = MyHandler(WeakReference(this))

    private class MyHandler(val f:WeakReference<SoundRecordFragment>) : Handler(Looper.getMainLooper()) {
        private var currentSecond = 0

        override fun handleMessage(msg: Message) {
            when (msg.what) {
                MSG_EVERY_SECOND -> {
                    f.get()?.apply {
                        mRecordSeconds++
                        updateRecordTime()
                        val delayTs = mStartTs + (mRecordSeconds + 1) * 1000 - System.currentTimeMillis()
                        sendEmptyMessageDelayed(MSG_EVERY_SECOND, delayTs)
                    }
                }
            }
        }
    }

    override fun afterViewCreated(savedInstanceState: Bundle?, viewBinding: FragmentSoundRecordBinding) {

        viewBinding.recordBtn.onClick {
            if (mediaRecord == null) {
                mFile = createExternalFileName(Dayer().currentDay,
                    ("audio_" + WeekDateUtil.currentHHmmssSSS() + ".amr"))
                mediaRecord = createRecord(mFile!!)
            }

            if (mediaRecord!!.isRecording) {
                toastOnTop("不要重复点击开始录制哟！")
                return@onClick
            }

            binding.countDownView.startCountDown(3) {
                mediaRecord!!.start(Globals.app)
                mStartTs = System.currentTimeMillis()
                binding.timeLine.initColors(Color.GREEN, Color.CYAN)
                binding.timeLine.initProgress()
                handler.sendEmptyMessage(MSG_EVERY_SECOND)
            }
        }

        viewBinding.stopBtn.onClick {
            if (mediaRecord?.isRecording == true) {
                mediaRecord!!.stop()
            }
        }
    }

    override fun onStop() {
        super.onStop()
        binding.root.keepScreenOn = false
        mediaRecord?.let {
            if (it.isRecording) {
                if(it.supportResume) it.pause() else it.stop()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        binding.root.keepScreenOn = true
        mediaRecord?.let {
            if (it.isPaused) {
                it.resume(mFile!!)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mediaRecord?.stop()
    }

    private fun updateRecordTime() {
        val timeStr = secondToTimeMS(mRecordSeconds)
        binding.currentTime.text = timeStr
    }
}