package com.au.audiorecordplayer.bt.ble

import android.bluetooth.le.BluetoothLeScanner
import android.os.Bundle
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.au.audiorecordplayer.bt.AbsSubBleTestFragment
import com.au.audiorecordplayer.util.MainUIManager
import com.au.audiorecordplayer.util.MyLog
import com.au.module_android.simpleflow.collectStatusState
import kotlinx.coroutines.launch

class BleFragment : AbsSubBleTestFragment() {
    val bluetoothLeScanner: BluetoothLeScanner?
        get()= bluetoothAdapter?.bluetoothLeScanner

    private var mMyBleScanner = MyBleScanner()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                mMyBleScanner.uiState.collectStatusState(
                    success = {
                        updateInfo("${it.name} ${it.address}\n${it.log}")
                    },
                    error = {}
                )
            }
        }
    }

    override fun onStop() {
        super.onStop()
        onStopScan()
    }

    override fun onStartScan() {
        if (bluetoothAdapter?.isEnabled != true) {
            MainUIManager.get().toastSnackbar(requireActivity().window.decorView, "蓝牙未打开！")
            return
        }
        blePermissionHelp.safeRun {
            MyLog.d("start le scan")
            mMyBleScanner.startLeScan(bluetoothLeScanner)
        }
    }

    override fun onStopScan() {
        mMyBleScanner.stopLeScan(bluetoothLeScanner)
    }

}