package com.allan.mydroid.views.receiver

import android.annotation.SuppressLint
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.allan.mydroid.R
import com.allan.mydroid.globals.MyDroidGlobalService
import com.allan.mydroid.globals.MyDroidMess
import com.au.module_android.Globals
import com.au.module_android.Globals.resStr
import com.au.module_android.utils.asOrNull
import com.au.module_android.utils.dp
import com.au.module_android.utils.gone
import com.au.module_android.utils.launchOnThread
import com.au.module_android.utils.logd
import com.au.module_android.utils.visible
import com.au.module_nested.decoration.VertPaddingItemDecoration
import kotlinx.coroutines.launch

class MyDroidReceiveFileListMgr(val f: MyDroidReceiverFragment) {
    val mAdapter = MyDroidReceiveFileListAdapter(fullClick = { bean ->
        ShowReceiveItemDialog.Companion.pop(
            f.childFragmentManager, arrayOf(
                bean.file.name,
                bean.md5,
                bean.fileSizeInfo
            )
        )
    }) {
        ExportSelectActionDialog.Companion.pop(
            f.childFragmentManager, it, fileExportFailCallback = f.fileExportFailCallback,
            fileExportSuccessCallback = f.fileExportSuccessCallback,
            refreshFileListCallback = f.fileChanged,
            importSendCallback = f.importSendCallback
        )
    }

    fun initRcv() {
        f.binding.receiveRcv.apply {
            adapter = mAdapter
            layoutManager = LinearLayoutManager(context)
            setHasFixedSize(true)
            addItemDecoration(VertPaddingItemDecoration(0, 0, 2.dp))
        }
    }

    fun changeRcvEmptyTextVisible() {
        val isRcvVisible = f.binding.receiveRcv.isVisible
        if (isRcvVisible) {
            if (mAdapter.datas.isEmpty()) {
                f.binding.receiveRcvEmptyTv.visible()
            } else {
                f.binding.receiveRcvEmptyTv.gone()
            }
        } else {
            f.binding.receiveRcvEmptyTv.gone()
        }
    }

    fun loadFileList() {
        logd { "load file list0" }
        MyDroidGlobalService.scope.launchOnThread {
            val fileList = MyDroidMess().loadFileList()
            logd { "load file list1" }
            f.lifecycleScope.launch {
                val isRcvVisible = f.binding.receiveRcv.isVisible
                mAdapter.submitList(fileList, false)
                changeRcvEmptyTextVisible()
                if (!isRcvVisible) {
                    f.receivedFileListTab.customView.asOrNull<TextView>()?.let { tabTv->
                        tabTv.text = f.getString(R.string.transfer_list_2)
                    }
                }
            }
        }
    }

    @SuppressLint("SetTextI18n")
    fun loadHistory(init: Boolean) {
        MyDroidGlobalService.scope.launchOnThread {
            val history = MyDroidMess().loadExportHistory()
            f.lifecycleScope.launch {
                f.binding.exportHistoryTv.text = R.string.keep_recent_records.resStr() + "\n\n" + history
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