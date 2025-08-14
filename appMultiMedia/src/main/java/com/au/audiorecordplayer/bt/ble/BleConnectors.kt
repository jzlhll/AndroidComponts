package com.au.audiorecordplayer.bt.ble

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothProfile
import android.os.Build
import com.au.audiorecordplayer.bt.BtParams
import com.au.audiorecordplayer.bt.ble.BleUtil.PACKAGE_VALUE
import com.au.audiorecordplayer.bt.ble.BleUtil.SUBTYPE_CUSTOM_DATA
import com.au.audiorecordplayer.util.MyLog
import com.au.module_android.Globals
import com.au.module_android.utils.SingleCoroutineTaskExecutor
import com.au.module_android.utils.logt
import com.au.module_android.utils.unsafeLazy
import okhttp3.internal.toHexString
import java.util.Arrays
import java.util.concurrent.atomic.AtomicInteger

object BleConnectors {
    private val taskExecutor = SingleCoroutineTaskExecutor("BleConnectorsManager")

    private var expectMtu = 500 //BtParams.DEFAULT_MTU_LENGTH

    //key是设备的mac地址
    private var bleConnectorMap = mutableMapOf<String, BleConnector>()

    // 回调监听器列表
    private val callbackListeners = mutableListOf<BluetoothCallbackListener>()

    private val listener = object : IBluetoothCallbackListener {
        override fun onConnectionStateChanged(address: String, state: Int) {
            callbackListeners.forEach {
                if (it.observerAddressList().contains(address)) {
                    it.onConnectionStateChanged(address, state)
                }
            }
        }

        override fun onPrepared(address: String) {
            callbackListeners.forEach {
                if (it.observerAddressList().contains(address)) {
                    it.onPrepared(address)
                }
            }
        }

        override fun onDataCharChanged(address: String, data: ByteArray, str: String) {
            callbackListeners.forEach {
                if (it.observerAddressList().contains(address)) {
                    it.onDataCharChanged(address, data, str)
                }
            }
        }

        override fun onDataCharRead(address: String, data: ByteArray) {
            callbackListeners.forEach {
                if (it.observerAddressList().contains(address)) {
                    it.onDataCharRead(address, data)
                }
            }
        }

        override fun onMtuChanged(address: String, newMtu: Int, status: Int) {
            callbackListeners.forEach {
                if (it.observerAddressList().contains(address)) {
                    it.onMtuChanged(address, newMtu, status)
                }
            }
        }
    }

    // 注册监听器
    fun addListener(listener: BluetoothCallbackListener) {
        callbackListeners.add(listener)
    }

    // 注销监听器
    fun removeListener(listener: BluetoothCallbackListener) {
        callbackListeners.remove(listener)
    }

    fun getOrCreate(bluetoothDevice: BluetoothDevice?) : IBleConnector? {
        bluetoothDevice ?: return null

        var bleConnector = bleConnectorMap[bluetoothDevice.address]
        if (bleConnector == null) {
            bleConnector = BleConnector(bluetoothDevice)
            bleConnector.expectMtu = expectMtu
            add(bleConnector)
        }

        if (bleConnector.mBleListener == null) {
            bleConnector.mBleListener = listener
        }

        return bleConnector
    }

    private fun add(bleConnector: BleConnector) {
        taskExecutor.submit {
            synchronized(bleConnectorMap) {
                bleConnectorMap[bleConnector.address()] = bleConnector
            }
        }
    }

    fun getBleConnector(address: String): IBleConnector? {
        return bleConnectorMap[address]
    }

    private fun connectSerial(bleConnector: IBleConnector) {
        taskExecutor.submit {
            (bleConnector as BleConnector).connectInner()
        }
    }

    fun removeAndDisconnect(bleConnector: IBleConnector) {
        taskExecutor.submit {
            val connector: BleConnector?
            synchronized(bleConnectorMap) {
                connector = bleConnectorMap.remove(bleConnector.address())
            }
            connector?.disconnectAndRelease()
        }
    }

