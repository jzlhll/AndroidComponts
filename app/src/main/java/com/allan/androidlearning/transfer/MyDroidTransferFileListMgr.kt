package com.allan.androidlearning.transfer

import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.allan.androidlearning.activities2.MyDroidTransferFragment
import com.au.module_android.utils.dp
import com.au.module_android.utils.launchOnThread
import com.au.module_nested.decoration.VertPaddingItemDecoration
import kotlinx.coroutines.launch

class MyDroidTransferFileListMgr(val f: MyDroidTransferFragment) {
    val adapter = MyDroidTransferFileListAdapter()

    fun initRcv() {
        f.apply {
            binding.rcv.adapter = adapter
            binding.rcv.layoutManager = LinearLayoutManager(binding.rcv.context)
            binding.rcv.setHasFixedSize(true)
            binding.rcv.addItemDecoration(VertPaddingItemDecoration(0, 0, 2.dp))

            lifecycleScope.launchOnThread {
                val fileList = f.viewModel.loadFileList()
                lifecycleScope.launch {
                    adapter.submitList(fileList, false)
                }
            }
        }
    }
}