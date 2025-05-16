package com.allan.androidlearning.transfer.nanohttp.h5client

import com.au.module_android.utils.logd
import java.nio.ByteBuffer
import java.nio.ByteOrder

class BufferManager(val chunkSize:Int) {
    init {
        logd { "chunkSize $chunkSize" }
    }
    private val preSize = 32 + 4 + 4 + 4 + 4

    //如果是buildChunkPacket的dataSize刚好是chunkSize，则可以利用cache
    private val buffer = ByteBuffer.allocate(preSize + chunkSize).also {
        it.order(ByteOrder.BIG_ENDIAN)  // 统一使用大端序
    }

    /**
     * 发送给前端的切片协议
     *
     */
    fun buildChunkPacket(
        uriUuid: String,
        index: Int,
        total: Int,
        offset: Int,
        dataSize:Int,
        data: ByteArray,
    ): ByteArray {
        val uuidBytes = uriUuid.toByteArray(Charsets.UTF_8)
        logd { "serverChunk: ${uuidBytes.size} index:$index/$total, dataSize:$dataSize - $offset" }
        buffer.apply {
            clear()
            put(uuidBytes)               // 32字节
            putInt(index)                // 4字节index
            putInt(total)                // 4字节total
            putInt(offset)               // 4字节offset
            putInt(dataSize)             // 4字节dataSize
            put(data)                    // 变长数据体
        }
        return buffer.array()
    }
}