    fun removeAndDisconnect(address: String) {
        taskExecutor.submit {
            val bleConnector: BleConnector?
            synchronized(bleConnectorMap) {
                bleConnector = bleConnectorMap.remove(address)
            }
            bleConnector?.disconnectAndRelease()
        }
    }

    private suspend fun sendMessageSerial(bleConnector: BleConnector, data:String, writeType: Int?) : Boolean {
        return taskExecutor.submitWithResult {
            bleConnector.gattWrite(data, writeType)
        }
    }

    /**
     * 由于是单例。可能需要监听GlobalBackgroundCallback
     */
    fun destroy() {
        synchronized(bleConnectorMap) {
            bleConnectorMap.forEach {
                it.value.disconnectAndRelease()
            }
            bleConnectorMap.clear()
        }
        taskExecutor.shutdown()
        callbackListeners.clear()
    }

    @SuppressLint("MissingPermission")
    private class BleConnector : IBleConnector {
        val mAddress:String

        private val addrStr by unsafeLazy { System.identityHashCode(this@BleConnector) }
        val logTag by unsafeLazy { "Ble@${addrStr.toHexString()}($mAddress)" }

        override fun address() : String {
            return mAddress
        }

        val bluetoothDevice: BluetoothDevice

        constructor(bluetoothDevice: BluetoothDevice) {
            this.bluetoothDevice = bluetoothDevice
            this.mAddress = bluetoothDevice.address!!
        }

        var expectMtu:Int = BtParams.DEFAULT_MTU_LENGTH

        private var mSendSequence: AtomicInteger = AtomicInteger(-1)
        private fun generateSendSequence(): Int {
            return mSendSequence.incrementAndGet() and 0xff
        }

        // GATT连接对象
        private var mGatt: BluetoothGatt? = null
        private var mWriteChar: BluetoothGattCharacteristic? = null
        private var mNotifyChar: BluetoothGattCharacteristic? = null

        var mBleListener: IBluetoothCallbackListener? = null

        private var mConnectState = BluetoothGatt.STATE_DISCONNECTED
        private var mValidMtu = 0

        override fun connect() {
            connectSerial(this)
        }

        /**
         * 连接设备
         */
        @Throws
        fun connectInner() = if (mGatt == null) {
            MyLog.d("$logTag Connecting to ${bluetoothDevice.address}")
            mGatt = bluetoothDevice.connectGatt(Globals.app, false, gattCallback, BluetoothDevice.TRANSPORT_LE)
        } else {
            MyLog.w("$logTag mGatt is already exist.")
        }

        /**
         * 断开连接
         * 清理资源
         * 彻底销毁自己。
         */
        fun disconnectAndRelease() {
            mConnectState = BluetoothGatt.STATE_DISCONNECTED
            release()
            mBleListener?.onConnectionStateChanged(mAddress, BluetoothGatt.STATE_DISCONNECTED)
            mBleListener = null
        }

        /**
         * 并不会销毁自己。只是变成了未连接状态
         */
        private fun release() {
            mGatt?.let { gatt ->
                gatt.disconnect()
                gatt.close()
                mGatt = null
            }
            mNotifyChar = null
            mWriteChar = null
        }

        override suspend fun sendMessage(data: String, writeType: Int?): Boolean {
            return sendMessageSerial(this, data, writeType)
        }

