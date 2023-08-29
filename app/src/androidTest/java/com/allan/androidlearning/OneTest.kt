package com.allan.androidlearning

import java.lang.Integer.max

/**
 * @author allan.jiang
 * Date: 2023/8/29
 * Description TODO
 */
class OneTest {
    data class BytesCache(val bytes: ByteArray,
                          var isUsing:Boolean,
                          val size:Int)

    private val cachedList = ArrayList<BytesCache>(4)

    private fun findCache(fixSize:Int):BytesCache {
        synchronized(cachedList) {
            for (c in cachedList) {
                if (!c.isUsing && c.size == fixSize) {
                    c.isUsing = true
                    return c
                }
            }
            val c = BytesCache(ByteArray(fixSize), true, fixSize)
            cachedList.add(c)
            return c
        }
    }

    fun obtain(size: Int, useBlock:(ByteArray)->Unit) {
        if (size > 4096) { //todo 不太可能超过4k。直接给出使用。
            useBlock(ByteArray(size))
            return
        }

        val fixSize = if (size > 2048) {
            4096
        } else {
            2048
        }

        val c = findCache(fixSize)

        useBlock(c.bytes)

        recycle(c)
    }

    private fun recycle(bc: BytesCache) {
        synchronized(cachedList) {
            bc.isUsing = false
            if (cachedList.size > 8) {
                cachedList.remove(bc)
            }
        }
    }
}