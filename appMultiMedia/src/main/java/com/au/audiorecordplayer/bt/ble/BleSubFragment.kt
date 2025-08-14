package com.au.audiorecordplayer.bt.ble

import android.bluetooth.le.BluetoothLeScanner
import android.os.Bundle
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.au.audiorecordplayer.bt.AbsSubBleTestFragment
import com.au.audiorecordplayer.util.MainUIManager
import com.au.audiorecordplayer.util.MyLog
import com.au.module_android.Globals
import com.au.module_android.json.formatJsonBeautiful
import com.au.module_android.simpleflow.collectStatusState
import com.au.module_android.utils.launchOnThread
import com.au.module_android.utils.logt
import kotlinx.coroutines.launch

class BleSubFragment : AbsSubBleTestFragment(), BluetoothCallbackListener {
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

    override fun onStart() {
        super.onStart()
        BleConnectors.addListener(this)
    }

    override fun onStop() {
        super.onStop()
        onStopScan()
        BleConnectors.removeListener(this)
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

    private var mObAddressList = mutableListOf<String>()

    override fun onBleClick(buttonType: String) {
        when (buttonType) {
            "read"-> {
                val address = "48:27:E2:1B:A8:92"
                lifecycleScope.launchOnThread {
                    val isSuc = BleConnectors.getBleConnector(address)?.sendMessage("read_status:all")
                    MyLog.d("send read_status $isSuc")
                }
            }
            "connect"-> {
                //todo demo就按照某个固定的来了。正式情况，你应该编写recyclerView选择点击，传递BluetoothDevice
                val address = "48:27:E2:1B:A8:92"
                mObAddressList.add(address)
                val adapter = bluetoothAdapter
                if (adapter == null) {
                    MainUIManager.get().toastSnackbar(requireActivity().window.decorView, "蓝牙未打开！")
                } else {
                    val bluetoothDevice = bluetoothAdapter?.getRemoteDevice(address)
                    val bleConnector = BleConnectors.getOrCreate(bluetoothDevice) ?: return
                    if (bleConnector.isConnected()) {
                        updateInfo("${address}已连接!")
                    } else if (bleConnector.isPrepared()) {
                        updateInfo("${address}可发送数据!")
                    } else {
                        bleConnector.connect()
                    }
                }
            }
            "disconnect"-> {
                val address = "48:27:E2:1B:A8:92"
                BleConnectors.removeAndDisconnect(address)
            }
        }
    }

    override fun onConnectionStateChanged(address: String, state: Int) {
        logt { "onConnectionStateChanged $address $state" }
        lifecycleScope.launch {
            updateInfo("onConnectionStateChanged $address ${BleUtil.bluetoothProfileToString(state)}")
        }
    }

    override fun onPrepared(address: String) {
        logt { "onPrepared $address" }
        lifecycleScope.launch {
            updateInfo("onPrepared $address}")
        }
    }

    override fun onDataCharChanged(address: String, data: ByteArray, str: String) {
        logt { "onDataCharChanged $address $str" }
        lifecycleScope.launch {
            val json = formatJsonBeautiful(str)
            updateInfo("onDataCharChanged $address\n$json")
        }
    }

    override fun onDataCharRead(address: String, data: ByteArray) {
        logt { "onDataCharRead $address $data" }
        lifecycleScope.launch {
            updateInfo("onDataCharRead $address $data")
        }
    }

    override fun onMtuChanged(address: String, newMtu: Int, status: Int) {
        logt { "onMtuChanged $address newMtu $newMtu $status" }
//        lifecycleScope.launch {
//            updateInfo("onMtuChanged $address newMtu $newMtu $status")
//        }
    }

    override fun observerAddressList(): List<String> {
        return mObAddressList
    }
}