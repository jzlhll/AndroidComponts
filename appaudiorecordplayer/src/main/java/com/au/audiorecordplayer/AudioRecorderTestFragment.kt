package com.au.audiorecordplayer

import android.media.AudioFormat
import android.os.Bundle
import android.view.View
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.au.audiorecordplayer.databinding.AudioRecordsBinding
import com.au.audiorecordplayer.recorder.IRecord
import com.au.audiorecordplayer.recorder.ISimpleRecord
import com.au.audiorecordplayer.recorder.a2AudioRecord.ResumeWavAudioRecord3_1
import com.au.audiorecordplayer.recorder.a1mediaRecord.MediaRecordAudio
import com.au.audiorecordplayer.util.CacheFileGenerator
import com.au.audiorecordplayer.recorder.a2AudioRecord.SimplePCMAudioRecord
import com.au.audiorecordplayer.recorder.a2AudioRecord.SimpleWavAudioRecord
import com.au.audiorecordplayer.recorder.a2AudioRecord.SimpleWavAudioRecord2_0
import com.au.audiorecordplayer.util.FileUtil
import com.au.audiorecordplayer.util.MainUIManager
import com.au.module_android.click.onClick
import com.au.module_android.permissions.createPermissionForResult
import com.au.module_android.ui.bindings.BindingFragment
import com.au.module_android.ui.views.ToolbarInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class AudioRecorderTestFragment : BindingFragment<AudioRecordsBinding>() {
    override fun toolbarInfo(): ToolbarInfo? {
        return ToolbarInfo(title = "Audio Recorders")
    }

    val permissionHelper = createPermissionForResult(android.Manifest.permission.RECORD_AUDIO)

    private val decorView : View
        get() = requireActivity().window.decorView

    var mRecord: ISimpleRecord? = null

    private fun hasRecordingAndInterrupt(): Boolean {
        val r = mRecord
        if (r != null && r.isRecording()) {
            MainUIManager.get().toastSnackbar(decorView, "录制正在进行，请停止！")
            return true
        }
        return false
    }

    private fun startRecord(record: ISimpleRecord) {
        permissionHelper.safeRun({
            runCatching {
                record.start()
            }.onFailure {
                MainUIManager.get().toastSnackbar(decorView, "开始失败-" + it.message)
            }
        }, notGivePermissionBlock = {
            MainUIManager.get().toastSnackbar(decorView, "没有授予权限")
        })
    }

    private fun stopRecord() {
        mRecord?.stop()
        MainUIManager.get().toastSnackbar(decorView, "录制已经停止")
    }

    private fun pauseRecord() {
        if (mRecord != null && mRecord is IRecord) {
            (mRecord as IRecord).pause()
            MainUIManager.get().toastSnackbar(decorView, "录制已经暂停")
        }
    }

    private fun initFirstLineButtons() {
        binding.buttonMediaStart.onClick {
            if (hasRecordingAndInterrupt()) {
                return@onClick
            }
            MediaRecordAudio().also {
                mRecord = it
                startRecord(it)
            }
        }
        binding.buttonMediaStop.onClick {
            stopRecord()
        }
        binding.buttonMediaPause.onClick {
            pauseRecord()
        }
        binding.buttonMediaResume.onClick {
            if (mRecord != null && mRecord is IRecord) {
                (mRecord as IRecord).resume()
            }
        }
    }
    private fun initSecondLineButtons() {
        binding.simplePCMRecordStartBtn.onClick {
            if (hasRecordingAndInterrupt()) {
                return@onClick
            }
            SimplePCMAudioRecord.SAMPLE_RATE = 16000
            SimplePCMAudioRecord.CANNEL_CONFIG = AudioFormat.CHANNEL_IN_STEREO
            SimplePCMAudioRecord.FORMAT = AudioFormat.ENCODING_PCM_16BIT
            SimplePCMAudioRecord().also {
                mRecord = it
                startRecord(it)
            }
        }
        binding.simplePCMWavStartBtn.onClick {
            if (hasRecordingAndInterrupt()) {
                return@onClick
            }
            SimpleWavAudioRecord().also {
                mRecord = it
                startRecord(it)
            }
        }
        binding.simplePCMRecordStopBtn.onClick {
            stopRecord()
        }
        binding.simplePCMWavStopBtn.onClick {
            stopRecord()
        }
    }


    private var mIndexOfDirectStart = -1
    private fun initThirdLineButtons() {
        binding.buttonDirectSelectParam.onClick {
            mIndexOfDirectStart++
            val infos = SimpleWavAudioRecord2_0.getAllPcmInfos()
            if (mIndexOfDirectStart == infos.size) {
                mIndexOfDirectStart = 0
            }
            binding.buttonDirectStart.isEnabled = true
            binding.buttonDirectStart.text = String.format("%s 开始", infos[mIndexOfDirectStart].mask)
        }
        binding.buttonDirectStart.onClick {
            if (mIndexOfDirectStart == -1) {
                MainUIManager.get().toastSnackbar(view, "参数错误！")
                return@onClick
            }
            if (hasRecordingAndInterrupt()) {
                return@onClick
            }
            val pcmInfo = SimpleWavAudioRecord2_0.getAllPcmInfos()[mIndexOfDirectStart]
            SimpleWavAudioRecord2_0(pcmInfo).also {
                mRecord = it
                startRecord(it)
            }
        }
        binding.buttonDirectStop.onClick {
            stopRecord()
        }
    }

    private fun initFourthLineButtons() {
        binding.buttonResumeStart.onClick {
            if (hasRecordingAndInterrupt()) {
                return@onClick
            }
            //3.1 文件拼接
            ResumeWavAudioRecord3_1().also {
                mRecord = it
                startRecord(it)
            }
        }
        binding.buttonResumeStop.onClick {
            stopRecord()
        }

        binding.buttonResumeResume.onClick {
            if (mRecord != null && mRecord is IRecord) {
                (mRecord as IRecord).resume()
            }
        }
        binding.buttonResumePause.onClick {
            pauseRecord()
        }
    }

    override fun onBindingCreated(savedInstanceState: Bundle?) {
        initFirstLineButtons()
        initSecondLineButtons()
        initThirdLineButtons()
        initFourthLineButtons()

        binding.fileObBtn.onClick {
            if (mRecord?.isRecording != true) {
                scanShowFileList()
            }
        }

        binding.deleteAllBtn.onClick {
            if (mRecord?.isRecording == true) return@onClick
            lifecycleScope.launch {
                withContext(Dispatchers.IO) {
                    val dir = CacheFileGenerator.cacheFilePath()
                    val dirFile = File(dir)
                    val files = dirFile.listFiles()
                    files?.forEach {
                        FileUtil.delete(it)
                    }
                }

                delay(100)
                scanShowFileList()
            }
        }

        viewLifecycleOwner.lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onPause(owner: LifecycleOwner) {
                super.onPause(owner)
                stopRecord()
            }
        })
    }

    private fun scanShowFileList() {
        val dir = CacheFileGenerator.cacheFilePath()
        val dirFile = File(dir)
        val files = dirFile.listFiles()
        val sb = StringBuilder()
        files?.forEach {
            sb.append(it.name).append(" ").append(it.length()).append("\n")
        }
        binding.fileObTextView.text = sb
    }
}