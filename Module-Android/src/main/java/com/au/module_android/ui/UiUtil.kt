package com.au.module_android.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.viewbinding.ViewBinding
import com.au.module_android.ui.base.IFullWindow
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

private fun <T> findViewBinding(javaClass:Class<*>, typeIndex:Int = 0) : Class<T>? {
    val parameterizedType = javaClass.getParameterizedType() ?: return null
    val actualTypeArguments = parameterizedType.actualTypeArguments
    val type = actualTypeArguments[typeIndex]
    if ((ViewBinding::class.java).isAssignableFrom(type as Class<*>)) {
        return type as Class<T>
    }
    return null
}

fun <T : ViewBinding> createViewBinding(self: Class<*>, inflater: LayoutInflater, container: ViewGroup?, attach: Boolean): T {
    var clz: Class<T>? = findViewBinding(self)
    //修正框架，允许往上寻找3层superClass的第一个泛型做为ViewBinding
    if (clz == null) {
        val superClass = self.superclass
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

/**
 * 暂时不继续往父类查找。
 */
fun <T : ViewBinding> createViewBindingT2(self: Class<*>, inflater: LayoutInflater, container:ViewGroup, isContentMergeXml:Boolean): T {
    val clz: Class<T> = findViewBinding(self, 1) ?: throw IllegalArgumentException("需要一个ViewBinding类型的泛型") //不再向上去找
    return if (isContentMergeXml) {
        clz.getMethod("inflate",
            LayoutInflater::class.java,
            ViewGroup::class.java).invoke(null, inflater, container) as T
    } else {
        clz.getMethod(
            "inflate",
            LayoutInflater::class.java,
            ViewGroup::class.java,
            Boolean::class.java
        ).invoke(null, inflater, null, false) as T
    }
}

/**
 * 子类调用。
 * 所以子类不得再调用window.decorView
 * ViewCompat.setOnApplyWindowInsetsListener(window.decorView)
 */
fun IFullWindow.fullPaddingEdgeToEdge(activity: ComponentActivity, window: Window, updatePaddingRoot: View) {
    val isPaddingNav = isPaddingNavBar()
    val isPaddingStatusBar = isPaddingStatusBar()

    if(fullWindowSetEdgeToEdge()) activity.enableEdgeToEdge()

    if (isPaddingNav || isPaddingStatusBar) {
        ViewCompat.setOnApplyWindowInsetsListener(window.decorView) { _, insets ->
            val statusBarsHeight = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top
            val bottomBarHeight = insets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom

            if (isPaddingStatusBar) {
                if (isPaddingNav) {
                    updatePaddingRoot.updatePadding(top = statusBarsHeight, bottom = bottomBarHeight)
                } else {
                    updatePaddingRoot.updatePadding(top = statusBarsHeight)
                }
            } else {
                updatePaddingRoot.updatePadding(bottom = bottomBarHeight)
            }

            insets
        }
    }
}