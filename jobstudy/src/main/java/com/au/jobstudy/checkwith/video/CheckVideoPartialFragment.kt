package com.au.jobstudy.checkwith.video

import android.os.Bundle
import androidx.core.content.FileProvider
import com.au.jobstudy.BuildConfig
import com.au.jobstudy.check.CheckConsts
import com.au.jobstudy.checkwith.base.FirstResumeBindingFragment
import com.au.jobstudy.databinding.PartialVideoBinding
import com.au.jobstudy.utils.WeekDateUtil
import com.au.module_android.Globals
import com.au.module_android.click.onClick
import com.au.module_android.permissions.activity.SystemTakeVideoFaceForResult
import com.au.module_android.permissions.systemTakeVideo2FrontForResult
import com.au.module_android.utils.gone
import com.au.module_android.utils.visible
import java.io.File
import java.util.Collections

/**
 * @author allan
 * @date :2024/7/15 19:36
 * @description:
 */
class CheckVideoPartialFragment : FirstResumeBindingFragment<PartialVideoBinding> {
    val forResult: SystemTakeVideoFaceForResult

    constructor():super() {
        forResult = systemTakeVideo2FrontForResult(isFront = true, maxSec = 120)
    }

    constructor(maxSec:Int):super() {
        forResult = systemTakeVideo2FrontForResult(isFront = true, maxSec = maxSec)
    }

    private var mFile: File? = null

    override fun onBindingCreated(savedInstanceState: Bundle?) {
        binding.ivDelete.onClick {
            checkWithFragment?.changeUploadIcon(false)
            checkWithFragment?.showVideoBtn()

            if (binding.videoView.isPlaying) {
                binding.videoView.stopPlayback()
            }
            binding.videoView.gone()
            binding.ivDelete.gone()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (binding.videoView.isPlaying) {
            binding.videoView.stopPlayback()
        }
    }

    fun startTake() {
        val picture = File(Globals.cacheDir.absolutePath + "/videos/" + CheckConsts.currentDay())
        picture.mkdirs()
        val file = File(picture, "vid_" + WeekDateUtil.currentHHmmssSSS() + ".mp4")
        val uri = FileProvider.getUriForFile(
            Globals.app,
            "${BuildConfig.APPLICATION_ID}.provider",
            file
        )

        mFile = file
        tempFiles.add(file)
        forResult.start(uri) {
            if (it) {
                binding.ivDelete.visible()
                binding.videoView.visible()
                binding.videoView.setVideoURI(uri)

                checkWithFragment?.changeUploadIcon(true)

                binding.videoView.onClick {
                    if (binding.videoView.isPlaying) {
                        binding.videoView.stopPlayback()
                    } else {
                        binding.videoView.start()
                    }
                }
            } else {
                checkWithFragment?.showVideoBtn()
            }
        }
    }

    override fun getUploadFiles(): List<String> {
        val f = mFile ?: return Collections.emptyList()
        return listOf(f.absolutePath)
    }

    override fun usedFiles(): List<File> {
        val f = mFile ?: return Collections.emptyList()
        return listOf(f)
    }
}