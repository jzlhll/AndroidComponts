package com.au.jobstudy.checkwith

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.lifecycle.lifecycleScope
import com.au.jobstudy.check.CheckConsts
import com.au.jobstudy.check.bean.CompletedEntity
import com.au.jobstudy.check.bean.WorkEntity
import com.au.jobstudy.check.modes.CheckMode
import com.au.jobstudy.check.modes.MediaType
import com.au.jobstudy.checkwith.base.FirstResumeBindingFragment
import com.au.jobstudy.checkwith.parent.CheckParentPartialFragment
import com.au.jobstudy.checkwith.pic.CheckPicturePartialFragment
import com.au.jobstudy.checkwith.video.CheckVideoPartialFragment
import com.au.jobstudy.databinding.FragmentCheckInBinding
import com.au.module_android.click.onClick
import com.au.module_android.ui.FragmentRootActivity
import com.au.module_android.ui.bindings.BindingFragment
import com.au.module_android.utils.invisible
import com.au.module_android.utils.launchOnThread
import com.au.module_android.utils.replaceFragment
import com.au.module_android.utils.visible
import java.util.stream.Collectors

/**
 * @author au
 * @date :2023/12/29 17:04
 * @description:
 */
class CheckWithFragment : BindingFragment<FragmentCheckInBinding>() {
    companion object {
        private var sDataItem:WorkEntity? = null

        fun start(context: Context, dataItem: WorkEntity) {
            this.sDataItem = dataItem
            FragmentRootActivity.start(context, CheckWithFragment::class.java, autoHideIme = true)
        }
    }

    private val dataItem = sDataItem!!

    private var mCheckMode:CheckMode? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.subjectText.text = dataItem.subject
        binding.descText.text = dataItem.desc
        binding.subjectColor.setBackgroundColor(Color.parseColor(dataItem.colorStr))

        binding.submitButton.onClick {
            lifecycleScope.launchOnThread {
                CheckConsts.markCompleted(CompletedEntity(dataItem.id, dataItem.day, dataItem.weekStartDay, partialFragment?.getUploadFiles(), 0))
                requireActivity().finishAfterTransition()
            }
        }

        if (dataItem.checkModes.size > 1) {
            binding.checkupText.text =
                "请选择一种方式上传：" + "\n" +
                        dataItem.checkModes.map { it.desc }.stream().collect(Collectors.joining("\n"))
        } else if(dataItem.checkModes.size == 1) {
            binding.checkupText.text = dataItem.checkModes[0].desc
        }

        val isOnlyOne = dataItem.checkModes.size == 1

        dataItem.checkModes.forEach {
            when (it.mediaType) {
                MediaType.TYPE_PIC -> {
                    binding.checkUpModePicture.visible()
                    binding.checkUpModePicture.tag = it
                    binding.checkUpModePicture.onClick { v->
                        mCheckMode = v.tag as CheckMode
                        addPicPartial(v.tag as CheckMode)
                    }
                    if (isOnlyOne) {
                        addPicPartial(it)
                    }
                }
                MediaType.TYPE_VIDEO -> {
                    binding.checkupModeVideo.visible()
                    binding.checkupModeVideo.tag = it
                    binding.checkupModeVideo.onClick { v->
                        mCheckMode = v.tag as CheckMode
                        addVideoPartial(v.tag as CheckMode)
                    }
                }
                MediaType.TYPE_PARENT -> {
                    binding.checkupModeParent.visible()
                    binding.checkupModeParent.tag = it
                    binding.checkupModeParent.onClick { v->
                        mCheckMode = v.tag as CheckMode
                        addParentPartial(v.tag as CheckMode)
                    }
                }
                MediaType.TYPE_VOICE -> {
                    binding.checkupModeVoice.visible()
                    binding.checkupModeVoice.tag = it
                    binding.checkupModeVoice.onClick { v->
                        mCheckMode = v.tag as CheckMode
//                        addVoicePartial(v.tag as CheckMode)
                    }
                    if (isOnlyOne) {
//                        addVoicePartial(it)
                    }
                }
                MediaType.TYPE_BELIEVE -> {
                    mCheckMode = it
                    binding.submitButton.visible()
                }
            }
        }
    }

    private var partialFragment:FirstResumeBindingFragment<*>? = null

    private fun addPicPartial(cm:CheckMode) {
        if (partialFragment == null) {
            val f = CheckPicturePartialFragment().apply {
                checkMode = cm
            }

            partialFragment = f
            replaceFragment(binding.fragmentContainerView.id, f)
            binding.checkUpModePicture.invisible()
        }
    }

    private fun addParentPartial(cm:CheckMode) {
        val f = CheckParentPartialFragment()
        partialFragment = f
        replaceFragment(binding.fragmentContainerView.id, f)
        binding.checkupModeParent.invisible()
    }

    private fun addVideoPartial(cm:CheckMode) {
        val f = CheckVideoPartialFragment(cm.max).also {
            it.firstStartCallback = {
                it.startTake()
            }
        }
        partialFragment = f
        replaceFragment(binding.fragmentContainerView.id, f)
        binding.checkupModeVideo.invisible()
    }

    fun changeUploadIcon(show:Boolean) {
        binding.submitButton.visibility = if (show) View.VISIBLE else View.GONE
    }

    fun showVideoBtn() {
        binding.checkupModeVideo.visible()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        partialFragment?.clearNoUsedFile()
    }
}