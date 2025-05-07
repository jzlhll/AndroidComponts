package com.au.logsystem

import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.au.logsystem.databinding.FragmentLogSystemBinding
import com.au.logsystem.oncelog.OnceLogViewFragment
import com.au.module_android.click.onClick
import com.au.module_android.ui.bindings.BindingFragment
import com.au.module_android.ui.views.ToolbarInfo
import com.au.module_android.utils.FileLog
import com.au.module_android.utils.MediaHelper
import com.au.module_android.utils.gone
import com.au.module_android.utils.invisible
import com.au.module_android.utils.launchOnThread
import com.au.module_android.utils.visible
import com.au.module_androidui.dialogs.ConfirmCenterDialog
import com.au.module_androidui.toast.toastOnTop
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class LogSystemFragment : BindingFragment<FragmentLogSystemBinding>(), LogViewActionDialog.IAction {
    override fun toolbarInfo(): ToolbarInfo? {
        return ToolbarInfo(title = "日志系统")
    }

    private val adapter = LogRcvAdapter()

    private var isSelectedMode = false

    private val viewModel by lazy { ViewModelProvider(requireActivity())[LogSystemViewModel::class.java] }

    override fun onDestroy() {
        super.onDestroy()
        FileLog.ignoreWrite = false
    }

    override fun onBindingCreated(savedInstanceState: Bundle?) {
        super.onBindingCreated(savedInstanceState)
        FileLog.ignoreWrite = true
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
            reload()
        }

        binding.uploadBtn.onClick {
            val selectedData = adapter.datas.filter { it.isSelected }
            if (selectedData.count() > 3) {
                toastOnTop("请最多选择3个文件")
            } else if(selectedData.count() > 0) {
                val files = selectedData.map { it.file }
                switchSelectMode()
                binding.uploadGroup.visible()
                viewModel.compressAndShare(files)
            }
        }

        binding.selectBtn.onClick {
            switchSelectMode()
        }

        binding.uploadCloseBtn.onClick {
            binding.uploadGroup.gone()
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

    private fun reload(init: Boolean = false) {
        lifecycleScope.launch {
            binding.holdingView.visible()
            if(!init) delay(500)
            viewModel.scanLogs(singleClickBlock)
        }
    }

    private var logBean: LogBean? = null

    private val singleClickBlock: (LogBean) -> Unit = { logBean->
        this.logBean = logBean
        LogViewActionDialog.pop(this)
    }

    override fun onNotify(mode: String) {
        val file = logBean?.file ?: return
        when (mode) {
            "view" -> OnceLogViewFragment.show(requireContext(), file)
            "delete" -> ConfirmCenterDialog.show(childFragmentManager, "删除", "是否删除?",
                "OK",
                sureClick = {
                    deleteFile()
                    it.dismissAllowingStateLoss()
                })
        }
    }

    private fun deleteFile() {
        binding.holdingView.visible()

        lifecycleScope.launchOnThread {
            delay(500)

            try {
                logBean?.file?.delete()
                delay(500)
            } catch (e: Exception) {
                e.printStackTrace()
            }

            reload()
        }
    }
}