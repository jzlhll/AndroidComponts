package com.au.audiorecordplayer

import android.os.Bundle
import android.widget.SeekBar
import android.widget.TextView
import com.au.audiorecordplayer.databinding.AudioPlaysBinding
import com.au.audiorecordplayer.player.audiotracker.MyAudioTracker
import com.au.audiorecordplayer.player.mediaplayer.MyMediaPlayer
import com.au.audiorecordplayer.player.mediaplayer.MyMediaPlayerController
import com.au.audiorecordplayer.player.soudpool.MySoundPool
import com.au.audiorecordplayer.recorder.PCMAndWavUtil
import com.au.audiorecordplayer.util.CacheFileGenerator
import com.au.audiorecordplayer.util.MainUIManager
import com.au.audiorecordplayer.util.MyLog
import com.au.module_android.click.onClick
import com.au.module_android.ui.bindings.BindingFragment
import com.au.module_android.ui.views.ToolbarInfo
import java.io.File


class AudioPlayerTestFragment : BindingFragment<AudioPlaysBinding>() {
    private lateinit var myMediaPlayerController: MyMediaPlayerController
    var mySoundPool: MySoundPool? = null
    private var mAudioTracker: MyAudioTracker? = null

    private var mAudioFile: File? = null

    override fun onBindingCreated(savedInstanceState: Bundle?) {
        myMediaPlayerController = MyMediaPlayerController(
            binding.mediaPlayerSeekBar as SeekBar?,
            binding.mediaPlayerCurrentText as TextView?
        )

        initSoundPoolClicks()
        initMediaPlayerClicks()
        initAudioTrackClicks()

        scanShowFileList()

        binding.roundOneAudioFileBtn.onClick {
            mAudioFile = randomAudioFile()
            binding.selectedFileTextView.text = mAudioFile?.name
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mySoundPool?.release()
        myMediaPlayerController.close()
        mAudioTracker?.release()
    }

    private fun initAudioTrackClicks() {
        binding.audioTrackTestGetPcmInfoBtn.onClick { v->
            runCatching {
                val f = mAudioFile
                if (f != null) {
                    PCMAndWavUtil.getInfo(f.absolutePath)
                } else {
                    throw Exception("No audio file selected.")
                }
            }.onFailure {
                MyLog.ex(it)
                MainUIManager.get().toastSnackbar(v, it.message)
            }.onSuccess { pcmInfo->
                MyLog.d( pcmInfo.toString())
                MainUIManager.get().toastSnackbar(v, pcmInfo.toString())
            }
        }

        binding.audioTrackPlayBtn.onClick { v->
            if (mAudioTracker == null) {
                mAudioTracker = MyAudioTracker()
            }
            val r = mAudioTracker?.play(
                requireContext(),
                mAudioFile?.absolutePath) {
                MainUIManager.get().toastSnackbar(v, "Selected File is not PCM.")
            }
            when (r) {
                -1 -> MainUIManager.get().toastSnackbar(v, "当前状态不应该点击。")
            }
        }
        binding.audioTrackStopBtn.onClick {
            mAudioTracker?.stop()
        }
        binding.audioTrackResumeBtn.onClick {
            mAudioTracker?.resume()
        }
        binding.audioTrackPauseBtn.onClick {
            mAudioTracker?.pause()
        }
    }

    private fun initSoundPoolClicks() {
        binding.soundPoolBtn.onClick {
            val randomId = (Math.random() * 3).toInt()
            if (mySoundPool == null) {
                mySoundPool = MySoundPool(requireContext())
            }
            val sp = mySoundPool!!
            when (randomId) {
                0 -> sp.play(sp.mSoundEffectPaopaoId)
                1 -> sp.play(sp.mSoundEffectQiuId)
                2 -> MySoundPool.play(requireContext())
            }
        }
    }

    private fun initMediaPlayerClicks() {
        binding.mediaPlayerStartBtn.onClick {
            val path = mAudioFile?.absolutePath
            if (path == null) {
                MainUIManager.get().toastSnackbar(it, "No audio file selected.")
                return@onClick
            }

            val player = myMediaPlayerController.mediaPlayer ?: MyMediaPlayer()
            if (myMediaPlayerController.mediaPlayer == null) {
                myMediaPlayerController.setMediaPlayer(player)
            }
            try {
                player.start(path)
            } catch (e: Exception) {
                MainUIManager.get().toastSnackbar(it, e.toString())
            }
        }

        binding.mediaPlayerPauseBtn.onClick {
            myMediaPlayerController.mediaPlayer?.pause()
        }
        binding.mediaPlayerStopBtn.onClick {
            myMediaPlayerController.mediaPlayer?.stop()
        }
        binding.mediaPlayerResumeBtn.onClick {
            myMediaPlayerController.mediaPlayer?.resume()
        }
    }

    private fun scanShowFileList() {
        val dir = CacheFileGenerator.cacheFilePath()
        val dirFile = File(dir)
        val files = dirFile.listFiles()
        val sb = StringBuilder()
        files?.forEach {
            sb.append(it.name).append(" ").append(it.length()).append("\n")
        }
        if (sb.isEmpty()) {
            sb.append("No files.")
        }
        binding.fileListTextView.text = sb
    }

    private fun randomAudioFile(): File? {
        val dir = CacheFileGenerator.cacheFilePath()
        val dirFile = File(dir)
        val files = dirFile.listFiles()
        if (files.isNullOrEmpty()) {
            return null
        }
        var randomId = (Math.random() * files.size).toInt()
        var maxWhileCount = 100
        while(files[randomId].name.endsWith("pcm") && maxWhileCount > 0) {
            randomId = (Math.random() * files.size).toInt()
            maxWhileCount--
        }
        return files[randomId]
    }

    override fun toolbarInfo(): ToolbarInfo? {
        return ToolbarInfo("AudioPlayers")
    }
}