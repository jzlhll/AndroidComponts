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
import com.au.module_android.utils.logd
import kotlinx.coroutines.launch

class BleSubFragment : AbsSubBleTestFragment() {
    val bluetoothLeScanner: BluetoothLeScanner?
        get()= bluetoothAdapter?.bluetoothLeScanner

    private var mMyBleScanner = BleScanner()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                mMyBleScanner.uiState.collectStatusState(
                    success = {
                        updateInfo(it)
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

    override fun onBleClick(buttonType: String) {
        when (buttonType) {
            "read"-> {
                //todo
            }
            "connect"-> {
                //todo demo就按照某个固定的来了。正式情况，你应该编写recyclerView选择点击，传递BluetoothDevice
                val address = "48:27:E2:C9:63:22"
                val bluetoothDevice = bluetoothAdapter?.getRemoteDevice(address)
                logd { "bluetoothDevice $bluetoothDevice" }
            }
            "disconnect"-> {

            }
        }
    }
}