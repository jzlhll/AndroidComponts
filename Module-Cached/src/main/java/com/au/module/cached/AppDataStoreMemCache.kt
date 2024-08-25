package com.au.module.cached

/**
 * @author au
 * @date :2024/7/16 9:59
 * @description: 对于一些到处需要读取，使用内存缓存来降低对于文件的读取。
 * 与AppDataStore相同，只允许
 *             Int::class.java
 *             Long::class.java
 *             Double::class.java
 *             Float::class.java
 *             Boolean::class.java
 *             String::class.java
 *             ByteArray::class.java
 */
class AppDataStoreMemCache(val key:String, val defaultValue:Any) {
    private var data:Any?= null
    fun readInt() : Int {
        return AppDataStore.readBlocked(key, defaultValue as Int)
    }
    fun readLong() : Long {
        return AppDataStore.readBlocked(key, defaultValue as Long)
    }
    fun readDouble() : Double {
        return AppDataStore.readBlocked(key, defaultValue as Double)
    }
    fun readFloat() : Float {
        return AppDataStore.readBlocked(key, defaultValue as Float)
    }
    fun readBoolean() : Boolean {
        return AppDataStore.readBlocked(key, defaultValue as Boolean)
    }
    fun readByteArray() : ByteArray {
        return AppDataStore.readBlocked(key, defaultValue as ByteArray)
    }
    fun readString() : String {
        return AppDataStore.readBlocked(key, defaultValue as String)
    }

    fun save(value: Any) {
        AppDataStore.save(key, value)
    }
}