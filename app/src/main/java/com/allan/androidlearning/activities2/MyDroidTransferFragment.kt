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
import androidx.core.net.toUri
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.allan.androidlearning.databinding.FragmentMyDroidBinding
import com.allan.androidlearning.transfer.MyDroidServerViewModel
import com.allan.classnameanno.EntryFrgName
import com.au.module_android.ui.bindings.BindingFragment
import com.au.module_android.ui.views.ToolbarInfo
import com.au.module_android.utils.logt
import com.au.module_android.utils.startActivityFix
import com.au.module_android.utils.unsafeLazy
import com.au.module_androidui.dialogs.ConfirmCenterDialog
import com.au.module_androidui.toast.ToastBuilder
import kotlinx.coroutines.launch
import java.net.Inet4Address

@EntryFrgName(priority = 12)
class MyDroidTransferFragment : BindingFragment<FragmentMyDroidBinding>() {
    override fun toolbarInfo() = ToolbarInfo("MyDroidTransfer")

    private var mConnectCb:ConnectivityManager.NetworkCallback? = null
    private val viewModel by unsafeLazy { ViewModelProvider(this)[MyDroidServerViewModel::class.java] }

    private val mFileListMgr by unsafeLazy { MyDroidTransferFileListMgr(this) }

    override fun onBindingCreated(savedInstanceState: Bundle?) {
        viewModel.ipPortData.observe(this) { pair->
            logt { "ip port changed $pair" }
            if (pair.second.isEmpty()) {
                binding.title.text = pair.first
            } else if (viewModel.isSuccessOpenServer) {
                binding.title.text = "局域网内访问：" + pair.first + ":" + pair.second
            } else {
                binding.title.text = pair.first + ":" + pair.second
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            requireActivity().setTurnScreenOn(true)
        }
        requireActivity().window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        connectRegister()
    }

    private fun startServer() {
        viewModel.startServer{ msg->
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
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

        val desc = binding.descTitle.text.toString()
        if (!desc.contains(" (")) {
            binding.descTitle.text = binding.descTitle.text.toString() + " (${viewModel.magicCode})"
        }

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

                    logt { "network callback $sb " + "ipport22 " + viewModel.ipPortData.realValue }
                    viewModel.ipPortData.setValueSafe(
                        sb.toString() to
                                (viewModel.ipPortData.realValue?.second ?: "")
                    )
                }
            }
        }

        val networkRequest = NetworkRequest.Builder()
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .build()
        val connectivityManager = requireContext().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        connectivityManager.registerNetworkCallback(networkRequest, mConnectCb!!)
    }

    private fun connectUnRegister() {
        mConnectCb ?: return
        val connectivityManager = requireContext().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        connectivityManager.unregisterNetworkCallback(mConnectCb!!)
    }

}