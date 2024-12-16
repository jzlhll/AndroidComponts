package com.au.module_android.utils

import android.content.Context
import android.os.Parcel
import android.os.Parcelable
import android.os.Parcelable.Creator
import android.util.Base64
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream

/**
 * @author au
 * @date :2023/10/30 16:15
 * @description: 工具类。用来序列化和反序列化为文字。
 */
fun parcelableSave(context: Context, parcelable: Parcelable?, file: File) {
    try {
        FileOutputStream(file).use { out ->
            BufferedOutputStream(out).use { bos ->
                if (!file.exists()) {
                    file.createNewFile()
                }
                val parcel = Parcel.obtain()
                parcel.writeParcelable(parcelable, 0)
                bos.write(parcel.marshall())
                bos.flush()
                out.flush()
                parcel.recycle()
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

/**
 * 将Parcelable变成二进制；再行序列化为String
 */
fun parcelableToStr(obj: Parcelable): String {
    // 1.序列化
    val p = Parcel.obtain()
    obj.writeToParcel(p, 0)
    val bytes = p.marshall()
    p.recycle()

    // 2.编码
    return Base64.encodeToString(bytes, Base64.DEFAULT)
}

private fun parcelableUnmarshall(bytes: ByteArray): Parcel {
    val parcel = Parcel.obtain()
    parcel.unmarshall(bytes, 0, bytes.size)
    parcel.setDataPosition(0) // this is extremely important!
    return parcel
}

/**
 * 将使用我们的反序列化；反序列化为Parcelable
 */
fun <T> parcelableUnmarshall(str: String?, creator: Creator<T>): T {
    // 1.解码
    val bytes = Base64.decode(str, Base64.DEFAULT)
    // 2.反序列化
    val parcel = parcelableUnmarshall(bytes)
    return creator.createFromParcel(parcel)
}
