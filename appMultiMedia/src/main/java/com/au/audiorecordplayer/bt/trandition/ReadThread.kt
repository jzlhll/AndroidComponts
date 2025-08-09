package com.au.audiorecordplayer.bt.trandition

import com.au.module_android.utils.ignoreError
import com.au.module_android.utils.logt
import kotlinx.coroutines.delay
import java.io.InputStream
import java.nio.charset.StandardCharsets

/**
 * 起一个线程读取
 */
suspend fun IConnector.readThread(inputStream: InputStream, name:String, callback: ((data: String?, isError: Boolean) -> Unit)) {
    delay(0)
    val buffer = ByteArray(1024)
    var bytes:Int
    while (true) {
        try {
            // 读取数据（阻塞操作，无数据时会等待）
            bytes = inputStream.read(buffer)
            if (bytes > 0) {
                val received = buffer.copyOfRange(0, bytes)
                val data = ignoreError {
                    String(received, StandardCharsets.UTF_8)
                }
                logt{"$name 收到数据：$data"}
                callback(data, true)
            }
        } catch (e: Exception) {
            // 读取失败（通常是连接断开）
            e.printStackTrace()
            logt{"$name ex ${e.message}"}
            callback(e.message, false)
            break
        }
    }
}