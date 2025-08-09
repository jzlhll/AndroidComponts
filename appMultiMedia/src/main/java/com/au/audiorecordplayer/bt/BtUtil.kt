package com.au.audiorecordplayer.bt

import android.bluetooth.le.ScanFilter
import android.os.ParcelUuid
import java.util.UUID

class BtUtil {
    companion object {
        /**
         * 服务发现的UUID
         */
        val UUID_SERVICE: UUID = UUID.fromString("0000ffff-0000-1000-8000-00805f9b34fb")
        val UUID_WRITE_CHARACTERISTIC: UUID = UUID.fromString("0000ff01-0000-1000-8000-00805f9b34fb")
        val UUID_NOTIFICATION_CHARACTERISTIC: UUID = UUID.fromString("0000ff02-0000-1000-8000-00805f9b34fb")
        val UUID_NOTIFICATION_DESCRIPTOR: UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")

        /**
         * 经典蓝牙串口通信UUID
         */
        const val SERIAL_UUID_SERVICE = "00001101-0000-1000-8000-00805F9B34FB"
        val SERIAL_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")

        /**
         * 创建BLE的过滤条件
         */
        fun createFilter(uuid: String): ScanFilter {
            val builder = ScanFilter.Builder().also {
                it.setServiceUuid(ParcelUuid.fromString(uuid))
            }
            return builder.build()
        }
    }
}