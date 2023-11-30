package com.au.module_android.simplelivedata

/**
 * @author allan.jiang
 * @date :2023/11/30 14:03
 * @description: 总线思想的设计
 */
class Bus {
    data class Content(val real:Any?, val error:String?) {
        val isGood
            get() = real != null
        val isError
            get() = !error.isNullOrBlank()
    }

    private val data = HashMap<String, Content>()

    @Synchronized
    fun push(key:String, value:Any) {
        data[key] = Content(value, null)
    }

    @Synchronized
    fun pushError(key:String, error:String) {
        data[key] = Content(null, error)
    }

    @Synchronized
    fun fetch(key:String) : Content? {
        return data.remove(key)
    }

    @Synchronized
    fun look(key:String) : Content? {
        if (data.containsKey(key)) {
            return data[key]
        }
        return null
    }

    @Synchronized
    /**
     * action 返回值表示，如果想要从bus中移除。消费掉。则返回true。
     */
    fun foreach(action:(key:String, content:Content)->Boolean) {
        val list = mutableListOf<String>()
        for (item in data) {
            if(action(item.key, item.value)) {
                list.add(item.key)
            }
        }

        for (k in list) {
            data.remove(k)
        }
    }
}