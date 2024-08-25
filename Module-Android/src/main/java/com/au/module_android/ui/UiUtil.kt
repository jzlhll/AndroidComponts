package com.au.module_android.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.viewbinding.ViewBinding
import java.lang.reflect.ParameterizedType

/**
 * 将class转为ParameterizedType，方便获取此类的类泛型
 */
private fun Class<*>?.getParameterizedType(): ParameterizedType? {
    if (this == null) {
        return null
    }
    val type = this.genericSuperclass
    return if (type == null || type !is ParameterizedType) {
        this.superclass.getParameterizedType()
    } else {
        type
    }
}

private fun <T> findViewBinding(javaClass:Class<*>) : Class<T>? {
    val parameterizedType = javaClass.getParameterizedType() ?: return null
    val actualTypeArguments = parameterizedType.actualTypeArguments
    val type = actualTypeArguments[0]
    if ((ViewBinding::class.java).isAssignableFrom(type as Class<*>)) {
        return type as Class<T>
    }
    return null
}

fun <T : ViewBinding> createViewBinding(self: Class<*>, inflater: LayoutInflater, container: ViewGroup?, attach: Boolean): T {
    var clz: Class<T>? = findViewBinding(self)
    //修正框架，允许往上寻找3层superClass的第一个泛型做为ViewBinding
    if (clz == null) {
        val superClass = self.javaClass.superclass
        if (superClass != null) {
            clz = findViewBinding(superClass) ?: superClass.superclass?.let { findViewBinding(it) }
        }
    }
    if (clz == null) throw IllegalArgumentException("需要一个ViewBinding类型的泛型")
    return clz.getMethod(
        "inflate",
        LayoutInflater::class.java,
        ViewGroup::class.java,
        Boolean::class.java
    ).invoke(null, inflater, container, attach) as T
}