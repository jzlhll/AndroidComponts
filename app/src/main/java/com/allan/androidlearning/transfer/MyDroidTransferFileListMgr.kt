package com.allan.androidlearning.transfer

import android.widget.TextView
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.allan.androidlearning.activities2.MyDroidTransferFragment
import com.au.module_android.Globals
import com.au.module_android.utils.asOrNull
import com.au.module_android.utils.dp
import com.au.module_android.utils.launchOnThread
import com.au.module_nested.decoration.VertPaddingItemDecoration
import kotlinx.coroutines.launch

class MyDroidTransferFileListMgr(val f: MyDroidTransferFragment) {
    val adapter = MyDroidTransferFileListAdapter(f)

    fun initRcv() {
        f.apply {
            binding.rcv.adapter = adapter
            binding.rcv.layoutManager = LinearLayoutManager(binding.rcv.context)
            binding.rcv.setHasFixedSize(true)
            binding.rcv.addItemDecoration(VertPaddingItemDecoration(0, 0, 2.dp))
        }
    }

    fun loadFileList() {
        MyDroidGlobalService.loadFileListAsync { fileList->
            f.lifecycleScope.launch {
                adapter.submitList(fileList, false)

                if (!f.binding.rcv.isVisible) {
                    f.transferFileListTab.customView.asOrNull<TextView>()?.let { tabTv->
                        tabTv.text = f.getString(com.allan.androidlearning.R.string.transfer_list_2)
                    }
                }
            }
        }
    }

    fun loadHistory(init: Boolean) {
        MyDroidGlobalService.scope.launchOnThread {
            val history = MyDroidGlobalService.loadExportHistory()
            f.lifecycleScope.launch {
                f.binding.exportHistoryTv.text = "只保留最近80~100条导出记录:\n\n" + history
                if (!f.binding.exportHistoryHost.isVisible && !init) {
                    f.exportHistoryTab.customView.asOrNull<TextView>()?.let { tabTv->
                        tabTv.text = f.getString(com.allan.androidlearning.R.string.export_history_2)
                    }
                }
            }
        }
    }

    fun saveHistory(info:String, endCallback:()->Unit) {
        //确保写错。避免退出界面，没写。
        Globals.mainScope.launchOnThread {
            MyDroidGlobalService.writeNewExportHistory(info)
            endCallback()
        }
    }
}