package com.au.audiorecordplayer.bt.trandition

import android.Manifest
import android.annotation.SuppressLint
import android.os.Bundle
import androidx.annotation.RequiresPermission
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.au.audiorecordplayer.bt.AbsSubBleTestFragment
import com.au.audiorecordplayer.bt.trandition.BtBroadcastReceiver.Companion.register
import com.au.audiorecordplayer.bt.trandition.BtBroadcastReceiver.Companion.unRegister
import com.au.audiorecordplayer.util.MainUIManager
import com.au.audiorecordplayer.util.MyLog
import com.au.module_android.simpleflow.collectStatusState
import kotlinx.coroutines.launch

class BtClassicSubFragment : AbsSubBleTestFragment() {
    private var mReceiver : BtBroadcastReceiver = BtBroadcastReceiver()
    private var clientConnector: ClientConnector?=null
    private var serverConnector: ServerConnector?=null

    override fun onClassicClick(buttonType: String) {
        when (buttonType) {
            "startServer" -> {
                val adapter = bluetoothAdapter
                if (adapter == null) {
                    MainUIManager.get().toastSnackbar(requireActivity().window.decorView, "蓝牙Adapter不存在！")
                } else {
                    serverConnector = ServerConnector(adapter).also {
                        lifecycleScope.launch {
                            serverConnector?.prepare()
                        }
                    }
                }
            }
            "clientStartTransfer" -> {
                val adapter = bluetoothAdapter
                if (adapter == null) {
                    MainUIManager.get().toastSnackbar(requireActivity().window.decorView, "蓝牙Adapter不存在！")
                } else {
                    clientConnector = ClientConnector(adapter)
                }
            }
            "closeTransfer" -> {
                serverConnector?.shutdown()
                clientConnector?.shutdown()
            }
        }
    }

    @SuppressLint("MissingPermission")
    override fun onStartScan() {
        if (bluetoothAdapter?.isEnabled != true) {
            MainUIManager.get().toastSnackbar(requireActivity().window.decorView, "蓝牙未打开！")
            return
        }
        blePermissionHelp.safeRun {
            MyLog.d("start discovery..$bluetoothAdapter")
            bluetoothAdapter?.startDiscovery()
        }
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_SCAN)
    override fun onStopScan() {
        bluetoothAdapter?.cancelDiscovery()
    }

    override fun onStart() {
        super.onStart()
        mReceiver.register(requireContext())
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_SCAN)
    override fun onStop() {
        super.onStop()
        mReceiver.unRegister(requireActivity())
        onStopScan()
        onClassicClick("closeTransfer")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                mReceiver.uiState.collectStatusState(
                    success = {
                        updateInfo(if(it.address != null) "${it.name} ${it.address}" else it.log)
                    },
                    error = {}
                )
            }
        }
    }
}