        /**
         * 发送内容
         *
         *  writeType
         *    int WRITE_TYPE_DEFAULT      = 2; // 需要对方设备确认（可靠）
         *    int WRITE_TYPE_NO_RESPONSE  = 1; // 不要求对方响应（快速）
         *    int WRITE_TYPE_SIGNED       = 4; // 带签名的写入（需配对）
         */
        fun gattWrite(data: String, writeType: Int?) : Boolean{
            if (!isPrepared()) {
                return false
            }
            val gatt = mGatt!!
            val writeChar = mWriteChar!!
            //todo write delay

            val data = data.toByteArray()
            val type = BleUtil.getTypeValue(PACKAGE_VALUE, SUBTYPE_CUSTOM_DATA)
            val seqIndex = generateSendSequence()
            val dataLength = data.size
            //todo 参考blufi实现分包。根据是否需要分包来实现。
            val frameCtrl = 0//BleUtil.getFrameCTRLValue(false, false, BleUtil.DIRECTION_INPUT, false, todo)

            MyLog.d("$logTag gatt.writeCharacteristic orig ${data.toHexString()}")
            val addHeadsData = byteArrayOf(
                type.toByte(),
                seqIndex.toByte(),
                frameCtrl.toByte(),
                dataLength.toByte(),
                *data
            )
            if (writeType != null) {
                writeChar.writeType = writeType
            }
            if(!writeChar.setValue(addHeadsData)) {
                return false
            }
            val isSuc = gatt.writeCharacteristic(writeChar)
            MyLog.d("$logTag gatt.writeCharacteristic ${addHeadsData.toHexString()} ($dataLength/${addHeadsData.size}) isSuc $isSuc")
            return isSuc
        }

        /**
         * 请求MTU大小 (Android 5.0+)
         * @param mtu 请求的MTU大小 (23-517)
         */
        private fun requestMtu(mtu: Int) {
            if (mGatt == null) {
                MyLog.d("$logTag Cannot request MTU: not connected")
                return
            }

            // 检查MTU范围
            val validMtu = mtu.coerceIn(23, 517)
            if (mtu != validMtu) {
                MyLog.d("$logTag Adjusted MTU from $mtu to $validMtu")
            }

            // 发起MTU请求
            val success = mGatt?.requestMtu(validMtu) ?: false
            if (!success) {
                MyLog.e("$logTag Failed to initiate MTU request")
            } else {
                mValidMtu = validMtu
            }
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
            val descriptor = characteristic.getDescriptor(BtParams.UUID_NOTIFICATION_DESCRIPTOR)
            descriptor?.value = if (enable) {
                BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
            } else {
                BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE
            }
            gatt.writeDescriptor(descriptor)
        }

        override fun isConnected() : Boolean {
            return mConnectState == BluetoothGatt.STATE_CONNECTED && mGatt != null
        }

