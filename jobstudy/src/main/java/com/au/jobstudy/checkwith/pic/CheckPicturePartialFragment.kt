package com.au.jobstudy.checkwith.pic

import android.os.Bundle
import androidx.recyclerview.widget.GridLayoutManager
import com.au.module_nested.decoration.GridMultiDownItemDecoration
import com.au.module_nested.recyclerview.DataExtraInfo
import com.au.module_nested.recyclerview.IOnChangeListener
import com.au.jobstudy.check.modes.CheckMode
import com.au.jobstudy.checkwith.CheckWithFragment
import com.au.jobstudy.checkwith.base.FirstResumeBindingFragment
import com.au.jobstudy.databinding.PartialPicturesBinding
import com.au.module_android.utils.asOrNull
import com.au.module_android.utils.dp
import java.io.File

/**
 * @author allan
 * @date :2024/7/15 19:36
 * @description:
 */
class CheckPicturePartialFragment : FirstResumeBindingFragment<PartialPicturesBinding>() {
    val selector = CheckParentPicSelector(this)

    var checkMode:CheckMode? = null

    lateinit var adapter:PicAdapter
    override fun getUploadFiles(): List<String> {
        return adapter.datas.filter { it.file != null }.map { it.file!!.absolutePath }
    }

    override fun onBindingCreated(savedInstanceState: Bundle?) {
        adapter = selector.createAdapter()
        adapter.addDataChanged(object : IOnChangeListener {
            override fun onChange(info: DataExtraInfo) {
                if (adapter.datas.size > 1 || (adapter.datas.size == 1 && !adapter.datas[0].isAdd)) {
                    parentFragment.asOrNull<CheckWithFragment>()?.changeUploadIcon(true)
                } else {
                    parentFragment.asOrNull<CheckWithFragment>()?.changeUploadIcon(false)
                }
            }
        })

        binding.rcv.layoutManager = GridLayoutManager(requireContext(), 3)
        binding.rcv.addItemDecoration(GridMultiDownItemDecoration(0.dp, 6.dp, 3))
        binding.rcv.setHasFixedSize(true)

        binding.rcv.adapter = adapter
        adapter.submitList(mutableListOf(Bean.ADD_BEAN), false)
    }

    override fun usedFiles(): List<File> {
        return adapter.datas.filter { it.file != null }.map { it.file!! }
    }
}