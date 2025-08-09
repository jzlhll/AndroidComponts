package com.au.audiorecordplayer.bt.trandition;

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import com.au.audiorecordplayer.bt.BtUtil
import com.au.module_android.utils.ignoreError
import com.au.module_android.utils.logt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.withContext
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import kotlin.onFailure

@SuppressLint("MissingPermission")
class ServerConnector(val adapter: BluetoothAdapter) : IConnector{
    var callback: ((name:String, data: String?, isError: Boolean) -> Unit)?=null

    private var mServerSocket: BluetoothServerSocket? = null
    private val isSingleConnect = true

    private val clients = mutableListOf<OneClient>()

    private inner class OneClient(val socket: BluetoothSocket) : IConnector {
        val tag = "OnClient-$socket"

        val inputStream: InputStream = socket.inputStream
        val outputStream: OutputStream = socket.outputStream

        suspend fun startRead() {
            readThread(inputStream, socket.remoteDevice.name, { data, isError ->
                callback?.invoke(tag, data, isError)
                logt { "data $data isError $isError" }
                val backData = "Response: $data"
                if (isError) {
                    ignoreError { socket.close() }
                } else {
                    outputStream.write(backData.toByteArray())
                    outputStream.flush()
                }
            })
        }

        override fun shutdown() {
            ignoreError { shutdown() }
        }
    }

    /**
     * 放到IO线程执行
     */
    suspend fun prepare() {
        adapter.cancelDiscovery() //取消扫描，减少带宽占用

        withContext(Dispatchers.IO) {
            try {
                // 创建服务器socket（UUID需与客户端一致）
                val serverSocket = adapter.listenUsingRfcommWithServiceRecord("ClassicBT_Server", BtUtil.SERIAL_UUID)
                mServerSocket = serverSocket
                logt { "classic BT server preparing..." }
                // 阻塞等待客户端连接（只处理一次连接，如需多连接需循环）
                val clientSocket = serverSocket.accept()
                val oneClient = OneClient(clientSocket)
                clients.add(oneClient)
                logt { "classic BT server a new client come! " + oneClient.socket.remoteDevice }

                if (isSingleConnect) {
                    // 连接成功后，关闭serverSocket（单连接模式）
                    serverSocket.close()
                    oneClient.startRead()
                } else {
                    //开启独立线程去执行，并不阻碍之前的线程
                    supervisorScope {
                        // 后续流程：获取流、启动接收线程（同客户端）
                        oneClient.startRead()
                    }
                }
            } catch (e: IOException) {
                e.printStackTrace()
            } finally {
                shutdown()
            }
        }
        logt { "classic BT server after withContextIO" }
    }

    override fun shutdown() {
        clients.forEach {
            it.shutdown()
        }
        clients.clear()
        ignoreError { mServerSocket?.close() }
    }
}