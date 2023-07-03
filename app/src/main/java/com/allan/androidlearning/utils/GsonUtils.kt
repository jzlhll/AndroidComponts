package com.allan.androidlearning.utils

import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type


val gson = GsonBuilder().create()

/**
 * 这里泛型只做约束使用
 */
abstract class GsonParameterizedType<T> : ParameterizedType


inline fun <reified T> String.fromJson(customType: GsonParameterizedType<T>? = null): T {
    if (T::class.java == String::class.java) {
        return this as T
    }
    return gson.fromJson(this, customType ?: object : TypeToken<T>() {}.type)
}

inline fun <reified T> String.fromJsonList(): List<T> {
    return fromJson(createGsonParameterizedType<List<T>> { //这里其实省略了List<T>。因此传给fromJson的实体类是List<XX>对象，
            arrayOf(T::class.java)
        })
}


inline fun <reified T> String.fromJsonList2(): List<T> {
    val listType = object : TypeToken<List<T>>() {}.type
    return gson.fromJson(this, listType)
}

//错误！
inline fun <reified T> String.fromJsonList3(): T { //JsonSyntaxException: java.lang.IllegalStateException: Expected BEGIN_ARRAY but was STRING at line 1 column 3 path $[0]
    return fromJson(createGsonParameterizedType {
        arrayOf(T::class.java)
    })
}

//错误！
inline fun <reified T> String.fromJsonList4(): T { //JsonSyntaxException: java.lang.IllegalStateException: Expected BEGIN_ARRAY but was STRING at line 1 column 3 path $[0]
    val listType = object : TypeToken<List<T>>() {}.type
    return gson.fromJson(this, listType)
}


inline fun <reified T> createGsonParameterizedType(
    crossinline actualTypeArguments: () -> Array<Type>,
): GsonParameterizedType<T> {
    return object : GsonParameterizedType<T>() {
        override fun getActualTypeArguments(): Array<Type> {
            return actualTypeArguments.invoke()
        }

        override fun getRawType(): Type {
            return T::class.java
        }

        override fun getOwnerType() = null
    }
}

fun testGsonList() {
    val text = """
        ["0"
        ,"1"
        ,"2"
        ,"3"
        ,"4"
        ,"5"
        ]
    """.trimIndent()
    val listStr:List<String> = text.fromJson(createGsonParameterizedType<List<String>> {
        arrayOf(String::class.java)
    })
    logd("listStr $listStr")
}


class CommonResponse<T, G> {
    var status = 0
    var data: T? = null
    var data2:G? = null
}
class CommonResponse2 {
    var status = 0
    var data: Any? = null
}

class DataInfo {
    var name: String? = null
}


fun testGsonData() {
    val text = "{\"status\":102,\"data\":{\"name\":\"dog\"}, \"data2\":{\"name\":\"dog2\"}}"
    // IllegalStateException: Expected a string but was BEGIN_OBJECT at line 1 column 22 path $.data
    //val data:CommonResponse<String> = text.fromJson()
    //正确：通过传入Type，明确指定了我们需要解析的泛型是什么。
    val data:CommonResponse<DataInfo, DataInfo> = text.fromJson(
        createGsonParameterizedType {
            arrayOf(DataInfo::class.java, DataInfo::class.java)
        })


    logd("data $data")
    //LinkedTreeMap  {name:dog} ： 因为是Any即Object，gson默认都是处理成基础类型或者List，Map
    val data2 = gson.fromJson<CommonResponse2>(text, CommonResponse2::class.java)

    //错误：不符合预期，这里得到的与data2一样，是LinkedTreeMap  {name:dog}。
    // 虽然我们写了泛型，但是由于这里没有实际意义，编译后被擦除，最终与上述差不多
    val data3 = gson.fromJson<CommonResponse<DataInfo, DataInfo>>(text, CommonResponse::class.java)

}