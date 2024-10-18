package com.au.module_android.ui.bindings

import androidx.viewbinding.ViewBinding
import com.au.module_android.utils.asOrNull
import java.util.concurrent.ConcurrentHashMap

/**
 * @author au
 * 简化和传入复杂的参数类型。
 * 主要使用场景为做为整个activity的唯一。而且不能跨进程。
 */
abstract class BindingParamsFragment<Binding : ViewBinding> : BindingFragment<Binding>() {

    companion object {
        val paramsMap = ConcurrentHashMap<String, Any>()

        /**
         * 临时放入数组
         * pairs就是 （mask + 想要传递到fragment中的内容）。
         */
        fun putTempParams(clazz:Class<out BindingParamsFragment<*>>, vararg pairs:Pair<String, Any?>) {
            val keyPrefix = clazz.name + "@"
            pairs.forEach {pair ->
                val key = keyPrefix + pair.first
                val second = pair.second
                if(second != null) paramsMap[key] = second
            }
        }

        /**
         * 使用mask从中提取
         */
        inline fun <reified T> BindingParamsFragment<*>.getTempParams(mask:String) : T? {
            val key = this::class.java.name + "@" + mask
            val m = paramsMap
            if (m.containsKey(key)) {
                return m[key].asOrNull()
            }
            return null
        }

        private fun BindingParamsFragment<*>.destroyAllTempParams() {
            val iter = paramsMap.iterator()
            val keyPrefix = this::class.java.name + "@"
            while (iter.hasNext()) {
                val cur = iter.next()
                if (cur.key.startsWith(keyPrefix)) {
                    iter.remove()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        destroyAllTempParams()
    }
}