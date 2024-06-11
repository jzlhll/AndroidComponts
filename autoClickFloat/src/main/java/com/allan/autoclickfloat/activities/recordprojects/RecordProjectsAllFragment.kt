package com.allan.autoclickfloat.activities.recordprojects

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.allan.autoclickfloat.databinding.RecordProjectsAllFragmentBinding
import com.au.module_android.click.onClick
import com.au.module_android.simplelivedata.Status
import com.au.module_android.ui.FragmentRootOrientationActivity
import com.au.module_android.ui.bindings.BindingFragment
import com.au.module_android.utils.gone
import com.au.module_android.utils.unsafeLazy
import com.au.module_android.utils.visible
import com.au.module_android.utils.visibleOrGone
import kotlinx.coroutines.flow.combine

/**
 * @author allan
 * @date :2024/4/23 17:46
 * @description:
 */
class RecordProjectsAllFragment : BindingFragment<RecordProjectsAllFragmentBinding>() {
    private val adapter = RecordProjectsAllAdapter {
        RecordOneProjectFragment.startAsEdit(requireActivity(), it.project.projectName, it.project.projectId)
    }

    private val viewModel by unsafeLazy { ViewModelProvider(requireActivity())[RecordProjectsAllViewModel::class.java] }

    private var isSelectedRemove = false
    private val redColorList by unsafeLazy {
        ColorStateList.valueOf(Color.RED)
    }
    private val blackColorList by unsafeLazy {
        ColorStateList.valueOf(Color.BLACK)
    }

    override fun onBindingCreated(savedInstanceState: Bundle?) {
        binding.removeMenuBtn.onClick {
            if(adapter.datas.size <= 0) return@onClick

            val remove = !isSelectedRemove
            isSelectedRemove = remove

            if (remove) {
                binding.removeMenuBtn.imageTintList = redColorList
            } else {
                binding.removeMenuBtn.imageTintList = blackColorList
            }

            //改变数组状态
            adapter.datas.forEach {
                it.isSelectMode = remove
            }

            adapter.notifyDataSetChanged()
        }

        binding.addMenuBtn.onClick {
            RecordOneProjectFragment.startAsAdd(requireActivity())
        }

        binding.rcv.adapter = adapter
        binding.rcv.layoutManager = LinearLayoutManager(binding.rcv.context)
        binding.rcv.setHasFixedSize(true)

        viewModel.allProjectsData.observe(viewLifecycleOwner) { list->
            if (list.isEmpty()) {
                requireActivity().finish()
                RecordOneProjectFragment.startAsAdd(requireActivity())
            } else {
                val mapList = list.map { RecordProjectsItemInfo(false, it) }
                adapter.submitList(mapList, false)
                binding.emptyText.visibleOrGone(list.isEmpty())
            }
        }

        viewModel.getAll()
    }
}