package com.au.jobstudy.checkwith.parent

import android.os.Bundle
import com.au.jobstudy.checkwith.base.FirstResumeBindingFragment
import com.au.jobstudy.databinding.PartialParentBinding
import java.io.File

/**
 * @author allan
 * @date :2024/7/15 19:36
 * @description:
 */
class CheckParentPartialFragment : FirstResumeBindingFragment<PartialParentBinding>() {
    private var codesManager: CodesManager? = null

    val parentCode = "192837"

    override fun onBindingCreated(savedInstanceState: Bundle?) {
        codesManager = CodesManager(requireActivity(),
            arrayOf(binding.code1, binding.code2, binding.code3, binding.code4, binding.code5, binding.code6),
            binding.hiddenEdit)
        codesManager!!.allEnterCodeListener = {
            if (it == parentCode) {
                checkWithFragment?.changeUploadIcon(true)
            }
        }
    }

    override fun getUploadFiles(): String {
        return ""
    }

    override fun usedFiles(): List<File> {
        return listOf()
    }
}