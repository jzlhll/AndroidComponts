package com.au.audiorecordplayer.bt.ble

import android.annotation.SuppressLint
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import com.au.audiorecordplayer.bt.BtUtil
import com.au.audiorecordplayer.bt.bean.BleScannedDevice
import com.au.audiorecordplayer.util.MyLog
import com.au.module_android.simpleflow.StatusState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.concurrent.ConcurrentHashMap

class BleScanner {
    private val _uiState : MutableStateFlow<StatusState<String>> = MutableStateFlow(StatusState.Loading)
    val uiState: StateFlow<StatusState<String>> = _uiState

    /**
     * key 是 address。
     */
    private val allBleDevices = ConcurrentHashMap<String, BleScannedDevice>()

    private var mScanSettings : ScanSettings? = null
    /**
     *  功能描述： 过滤列表，如果添加了就必须有过滤条件，否则会什么都扫不到
     **/
    private val scanFilterList = mutableListOf<ScanFilter>()

    init {
        //添加扫描过滤广播
        setCustomFilter(BtUtil.createFilter(BtUtil.UUID_SERVICE.toString()))
    }

    fun getScanSettings() : ScanSettings {
        val s = mScanSettings
        if (s != null) {
            return s
        }

        val builder = ScanSettings.Builder()
        builder.setScanMode(ScanSettings.SCAN_MODE_BALANCED)
            .setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES)
        return builder.build().also { mScanSettings = it }
    }

    @SuppressLint("MissingPermission")
    fun startLeScan(leScanner: BluetoothLeScanner?) {
        leScanner?:return
        MyLog.d("start le scan $scanFilterList")
        if (scanFilterList.isEmpty()) {
            leScanner.startScan(scanCallback)
        } else {
            leScanner.startScan(scanFilterList, getScanSettings(), scanCallback)
        }
    }

    @SuppressLint("MissingPermission")
    fun stopLeScan(leScanner: BluetoothLeScanner?) {
        leScanner?:return
        MyLog.d("stop le scan")
        leScanner.stopScan(scanCallback)
        allBleDevices.clear()
        _uiState.value = StatusState.Success("stopped ble scan")
    }

    val scanCallback = object : ScanCallback() {
        override
        fun onScanResult(callbackType: Int, result: ScanResult?) {
            result?:return
            synchronized(allBleDevices) {
                onOneScanResultUnlock(result)
                updateDevicesUiStateUnlock()
            }
        }

        override
        fun onBatchScanResults(results: MutableList<ScanResult>?) {
            synchronized(allBleDevices) {
                results?.forEach {
                    onOneScanResultUnlock(it)
                }
                updateDevicesUiStateUnlock()
            }
        }
     }

    private fun onOneScanResultUnlock(result: ScanResult) {
        val device = result.device
        allBleDevices[device.address] = BleScannedDevice(device, result.rssi)
    }

    @SuppressLint("MissingPermission")
    private fun updateDevicesUiStateUnlock() {
        val sb = StringBuilder()
        val list = allBleDevices.values.sortedByDescending { it.rssi }
        list.forEach {
            sb.append("${it.device.address} ${it.rssi} ${it.device.name} \n")
        }
        _uiState.value = StatusState.Success(sb.toString())
    }

    /**
     *  功能描述： 设置自定义的过滤，必须有条件
     **/
    fun setCustomFilter(vararg filter: ScanFilter) {
        scanFilterList.clear()
        filter.forEach {
            scanFilterList.add(it)
        }
    }
}