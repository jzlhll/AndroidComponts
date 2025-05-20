package com.allan.androidlearning.transfer.views

import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.allan.androidlearning.R
import com.allan.androidlearning.databinding.FragmentMyDroidBinding
import com.allan.androidlearning.transfer.MyDroidConst
import com.allan.androidlearning.transfer.MyDroidGlobalService
import com.allan.androidlearning.transfer.MyDroidGlobalService.scope
import com.allan.androidlearning.transfer.MyDroidMess
import com.allan.androidlearning.transfer.benas.MyDroidMode
import com.allan.classnameanno.EntryFrgName
import com.au.module_android.Globals
import com.au.module_android.click.onClick
import com.au.module_android.json.toJsonString
import com.au.module_android.ui.bindings.BindingFragment
import com.au.module_android.utils.asOrNull
import com.au.module_android.utils.gone
import com.au.module_android.utils.launchOnThread
import com.au.module_android.utils.launchOnUi
import com.au.module_android.utils.logdNoFile
import com.au.module_android.utils.transparentStatusBar
import com.au.module_android.utils.unsafeLazy
import com.au.module_android.utils.visible
import com.au.module_android.utilsmedia.getExternalFreeSpace
import com.au.module_androidui.toast.ToastBuilder
import com.google.android.material.tabs.TabLayout
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class MyDroidReceiverFragment : BindingFragment<FragmentMyDroidBinding>() {
    private val mFileListMgr by unsafeLazy { MyDroidReceiveFileListMgr(this) }

    lateinit var receivedFileListTab: TabLayout.Tab
    lateinit var exportHistoryTab: TabLayout.Tab

    val fileExportFailCallback:(String)->Unit = { info->
        Globals.mainScope.launchOnUi {
            ToastBuilder().setOnTop().setMessage(info).setIcon("error").toast()
        }
    }

    val fileExportSuccessCallback:(info:String, exportFileStr:String)->Unit = { info, exportFileStr->
        Globals.mainScope.launchOnUi {
            ToastBuilder().setOnTop().setMessage(info.replace("/storage/emulated/0/", "/sdcard/"))
                .setIcon("success").toast()

            //确保写错。避免退出界面，没写。
            mFileListMgr.writeHistory(exportFileStr) {
                fileChanged()
            }
        }
    }

    val fileChanged:()->Unit = {
        Globals.mainScope.launchOnThread {
            mFileListMgr.loadFileList()
            mFileListMgr.loadHistory(false)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            requireActivity().setTurnScreenOn(true)
        }
        requireActivity().window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        requireActivity().transparentStatusBar(statusBarTextDark = false) {  insets, statusBarsHeight, _ ->
            binding.toolbar.layoutParams.asOrNull<ConstraintLayout.LayoutParams>()?.let { toolbarLP->
                toolbarLP.topMargin = statusBarsHeight
                binding.toolbar.layoutParams = toolbarLP
            }
            insets
        }
    }

    override fun onBindingCreated(savedInstanceState: Bundle?) {
        binding.tabLayout.tabSelectTextColor = R.color.logic_receiver
        binding.tabLayout.tabNotSelectColor = com.au.module_androidcolor.R.color.color_text_desc

        val fmt = getString(R.string.not_close_window)
        binding.descTitle.text = String.format(fmt, " 存储剩余：" + getExternalFreeSpace(requireActivity()))

        MyDroidConst.onTransferInfoData.observeUnStick(this) { info->
            binding.transferInfo.text = info
        }
        MyDroidConst.onFileMergedData.observe(this) { file->
            scope.launch {
                ToastBuilder().setOnTop()
                    .setMessage("成功收到文件：${file.name} !")
                    .setIcon("success").toast()
            }
            mFileListMgr.loadFileList()
        }

        MyDroidConst.ipPortData.observe(this) { info->
            if (info == null || info.ip.isEmpty()) {
                binding.title.text = "请连接WI-FI或者开启热点"
            } else {
                if (info.httpPort == null) {
                    binding.title.text = info.ip
                } else if (MyDroidConst.serverIsOpen.realValueUnsafe) {
                    binding.title.text = "局域网内访问：" + info.ip + ":" + info.httpPort
                } else {
                    binding.title.text = info.ip + ":" + info.httpPort
                }
            }
        }

        MyDroidConst.clientListLiveData.observe(this) { clientList->
            logdNoFile {
                ">>client List:" + clientList.toJsonString()
            }
        }

        initLater()
    }

    override fun onStart() {
        MyDroidConst.myDroidModeData.setValueSafe(MyDroidMode.Receiver)
        super.onStart()
    }

    private fun initLater() {
        Globals.mainHandler.post {
            val transferFileList = binding.tabLayout.newTextTab(getString(R.string.transfer_list), true, 16f)
            transferFileList.view.onClick {
                binding.receiveRcv.visible()
                mFileListMgr.changeRcvEmptyTextVisible()
                binding.exportHistoryHost.gone()
                receivedFileListTab.customView.asOrNull<TextView>()?.let { tabTv->
                    tabTv.text = getString(R.string.transfer_list)
                }
            }
            receivedFileListTab = transferFileList
            val exportHistory = binding.tabLayout.newTextTab(getString(R.string.export_history), false, 16f)
            exportHistory.view.onClick {
                binding.receiveRcv.gone()
                mFileListMgr.changeRcvEmptyTextVisible()
                binding.exportHistoryHost.visible()
                exportHistoryTab.customView.asOrNull<TextView>()?.let { tabTv->
                    tabTv.text = getString(R.string.export_history)
                }
            }
            exportHistoryTab = exportHistory

            binding.tabLayout.addTab(transferFileList)
            binding.tabLayout.addTab(exportHistory)
            binding.tabLayout.initSelectedListener()

            mFileListMgr.initRcv()
            mFileListMgr.loadFileList()
            mFileListMgr.loadHistory(true)

            binding.toolbar.setNavigationOnClickListener {
                requireActivity().finishAfterTransition()
            }
        }
    }

}