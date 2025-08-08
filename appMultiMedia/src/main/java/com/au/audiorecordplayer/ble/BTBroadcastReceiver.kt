package com.au.audiorecordplayer.ble

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import com.au.audiorecordplayer.util.MyLog
import com.au.module_android.simpleflow.StatusState
import com.au.module_android.utils.parcelableExtraCompat
import com.au.module_android.utils.registerReceiverFix
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * 注册的广播监听，并非一定是你个人
 */
class BtBroadcastReceiver : BroadcastReceiver() {
    private val _uiState : MutableStateFlow<StatusState<ScannedInfo>> = MutableStateFlow(StatusState.Loading)
    val uiState: StateFlow<StatusState<ScannedInfo>> = _uiState

//    private var currentScanBtAddress:String? = null
//    private var currentScanIndex = 0
//
//    fun setCurrentScanAddress(address: String) {
//        currentScanBtAddress = address
//        currentScanIndex = 0
//    }

//    var cancelScan:()->Unit = {}

    companion object {
        fun createAndRegister(context: Context) : BtBroadcastReceiver {
            val intentFilter = IntentFilter()
            intentFilter.addAction(BluetoothDevice.ACTION_FOUND)
            intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED)
            intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
//            intentFilter.addAction(BluetoothDevice.ACTION_PAIRING_REQUEST)
            intentFilter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED)
//            intentFilter.addAction(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED)
//            intentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED)
            val receiver = BtBroadcastReceiver()
            context.registerReceiverFix(receiver, intentFilter)
            MyLog.d("register ed")
            return receiver
        }

        fun BtBroadcastReceiver.unRegister(context: Context) {
            MyLog.d("unregister Receiver ed")
            context.unregisterReceiver(this)
        }
    }

    @SuppressLint("MissingPermission")
    override fun onReceive(context: Context?, intent: Intent?) {
        MyLog.d( "onReceive" )
        when(intent?.action)
        {
            BluetoothDevice.ACTION_FOUND -> {
                val device = intent.parcelableExtraCompat<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                MyLog.d( "ACTION_FOUND device: ${device?.name}（${device?.address}）")
                _uiState.value = StatusState.Success(ScannedInfo("ACTION_FOUND", device?.name, device?.address))
//                if (currentScanBtAddress != null && device?.address == currentScanBtAddress && currentScanIndex == 0) {
//                    MyLog.d("ACTION_FOUND device: ${device?.name}（${device?.address}）attack!!!!" )
//                    currentScanIndex = 1
//                    cancelScan()
//                }
            }

            BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
                MyLog.d("ACTION_DISCOVERY_FINISHED")
                _uiState.value = StatusState.Success(ScannedInfo("ACTION_DISCOVERY_FINISHED"))
            }

            BluetoothAdapter.ACTION_DISCOVERY_STARTED -> {
                MyLog.d("ACTION_DISCOVERY_STARTED")
                _uiState.value = StatusState.Success(ScannedInfo("ACTION_DISCOVERY_STARTED"))
            }

            BluetoothAdapter.ACTION_STATE_CHANGED -> {
                MyLog.d("ACTION_STATE_CHANGED")

                val state = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.ERROR);
                val prevState = intent.getIntExtra(BluetoothDevice.EXTRA_PREVIOUS_BOND_STATE, BluetoothDevice.ERROR);

                if (state == BluetoothDevice.BOND_BONDED && prevState == BluetoothDevice.BOND_BONDING) {
                    MyLog.d("ACTION_STATE_CHANGED 1")
                    _uiState.value = StatusState.Success(ScannedInfo("ACTION_STATE_CHANGED 1"))
                } else if (state == BluetoothDevice.BOND_NONE && prevState == BluetoothDevice.BOND_BONDED) {
                    MyLog.d("ACTION_STATE_CHANGED 2")
                    _uiState.value = StatusState.Success(ScannedInfo("ACTION_STATE_CHANGED 2"))
                }
            }
        }
    }
}