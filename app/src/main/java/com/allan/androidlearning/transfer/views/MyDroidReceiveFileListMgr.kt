package com.allan.androidlearning.transfer.views

import android.widget.TextView
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.allan.androidlearning.R
import com.allan.androidlearning.transfer.MyDroidGlobalService
import com.allan.androidlearning.transfer.MyDroidMess
import com.au.module_android.Globals
import com.au.module_android.utils.asOrNull
import com.au.module_android.utils.dp
import com.au.module_android.utils.launchOnThread
import com.au.module_nested.decoration.VertPaddingItemDecoration
import kotlinx.coroutines.launch

class MyDroidReceiveFileListMgr(val f: MyDroidReceiverFragment) {
    val adapter = MyDroidReceiveFileListAdapter(f)

    fun initRcv() {
        f.apply {
            binding.rcv.adapter = adapter
            binding.rcv.layoutManager = LinearLayoutManager(binding.rcv.context)
            binding.rcv.setHasFixedSize(true)
            binding.rcv.addItemDecoration(VertPaddingItemDecoration(0, 0, 2.dp))
        }
    }

    fun loadFileList() {
        MyDroidGlobalService.scope.launchOnThread {
            val fileList = MyDroidMess().loadFileList()
            f.lifecycleScope.launch {
                adapter.submitList(fileList, false)

                if (!f.binding.rcv.isVisible) {
                    f.receivedFileListTab.customView.asOrNull<TextView>()?.let { tabTv->
                        tabTv.text = f.getString(R.string.transfer_list_2)
                    }
                }
            }
        }
    }

    fun loadHistory(init: Boolean) {
        MyDroidGlobalService.scope.launchOnThread {
            val history = MyDroidMess().loadExportHistory()
            f.lifecycleScope.launch {
                f.binding.exportHistoryTv.text = "只保留最近80~100条导出记录:\n\n" + history
                if (!f.binding.exportHistoryHost.isVisible && !init) {
                    f.exportHistoryTab.customView.asOrNull<TextView>()?.let { tabTv->
                        tabTv.text = f.getString(R.string.export_history_2)
                    }
                }
            }
        }
    }

    fun writeHistory(info:String, afterWriteCallback:()->Unit) {
        Globals.mainScope.launchOnThread {
            MyDroidMess().writeNewExportHistory(info)
            afterWriteCallback()
        }
    }
}