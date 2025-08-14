package com.au.audiorecordplayer.bt.trandition;

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothSocket
import com.au.audiorecordplayer.bt.BtParams
import com.au.module_android.utils.ignoreError
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.onFailure

@SuppressLint("MissingPermission")
class ClientConnector(val adapter: BluetoothAdapter) : IConnector {
    private var mSocket: BluetoothSocket? = null

    suspend fun connect(address:String, sucCb:(BluetoothSocket)->Unit, failCb:()->Unit) {
        adapter.cancelDiscovery() //取消扫描，减少带宽占用

        withContext(Dispatchers.IO) {
            runCatching {
                val device = adapter.getRemoteDevice(address)
                val socket = device.createRfcommSocketToServiceRecord(BtParams.SERIAL_UUID)
                mSocket = socket
                // 建立连接（阻塞操作，需在子线程）
                socket.connect()
                // 连接成功，获取读写流
                sucCb(socket) //回调成功，你再行开启读写线程
            }.onFailure {
                // 关闭socket
                shutdown()
                failCb()
            }
        }
    }

    override fun shutdown() {
        ignoreError {
            mSocket?.close()
        }
    }
}