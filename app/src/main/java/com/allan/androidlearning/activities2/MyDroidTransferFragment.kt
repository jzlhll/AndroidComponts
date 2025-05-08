package com.allan.androidlearning.activities2

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.view.WindowManager
import android.widget.TextView
import androidx.core.net.toUri
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.allan.androidlearning.databinding.FragmentMyDroidBinding
import com.allan.androidlearning.transfer.MyDroidServerViewModel
import com.allan.androidlearning.transfer.MyDroidTransferFileListMgr
import com.allan.classnameanno.EntryFrgName
import com.au.module_android.Globals
import com.au.module_android.click.onClick
import com.au.module_android.ui.bindings.BindingFragment
import com.au.module_android.ui.views.ToolbarInfo
import com.au.module_android.utils.asOrNull
import com.au.module_android.utils.gone
import com.au.module_android.utils.launchOnUi
import com.au.module_android.utils.logdNoFile
import com.au.module_android.utils.startActivityFix
import com.au.module_android.utils.unsafeLazy
import com.au.module_android.utils.visible
import com.au.module_androidui.dialogs.ConfirmCenterDialog
import com.au.module_androidui.toast.ToastBuilder
import com.google.android.material.tabs.TabLayout
import kotlinx.coroutines.launch
import java.net.Inet4Address

@EntryFrgName(priority = 12)
class MyDroidTransferFragment : BindingFragment<FragmentMyDroidBinding>() {
    companion object {
        var fileExportSuccessCallback : ((String, String)->Unit)? = null
        var fileExportFailCallback : ((String)->Unit)? = null
    }

    override fun onDestroy() {
        super.onDestroy()
        fileExportSuccessCallback = null
        fileExportFailCallback = null
    }

    override fun toolbarInfo() = ToolbarInfo("MyDroidTransfer")

    private var mConnectCb:ConnectivityManager.NetworkCallback? = null
//    private val viewModel by lazy { ViewModelProvider(requireActivity())[MyDroidServerViewModel::class.java] }
//    val viewModel: MyDroidServerViewModel by activityViewModels()
    lateinit var viewModel: MyDroidServerViewModel

    private val mFileListMgr by unsafeLazy { MyDroidTransferFileListMgr(this) }

    lateinit var transferFileListTab: TabLayout.Tab
    lateinit var exportHistoryTab: TabLayout.Tab

    override fun onBindingCreated(savedInstanceState: Bundle?) {
        fileExportFailCallback = { info->
            Globals.mainScope.launchOnUi {
                ToastBuilder().setOnTop().setMessage(info).setIcon("error").toast()
            }
        }
        fileExportSuccessCallback = { info, exportFileStr->
            Globals.mainScope.launchOnUi {
                ToastBuilder().setOnTop().setMessage(info.replace("/storage/emulated/0/", "/sdcard/"))
                    .setIcon("success").toast()
                mFileListMgr.saveHistory(exportFileStr) {
                    mFileListMgr.loadFileList()
                    mFileListMgr.loadHistory(false)
                }
            }
        }

        viewModel = ViewModelProvider(requireActivity())[MyDroidServerViewModel::class.java]

        viewModel.ipPortData.observe(this) { pair->
            if (pair.second.isEmpty()) {
                binding.title.text = pair.first
            } else if (viewModel.isSuccessOpenServer) {
                binding.title.text = "局域网内访问：" + pair.first + ":" + pair.second
            } else {
                binding.title.text = pair.first + ":" + pair.second
            }
        }

        val transferFileList = binding.tabLayout.newTextTab(getString(com.allan.androidlearning.R.string.transfer_list), true, 16f)
        transferFileList.view.onClick {
            binding.rcv.visible()
            binding.exportHistoryHost.gone()
            transferFileListTab.customView.asOrNull<TextView>()?.let { tabTv->
                tabTv.text = getString(com.allan.androidlearning.R.string.transfer_list)
            }
        }
        transferFileListTab = transferFileList
        val exportHistory = binding.tabLayout.newTextTab(getString(com.allan.androidlearning.R.string.export_history), false, 16f)
        exportHistory.view.onClick {
            binding.rcv.gone()
            binding.exportHistoryHost.visible()
            exportHistoryTab.customView.asOrNull<TextView>()?.let { tabTv->
                tabTv.text = getString(com.allan.androidlearning.R.string.export_history)
            }
        }
        exportHistoryTab = exportHistory

        binding.tabLayout.addTab(transferFileList)
        binding.tabLayout.addTab(exportHistory)
        binding.tabLayout.initSelectedListener()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            requireActivity().setTurnScreenOn(true)
        }
        requireActivity().window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        connectRegister()

        mFileListMgr.initRcv()
        mFileListMgr.loadFileList()
        mFileListMgr.loadHistory(true)
    }

    private fun startServer() {
        logdNoFile { "viewModel11 ${viewModel.ipPortData}" }
        viewModel.startServer(
            transferInfoCallback = { transferInfo->
                lifecycleScope.launch {
                    binding.transferInfo.text = transferInfo
                }
            },
            fileMergedSucCallback = { file->
                lifecycleScope.launch {
                    ToastBuilder().setOnFragment(this@MyDroidTransferFragment)
                        .setMessage("成功收到文件：${file.name} !")
                        .setIcon("success").toast()
                }
                mFileListMgr.loadFileList()
            }
        ) { msg->
            lifecycleScope.launch {
                ToastBuilder()
                    .setOnFragment(this@MyDroidTransferFragment)
                    .setIcon("error")
                    .setMessage(msg)
                    .toast()
            }
        }
    }

    private fun ifGotoMgrAll() : Boolean{
        if (false && Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val ex = Environment.isExternalStorageManager()
            if (!ex) {
                ConfirmCenterDialog.show(childFragmentManager,
                    "应用管理权限",
                    "该功能需要全局设置权限，即将跳转，打开该功能。",
                    "OK") {
                    gotoMgrAll()
                    it.dismissAllowingStateLoss()
                }
            }
            return ex
        }

        return true
    }

    private fun gotoMgrAll() {
        val intent = Intent().apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                action = Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION
            }
            data = "package:${requireContext().packageName}".toUri()
        }
        startActivityFix(intent)
    }

    @SuppressLint("SetTextI18n")
    override fun onStart() {
        super.onStart()
        if (!viewModel.isSuccessOpenServer && ifGotoMgrAll()) {
            startServer()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewModel.stopServer()
        connectUnRegister()
    }

    private fun connectRegister() {
        if (mConnectCb == null) {
            mConnectCb = object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) {
                    val connectivityManager = requireContext().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
                    val linkProperties = connectivityManager.getLinkProperties(network)
                    val sb = StringBuilder()
                    linkProperties?.linkAddresses?.forEach { linkAddress->
                        if (linkAddress.address is Inet4Address) {
                            sb.append(linkAddress.address.hostAddress)
                        }
                    }

                    viewModel.ipPortData.setValueSafe(
                        sb.toString() to
                                (viewModel.ipPortData.realValue?.second ?: "")
                    )
                }
            }
            val networkRequest = NetworkRequest.Builder()
                .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                .build()
            val connectivityManager = requireContext().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            connectivityManager.registerNetworkCallback(networkRequest, mConnectCb!!)
        }
    }

    private fun connectUnRegister() {
        mConnectCb ?: return
        val connectivityManager = requireContext().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        connectivityManager.unregisterNetworkCallback(mConnectCb!!)
    }

}