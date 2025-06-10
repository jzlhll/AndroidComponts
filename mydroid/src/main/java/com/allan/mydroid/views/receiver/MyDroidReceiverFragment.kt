package com.allan.mydroid.views.receiver

import android.os.Bundle
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.os.bundleOf
import com.allan.mydroid.R
import com.allan.mydroid.globals.KEY_AUTO_ENTER_SEND_VIEW
import com.allan.mydroid.globals.MyDroidConst
import com.allan.mydroid.beans.MyDroidMode
import com.allan.mydroid.databinding.FragmentMyDroidReceiveBinding
import com.allan.mydroid.views.AbsLiveFragment
import com.allan.mydroid.views.send.SendListSelectorFragment
import com.au.module_android.Globals
import com.au.module_android.click.onClick
import com.au.module_android.json.toJsonString
import com.au.module_android.ui.FragmentShellActivity
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

class MyDroidReceiverFragment : AbsLiveFragment<FragmentMyDroidReceiveBinding>() {
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
                mFileListMgr.loadHistory(false)
            }
        }
    }

    val fileChanged:()->Unit = {
        Globals.mainScope.launchOnThread {
            mFileListMgr.loadFileList()
        }
    }

    val importSendCallback:()->Unit = {
        activity?.let { a->
            a.finishAfterTransition()
            FragmentShellActivity.Companion.start(
                a, SendListSelectorFragment::class.java,
                bundleOf(KEY_AUTO_ENTER_SEND_VIEW to true)
            )
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requireActivity().transparentStatusBar(statusBarTextDark = false) { insets, statusBarsHeight, _ ->
            binding.toolbar.layoutParams.asOrNull<ConstraintLayout.LayoutParams>()?.let { toolbarLP->
                toolbarLP.topMargin = statusBarsHeight
                binding.toolbar.layoutParams = toolbarLP
            }
            insets
        }
    }

    override fun onBindingCreated(savedInstanceState: Bundle?) {
        super.onBindingCreated(savedInstanceState)
        binding.adHost.setColor(Globals.getColor(com.au.module_androidcolor.R.color.color_normal_block0))
        binding.adHost.startAnimation()

        binding.tabLayout.tabSelectTextColor = R.color.logic_receiver
        binding.tabLayout.tabNotSelectColor = com.au.module_androidcolor.R.color.color_text_desc

        val fmt = getString(R.string.not_close_window)
        val leftStr = getString(R.string.storage_remaining)
        binding.descTitle.text = String.format(fmt, leftStr + getExternalFreeSpace(requireActivity()))

        MyDroidConst.onFileMergedData.observeUnStick(this) { file->
            val strFmt = getString(R.string.file_received_success_fmt)
            ToastBuilder().setOnTop()
                .setMessage(String.format(strFmt, file.name))
                .setIcon("success").toast()
            mFileListMgr.loadFileList()
        }

        MyDroidConst.ipPortData.observe(this) { info->
            if (info == null || info.ip.isEmpty()) {
                binding.title.setText(R.string.connect_wifi_or_hotspot)
            } else {
                if (info.httpPort == null) {
                    binding.title.text = info.ip
                } else if (MyDroidConst.serverIsOpen) {
                    binding.title.text = String.format(getString(R.string.lan_access_fmt), info.ip, "" + info.httpPort)
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
        MyDroidConst.myDroidMode = MyDroidMode.Receiver
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