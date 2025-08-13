package com.au.audiorecordplayer.bt.ble

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic

class BleConnector(private val btDevice: BluetoothDevice) {
    // GATT连接对象
    private var bluetoothGatt: BluetoothGatt? = null
    // 连接状态回调
    var connectionStateCallback: ((state: Int) -> Unit)? = null
    // 数据接收回调 (特征值变化)
    var dataReceivedCallback: ((characteristic: BluetoothGattCharacteristic, data: ByteArray) -> Unit)? = null
}