package com.au.audiorecordplayer.bt.ble

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothProfile
import com.au.audiorecordplayer.bt.BtUtil.Companion.UUID_NOTIFICATION_DESCRIPTOR
import com.au.audiorecordplayer.util.MyLog

@SuppressLint("MissingPermission")
class BleConnector(private val context: android.content.Context,
    private val bluetoothDevice: BluetoothDevice) {
    // GATT连接对象
    private var bluetoothGatt: BluetoothGatt? = null
    // 连接状态回调
    var connectionStateCallback: ((state: Int) -> Unit)? = null
    // 数据接收回调 (特征值变化)
    var dataReceivedCallback: ((characteristic: BluetoothGattCharacteristic, data: ByteArray) -> Unit)? = null
    // 添加MTU变更回调
    var mtuChangedCallback: ((newMtu: Int, status: Int) -> Unit)? = null

    private var mValidMtu = -1

    /**
     * 连接设备
     */
    fun connect() {
        MyLog.d("Connecting to ${bluetoothDevice.address}")
        bluetoothGatt = bluetoothDevice.connectGatt(context, false, gattCallback)
    }

    /**
     * 断开连接
     * 清理资源
     */
    fun disConnectAndRelease() {
        mConnectState = BluetoothGatt.STATE_DISCONNECTED
        release()
    }

    fun release() {
        bluetoothGatt?.disconnect()
        bluetoothGatt?.close()
        bluetoothGatt = null
    }

    /**
     * 请求MTU大小 (Android 5.0+)
     * @param mtu 请求的MTU大小 (23-517)
     */
    fun requestMtu(mtu: Int) {
        if (bluetoothGatt == null) {
            MyLog.d("Cannot request MTU: not connected")
            return
        }

        // 检查MTU范围
        val validMtu = mtu.coerceIn(23, 517)
        if (mtu != validMtu) {
            MyLog.d("Adjusted MTU from $mtu to $validMtu")
        }

        // 发起MTU请求
        val success = bluetoothGatt?.requestMtu(validMtu) ?: false
        if (!success) {
            MyLog.e("Failed to initiate MTU request")
        } else {
            mValidMtu = validMtu
        }
    }
    
    /**
     * 写入特征值
     * @param characteristic 目标特征
     * @param data 要写入的数据
     * @param writeType 写入类型 (默认写入请求)
     */
    fun writeCharacteristic(
        characteristic: BluetoothGattCharacteristic,
        data: ByteArray,
        writeType: Int = BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
    ) {
        if (bluetoothGatt == null) {
            MyLog.d("GATT not connected")
            return
        }

        characteristic.writeType = writeType
        characteristic.value = data
        bluetoothGatt?.writeCharacteristic(characteristic)
    }

    /**
     * 读取特征值
     */
    fun readCharacteristic(characteristic: BluetoothGattCharacteristic) {
        bluetoothGatt?.readCharacteristic(characteristic)
    }

    /**
     * 启用/禁用特征通知
     */
    fun setCharacteristicNotification(
        characteristic: BluetoothGattCharacteristic,
        enable: Boolean
    ) {
        if (bluetoothGatt == null) return

        // 1. 设置本地通知
        bluetoothGatt?.setCharacteristicNotification(characteristic, enable)

        // 2. 写入描述器启用通知
        val descriptor = characteristic.getDescriptor(UUID_NOTIFICATION_DESCRIPTOR)
        descriptor?.value = if (enable) {
            BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
        } else {
            BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE
        }
        bluetoothGatt?.writeDescriptor(descriptor)
    }

    private var mConnectState = BluetoothGatt.STATE_DISCONNECTED
    fun isConnected(): Boolean {
        return mConnectState == BluetoothGatt.STATE_CONNECTED && mGatt != null
    }
    public boolean isPrepared() {
        return isConnected() && mClient != null && mWriteChar != null && mNotifyChar != null;
    }


    // GATT回调处理
    private val gattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            when (newState) {
                BluetoothProfile.STATE_CONNECTED -> {
                    MyLog.d("Connected to ${bluetoothDevice.address}")
                    gatt.discoverServices() // 开始发现服务
                }
                BluetoothProfile.STATE_DISCONNECTED -> {
                    MyLog.d("Disconnected from ${bluetoothDevice.address}")
                    release()
                }
            }
            connectionStateCallback?.invoke(newState)
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                MyLog.d("Services discovered")
                // 这里可以获取服务列表：gatt.services
            } else {
                MyLog.e("Service discovery failed: $status")
            }
        }

        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic
        ) {
            // 收到通知数据
            val data = characteristic.value
            dataReceivedCallback?.invoke(characteristic, data)
            MyLog.d("Received data: ${data?.toHexString()}")
        }

        override fun onCharacteristicWrite(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            status: Int
        ) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                MyLog.d("Characteristic write successful")
            } else {
                MyLog.e("Characteristic write failed: $status")
            }
        }

        override fun onCharacteristicRead(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            status: Int
        ) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                val data = characteristic.value
                dataReceivedCallback?.invoke(characteristic, data)
                MyLog.d("Characteristic read: ${data?.toHexString()}")
            } else {
                MyLog.d("Characteristic read failed: $status")
            }
        }

        override fun onDescriptorWrite(gatt: BluetoothGatt?, descriptor: BluetoothGattDescriptor?, status: Int) {
            super.onDescriptorWrite(gatt, descriptor, status)
        }

        override fun onMtuChanged(gatt: BluetoothGatt?, mtu: Int, status: Int) {
            super.onMtuChanged(gatt, mtu, status)
            if (status == BluetoothGatt.GATT_SUCCESS) {
                MyLog.d("MTU changed to $mtu bytes")
            } else {
                MyLog.d("MTU change failed: $status")
            }
            // 触发MTU变更回调
            //兼容android14的返回错误
            mtuChangedCallback?.invoke(if(mValidMtu >= 0) mValidMtu else mtu, status)
        }
    }
    
    // 扩展函数：字节数组转十六进制字符串
    private fun ByteArray.toHexString(): String {
        return joinToString("") { "%02X".format(it) }
    }
}