        override fun isPrepared() : Boolean {
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
                        MyLog.d("$logTag onConnection StateChange Connected to ${bluetoothDevice.address}")
                        gatt.discoverServices() // 开始发现服务
                    }
                    BluetoothProfile.STATE_DISCONNECTED -> {
                        MyLog.d("$logTag onConnection StateChange Disconnected from ${bluetoothDevice.address}")
                        release()
                    }
                }
                mBleListener?.onConnectionStateChanged(mAddress, newState)
            }

            override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    MyLog.d("$logTag Services discovered")
                    // 这里可以获取服务列表：gatt.services
                    val service = gatt.getService(BtParams.UUID_SERVICE)
                    var writeChar = service?.getCharacteristic(BtParams.UUID_WRITE_CHARACTERISTIC)
                    var notifyChar = service?.getCharacteristic(BtParams.UUID_NOTIFICATION_CHARACTERISTIC)
                    setCharacteristicNotification(notifyChar, true)
                    mWriteChar = writeChar
                    mNotifyChar = notifyChar

                    val notifyDesc = notifyChar?.getDescriptor(BtParams.UUID_NOTIFICATION_DESCRIPTOR)
                    notifyDesc?.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE)
                    gatt.writeDescriptor(notifyDesc)

                    mBleListener?.onPrepared(mAddress)
                    MyLog.d("$logTag onServicesDiscovered writeChar = $mWriteChar notifyChar = $mNotifyChar")
                } else {
                    MyLog.e("$logTag Service discovery failed: $status")
                }
            }

            override fun onCharacteristicChanged(
                gatt: BluetoothGatt,
                characteristic: BluetoothGattCharacteristic
            ) {
                // 增加是否加密通过判断
//                if (characteristic == mNotifyChar) {
//                    val data = characteristic.getValue()
//                    if (mPrintDebug) {
//                        Log.i(TAG, "Gatt Notification: " + data.contentToString())
//                    }
//                    // lt 0 is error, eq 0 is complete, gt 0 is continue
//                    val parse: Int = parseNotification(data, mNotifyData)
//                    if (parse < 0) {
//                        onError(BlufiCallback.CODE_INVALID_NOTIFICATION)
//                    } else if (parse == 0) {
//                        parseBlufiNotifyData(mNotifyData)
//                        mNotifyData = null
//                    }
//                }

                if (characteristic == mNotifyChar) {
                    // 收到通知数据
                    val data = characteristic.value
                    logt { "$logTag Received data: ${data?.size} first4bytes:${data.first4BytesStr()}" }
                    if (data.size > 4) {
                        val dataLength = if(data[3] >=0) data[3].toInt() else data[3].toInt() + 256
                        val cut4data = data.drop(4).toByteArray()
                        val str = String(cut4data)
                        logt { "$logTag len$dataLength Received data: $str" }
                        mBleListener?.onDataCharChanged(mAddress, data, str) //todo 参考blufi实现分包。根据是否有frag来组包
                    }
                }
            }

            override fun onCharacteristicWrite(
                gatt: BluetoothGatt,
                characteristic: BluetoothGattCharacteristic,
                status: Int
            ) {
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    MyLog.d("$logTag Characteristic write successful")
                } else {
                    MyLog.e("$logTag Characteristic write failed: $status")
                }
            }

            override fun onCharacteristicRead(
                gatt: BluetoothGatt,
                characteristic: BluetoothGattCharacteristic,
                status: Int
            ) {
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    val data = characteristic.value
                    mBleListener?.onDataCharRead(mAddress, data)
                    MyLog.d("$logTag Characteristic read: ${data?.toHexString()}")
                } else {
                    MyLog.d("$logTag Characteristic read failed: $status")
                }
            }

            override fun onDescriptorRead(gatt: BluetoothGatt, descriptor: BluetoothGattDescriptor, status: Int, value: ByteArray) {
                super.onDescriptorRead(gatt, descriptor, status, value)
                MyLog.d("$logTag on Descriptor Read: $status")
            }

            override fun onDescriptorWrite(gatt: BluetoothGatt?, descriptor: BluetoothGattDescriptor?, status: Int) {
                super.onDescriptorWrite(gatt, descriptor, status)
                var isSuc = status == BluetoothGatt.GATT_SUCCESS
                MyLog.d("$logTag on Descriptor Write: $status=$isSuc")
                if (descriptor?.uuid == BtParams.UUID_NOTIFICATION_DESCRIPTOR &&
                    descriptor.characteristic.uuid == BtParams.UUID_NOTIFICATION_CHARACTERISTIC) {
                    requestMtu(expectMtu)
                }
            }

            override fun onReliableWriteCompleted(gatt: BluetoothGatt?, status: Int) {
                super.onReliableWriteCompleted(gatt, status)
                MyLog.d("$logTag onReliable Write Completed: $status")
            }

            override fun onReadRemoteRssi(gatt: BluetoothGatt?, rssi: Int, status: Int) {
                super.onReadRemoteRssi(gatt, rssi, status)
                MyLog.d("$logTag on Read RemoteRssi: rssi $rssi, $status")
            }

            override fun onMtuChanged(gatt: BluetoothGatt?, mtu: Int, status: Int) {
                super.onMtuChanged(gatt, mtu, status)
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    MyLog.d("$logTag MTU changed to $mtu bytes")
                } else {
                    MyLog.d("$logTag MTU change failed: $status")
                }
                // 触发MTU变更回调
                //兼容android14的返回错误,这里返回的mtu可能是设备端不准确
                mBleListener?.onMtuChanged(mAddress, if(mValidMtu > 0) mValidMtu else mtu, status)
            }
        }

        // 扩展函数：字节数组转十六进制字符串
        private fun ByteArray.toHexString(): String {
            return Arrays.toString(this)
            //return joinToString("") { "%02X".format(it) }
        }

        private fun ByteArray.first4BytesStr(): String {
            val bytes = take(4).toByteArray()
            return bytes.toHexString()
        }
    }
}