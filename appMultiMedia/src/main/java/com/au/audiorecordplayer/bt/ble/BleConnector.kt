package com.au.audiorecordplayer.bt.ble

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothProfile
import com.au.audiorecordplayer.bt.BtUtil
import com.au.audiorecordplayer.util.MyLog


@SuppressLint("MissingPermission")
class BleConnector private constructor(private val context: android.content.Context,
                   private val expectMtu:Int,
    val bluetoothDevice: BluetoothDevice) {
    val address:String = bluetoothDevice.address!!

    // GATT连接对象
    private var mGatt: BluetoothGatt? = null
    private var mWriteChar: BluetoothGattCharacteristic? = null
    private var mNotifyChar: BluetoothGattCharacteristic? = null

    // 连接状态回调
    var connectionStateCallback: ((addressTag:String, state: Int) -> Unit)? = null
    // 接收到了数据回调
    var onDataCharChangedCallback: ((addressTag:String, data: ByteArray) -> Unit)? = null
    // 数据read回调 (特征值变化)
    var onDataCharReadCallback: ((addressTag:String, data: ByteArray) -> Unit)? = null
    // 添加MTU变更回调
    var mtuChangedCallback: ((addressTag:String, newMtu: Int, status: Int) -> Unit)? = null

    private var mConnectState = BluetoothGatt.STATE_DISCONNECTED
    private var mValidMtu = 0

    /**
     * 连接设备
     */
    fun connect() {
        MyLog.d("Connecting to ${bluetoothDevice.address}")
        mGatt = bluetoothDevice.connectGatt(context, false, gattCallback)
    }

    /**
     * 断开连接
     * 清理资源
     */
    fun disconnectAndRelease() {
        mConnectState = BluetoothGatt.STATE_DISCONNECTED
        release()
    }

    private fun release() {
        mGatt?.let { gatt ->
            gatt.disconnect()
            gatt.close()
            mGatt = null
        }
        mNotifyChar = null
        mWriteChar = null

        onDataCharChangedCallback = null
        onDataCharReadCallback = null
        mtuChangedCallback = null
        connectionStateCallback = null
    }

    /**
     * 发送内容
     *
     *  writeType
     *    int WRITE_TYPE_DEFAULT      = 2; // 需要对方设备确认（可靠）
     *    int WRITE_TYPE_NO_RESPONSE  = 1; // 不要求对方响应（快速）
     *    int WRITE_TYPE_SIGNED       = 4; // 带签名的写入（需配对）
     */
    fun gattWrite(data: ByteArray,
                  writeType: Int? = null) : Boolean{
        if (!isConnected()) {
            return false
        }
        val gatt = mGatt
        val writeChar = mWriteChar
        if (gatt == null || writeChar == null) {
            MyLog.e("gattWrite:: mGatt =  " + mGatt + "m WriteChar =  " + writeChar)
            return false
        }

        //todo write delay

        if(!writeChar.setValue(data)) {
            return false
        }
        if (writeType != null) {
            writeChar.writeType = writeType
        }
        return gatt.writeCharacteristic(writeChar)
    }

    /**
     * 请求MTU大小 (Android 5.0+)
     * @param mtu 请求的MTU大小 (23-517)
     */
    private fun requestMtu(mtu: Int) {
        if (mGatt == null) {
            MyLog.d("Cannot request MTU: not connected")
            return
        }

        // 检查MTU范围
        val validMtu = mtu.coerceIn(23, 517)
        if (mtu != validMtu) {
            MyLog.d("Adjusted MTU from $mtu to $validMtu")
        }

        // 发起MTU请求
        val success = mGatt?.requestMtu(validMtu) ?: false
        if (!success) {
            MyLog.e("Failed to initiate MTU request")
        } else {
            mValidMtu = validMtu
        }
    }

    /**
     * 读取特征值
     */
    fun readCharacteristic(characteristic: BluetoothGattCharacteristic) {
        mGatt?.readCharacteristic(characteristic)
    }

    /**
     * 启用/禁用特征通知
     */
    private fun setCharacteristicNotification(
        characteristic: BluetoothGattCharacteristic?,
        enable: Boolean
    ) {
        val gatt = mGatt
        if (gatt == null) return
        if (characteristic == null) return

        // 1. 设置本地通知
        gatt.setCharacteristicNotification(characteristic, enable)

        // 2. 写入描述器启用通知
        val descriptor = characteristic.getDescriptor(BtUtil.UUID_NOTIFICATION_DESCRIPTOR)
        descriptor?.value = if (enable) {
            BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
        } else {
            BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE
        }
        gatt.writeDescriptor(descriptor)
    }


    fun isConnected(): Boolean {
        return mConnectState == BluetoothGatt.STATE_CONNECTED && mGatt != null
    }

    fun isPrepared() : Boolean {
        return isConnected() && mWriteChar != null && mNotifyChar != null
    }

    // GATT回调处理
    private val gattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            mConnectState = newState
            when (newState) {
                BluetoothProfile.STATE_CONNECTED -> {
                    if (status == BluetoothGatt.GATT_SUCCESS) {
                        gatt.requestConnectionPriority(BluetoothGatt.CONNECTION_PRIORITY_HIGH)
                    }
                    MyLog.d("Connected to ${bluetoothDevice.address}")
                    gatt.discoverServices() // 开始发现服务
                }
                BluetoothProfile.STATE_DISCONNECTED -> {
                    MyLog.d("Disconnected from ${bluetoothDevice.address}")
                    release()
                }
            }
            connectionStateCallback?.invoke(address, newState)
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                MyLog.d("Services discovered")
                // 这里可以获取服务列表：gatt.services
                val service = gatt.getService(BtUtil.UUID_SERVICE)
                var writeChar = service?.getCharacteristic(BtUtil.UUID_WRITE_CHARACTERISTIC)
                var notifyChar = service?.getCharacteristic(BtUtil.UUID_NOTIFICATION_CHARACTERISTIC)
                setCharacteristicNotification(notifyChar, true)
                mWriteChar = writeChar
                mNotifyChar = notifyChar
                MyLog.d("onServicesDiscovered writeChar = $mWriteChar mNotifyChar = $mNotifyChar")
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
            onDataCharChangedCallback?.invoke(address, data)
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
                onDataCharReadCallback?.invoke(address, data)
                MyLog.d("Characteristic read: ${data?.toHexString()}")
            } else {
                MyLog.d("Characteristic read failed: $status")
            }
        }

        override fun onDescriptorRead(gatt: BluetoothGatt, descriptor: BluetoothGattDescriptor, status: Int, value: ByteArray) {
            super.onDescriptorRead(gatt, descriptor, status, value)
            MyLog.d("on Descriptor Read: $status")
        }

        override fun onDescriptorWrite(gatt: BluetoothGatt?, descriptor: BluetoothGattDescriptor?, status: Int) {
            super.onDescriptorWrite(gatt, descriptor, status)
            MyLog.d("on Descriptor Write: $status")
            if (descriptor?.uuid == BtUtil.UUID_NOTIFICATION_DESCRIPTOR &&
                descriptor.characteristic.uuid == BtUtil.UUID_NOTIFICATION_CHARACTERISTIC) {
                requestMtu(expectMtu)
            }
        }

        override fun onReliableWriteCompleted(gatt: BluetoothGatt?, status: Int) {
            super.onReliableWriteCompleted(gatt, status)
            MyLog.d("onReliable Write Completed: $status")
        }

        override fun onReadRemoteRssi(gatt: BluetoothGatt?, rssi: Int, status: Int) {
            super.onReadRemoteRssi(gatt, rssi, status)
            MyLog.d("on Read RemoteRssi: rssi $rssi, $status")
        }

        override fun onMtuChanged(gatt: BluetoothGatt?, mtu: Int, status: Int) {
            super.onMtuChanged(gatt, mtu, status)
            if (status == BluetoothGatt.GATT_SUCCESS) {
                MyLog.d("MTU changed to $mtu bytes")
            } else {
                MyLog.d("MTU change failed: $status")
            }
            // 触发MTU变更回调
            //兼容android14的返回错误,这里返回的mtu可能是设备端不准确
            mtuChangedCallback?.invoke(address, if(mValidMtu > 0) mValidMtu else mtu, status)
        }
    }
    
    // 扩展函数：字节数组转十六进制字符串
    private fun ByteArray.toHexString(): String {
        return joinToString("") { "%02X".format(it) }
    }
}