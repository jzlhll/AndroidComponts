package com.au.audiorecordplayer.ble

import android.annotation.SuppressLint
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.os.ParcelUuid
import com.au.audiorecordplayer.util.MyLog
import com.au.module_android.simpleflow.StatusState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.UUID

class MyBleScanner {
    private val _uiState : MutableStateFlow<StatusState<ScannedInfo>> = MutableStateFlow(StatusState.Loading)
    val uiState: StateFlow<StatusState<ScannedInfo>> = _uiState

    private var mScanSettings : ScanSettings? = null
    /**
     *  功能描述： 过滤列表，如果添加了就必须有过滤条件，否则会什么都扫不到
     **/
    private val scanFilterList = mutableListOf<ScanFilter>()

    init {
        //添加扫描过滤广播
        setCustomFilter(createFilter(UUID_SERVICE.toString()))
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
        if (scanFilterList.isEmpty()) {
            leScanner.startScan(scanCallback)
        } else {
            leScanner.startScan(scanFilterList, getScanSettings(), scanCallback)
        }
    }

    @SuppressLint("MissingPermission")
    fun stopLeScan(leScanner: BluetoothLeScanner?) {
        leScanner?:return
        leScanner.stopScan(scanCallback)
    }

    val scanCallback = object : ScanCallback() {
        override
        fun onScanResult(callbackType: Int, result: ScanResult?) {
            result?:return
            onOneScanResult(result, "ScanCallback onScan Result")
        }

        override
        fun onBatchScanResults(results: MutableList<ScanResult>?) {
            results?.forEach {
                onOneScanResult(it, "ScanCallback onBatch Scan Results")
            }
        }
     }

    @SuppressLint("MissingPermission")
    private fun onOneScanResult(result: ScanResult, from:String) {
        val device = result.device
        val name = device.name
        val address = device.address
        val log = "$from: $name, $address"
        MyLog.d(log)
        _uiState.value = StatusState.Success(ScannedInfo(from, name, address))
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

    companion object {
        /**
         * 服务发现的UUID
         */
        val UUID_SERVICE: UUID = UUID.fromString("0000ffff-0000-1000-8000-00805f9b34fb")
        val UUID_WRITE_CHARACTERISTIC: UUID = UUID.fromString("0000ff01-0000-1000-8000-00805f9b34fb")
        val UUID_NOTIFICATION_CHARACTERISTIC: UUID = UUID.fromString("0000ff02-0000-1000-8000-00805f9b34fb")
        val UUID_NOTIFICATION_DESCRIPTOR: UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")

        fun createFilter(uuid: String): ScanFilter {
            val builder = ScanFilter.Builder().also {
                it.setServiceUuid(ParcelUuid.fromString(uuid))
            }
            return builder.build()
        }
    }
}