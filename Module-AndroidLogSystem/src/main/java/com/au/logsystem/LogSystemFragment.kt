package com.au.logsystem

import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.au.logsystem.databinding.FragmentLogSystemBinding
import com.au.module_android.click.onClick
import com.au.module_android.ui.bindings.BindingFragment
import com.au.module_android.ui.views.ToolbarInfo
import com.au.module_android.utils.FileLog
import com.au.module_android.utils.MediaHelper
import com.au.module_android.utils.gone
import com.au.module_android.utils.invisible
import com.au.module_android.utils.unsafeLazy
import com.au.module_android.utils.visible
import com.au.module_androidui.dialogs.ConfirmCenterDialog
import com.au.module_androidui.toast.toastOnTop
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class LogSystemFragment : BindingFragment<FragmentLogSystemBinding>() {
    override fun toolbarInfo(): ToolbarInfo? {
        return ToolbarInfo(title = "日志系统")
    }

    private val adapter = LogRcvAdapter()

    private var isSelectedMode = false

    private val viewModel by unsafeLazy { ViewModelProvider(requireActivity())[LogSystemViewModel::class.java] }

    override fun onDestroy() {
        super.onDestroy()
        FileLog.ignoreWrite = false
    }

    override fun onBindingCreated(savedInstanceState: Bundle?) {
        super.onBindingCreated(savedInstanceState)
        this.setTitle("日志系统")

        viewModel.scannedList.observe(viewLifecycleOwner) {
            binding.holdingView.gone()
            adapter.submitList(it, true)
        }
        viewModel.compressProgress.observe(this) {
            binding.progressPercentText.text = "" + it.progress + "%"
            binding.progressText.text = it.info
            if (it.progress == 100 && it.file != null) {
                MediaHelper().shareFile(requireContext(), it.file)
            }
        }

        binding.rcv.adapter = this.adapter
        binding.rcv.layoutManager = LinearLayoutManager(requireContext()).apply {
            orientation = LinearLayoutManager.VERTICAL
        }
        binding.rcv.setHasFixedSize(true)
        binding.rcv.itemAnimator = null

        binding.refreshBtn.onClick {
            reload(false)
        }

        binding.uploadBtn.onClick {
            val selectedData = adapter.datas.filter { it.isSelected }
            if (selectedData.count() > 3) {
                toastOnTop("请最多选择3个文件")
            } else if(selectedData.count() > 0) {
                val files = selectedData.map { it.file }
                switchSelectMode()
                binding.uploadGroup.visible()
                FileLog.ignoreWrite = true
                viewModel.compressAndShare(files)
            }
        }

        binding.selectBtn.onClick {
            switchSelectMode()
        }

        binding.uploadCloseBtn.onClick {
            binding.uploadGroup.gone()
            FileLog.ignoreWrite = false
        }
    }

    private fun switchSelectMode() {
        if (!isSelectedMode) {
            binding.selectBtn.text = "取消"
            binding.uploadBtn.visible()
            binding.refreshBtn.invisible() //不能不见

            for (bean in adapter.datas) {
                bean.isSelectedMode = true
            }
        } else {
            binding.refreshBtn.visible()
            binding.selectBtn.text = "选择"
            binding.uploadBtn.gone()

            for (bean in adapter.datas) {
                bean.isSelectedMode = false
                bean.isSelected = false
            }
        }
        adapter.submitList(adapter.datas, false)

        isSelectedMode = !isSelectedMode
    }

    override fun onStart() {
        super.onStart()
        reload(true)
    }

    private fun reload(init: Boolean) {
        binding.holdingView.visible()
        lifecycleScope.launch {
            if (!init) {
                adapter.submitList(listOf(), false)
                delay(800)
            }
            viewModel.scanLogs(singleClickBlock)
        }
    }

    private val singleClickBlock: (LogBean) -> Unit = { logBean->
        val file = logBean.file
        if (file != null) {
            ConfirmCenterDialog.show(childFragmentManager, "查看", "是否开启阅读?",
                "OK",
                sureClick = {
                    LogViewFragment.show(requireContext(), file)
                    it.dismissAllowingStateLoss()
                })
        }
    }
}