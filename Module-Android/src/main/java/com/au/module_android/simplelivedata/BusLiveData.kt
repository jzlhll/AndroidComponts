package com.au.module_android.simplelivedata

class BusLiveData : NoStickLiveData<Bus>(Bus()) {
    fun publishSuccess(key:String, content:Any) {
        val value = super.getValue()
        value!!.push(key, content)
        super.setValueSafe(value)
    }

    fun publishError(key:String, errorMsg:String) {
        val value = super.getValue()
        value!!.pushError(key, errorMsg)
        super.setValueSafe(value)
    }

    fun fetch(key:String) : Bus.Content? {
        val value = super.getValue()
        return value!!.fetch(key)
    }

    fun look(key:String) : Bus.Content? {
        val value = super.getValue()
        return value!!.look(key)
    }

    fun foreach(action:(key:String, content: Bus.Content)->Boolean) {
        val value = super.getValue()
        return value!!.foreach(action)
    }

    override fun setValueSafe(data: Bus?) {
        throw IllegalAccessException("Please use publishSuccess or publishError function.")
    }

    override fun setValue(value: Bus?) {
        throw IllegalAccessException("Please use publishSuccess or publishError function.")
    }

    override fun getValue(): Bus? {
        throw IllegalAccessException("Please use fetch function.")
    }

}