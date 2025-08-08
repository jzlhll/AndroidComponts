package com.au.audiorecordplayer.ble

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.BluetoothLeScanner
import android.content.Context
import android.os.Bundle
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.au.audiorecordplayer.ble.BtBroadcastReceiver.Companion.unRegister
import com.au.audiorecordplayer.databinding.BleBinding
import com.au.audiorecordplayer.util.MainUIManager
import com.au.audiorecordplayer.util.MyLog
import com.au.module_android.Globals
import com.au.module_android.click.onClick
import com.au.module_android.simpleflow.collectStatusState
import com.au.module_android.ui.bindings.BindingFragment
import com.au.module_android.ui.views.ToolbarInfo
import kotlinx.coroutines.launch

class BleTestFragment : BindingFragment<BleBinding>() {
    override fun toolbarInfo(): ToolbarInfo? {
        return ToolbarInfo(title = "ble")
    }

    private val blePermissionHelp = BlePermissionHelp(this)

    val bluetoothAdapter: BluetoothAdapter? by lazy { (Globals.app.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager).adapter }

    val bluetoothLeScanner: BluetoothLeScanner?
        get()= bluetoothAdapter?.bluetoothLeScanner

    private var mReceiver : BtBroadcastReceiver? = null
    private var mScanJob: kotlinx.coroutines.Job? = null

    @SuppressLint("MissingPermission")
    override fun onBindingCreated(savedInstanceState: Bundle?) {
        lifecycleScope.launch {
            repeatOnLifecycle(androidx.lifecycle.Lifecycle.State.STARTED) {
                mMyBleScanner.uiState.collectStatusState(
                    success = {
                        binding.textView.text = "${it.name} ${it.address}\n${it.log}"
                    },
                    error = {}
                )
            }
        }

        viewLifecycleOwner.lifecycle.addObserver(object: DefaultLifecycleObserver {
            override fun onStart(owner: LifecycleOwner) {
                mReceiver = BtBroadcastReceiver.createAndRegister(requireActivity())
                mScanJob = lifecycleScope.launch {
                    repeatOnLifecycle(androidx.lifecycle.Lifecycle.State.STARTED) {
                        mReceiver?.uiState?.collectStatusState(
                            success = {
                                binding.textView.text = if(it.address != null) "${it.name} ${it.address}" else it.log
                            },
                            error = {}
                        )
                    }
                }
            }

            override fun onStop(owner: LifecycleOwner) {
                mScanJob?.cancel()
                mReceiver?.unRegister(requireActivity())
                mReceiver = null
                bluetoothAdapter?.cancelDiscovery()
                mMyBleScanner.stopLeScan(bluetoothLeScanner)
            }
        })
        normalBtStartScanInit()
        bleStartScanInit()
    }

    @SuppressLint("MissingPermission")
    private fun normalBtStartScanInit() {
        binding.scanDevices.onClick { v ->
            if (bluetoothAdapter?.isEnabled != true) {
                MainUIManager.get().toastSnackbar(v, "蓝牙未打开！")
                return@onClick
            }

            blePermissionHelp.safeRun {
                MyLog.d("start discovery..$bluetoothAdapter")
                bluetoothAdapter?.startDiscovery()
            }
        }
    }

    //====================================================
    //=====
    ////////////////////////////////////////////////////

    private var mMyBleScanner = MyBleScanner()

    private fun bleStartScanInit() {
        binding.bleScanDevices.onClick {
            mMyBleScanner.startLeScan(bluetoothLeScanner)
        }
    }
}