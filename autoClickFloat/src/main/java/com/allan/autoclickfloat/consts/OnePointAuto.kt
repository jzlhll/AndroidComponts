package com.allan.autoclickfloat.consts

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.au.module_android.Globals
import com.au.module_android.datastore.AppDataStore
import kotlinx.coroutines.launch

class OnePointAuto {
    /**
     * 是否打开功能的开关
     */
    val autoOnePointOpenLiveData = MutableLiveData<Boolean>(false)

    private val _autoOnePointClickMsLiveData = MutableLiveData<Int>()
    /**
     * 自动点击的监听
     */
    val autoOnePointClickMsLiveData:LiveData<Int> = _autoOnePointClickMsLiveData

    private val _autoOnePointLiveData = MutableLiveData<Pair<Int, Int>>()
    /**
     * 自动点击的真实坐标点
     */
    val autoOnePointLiveData:LiveData<Pair<Int, Int>> = _autoOnePointLiveData

    private val _autoOnePointLocLiveData = MutableLiveData<Pair<Int, Int>>()
    /**
     * 自动点击的真实坐标点
     */
    val autoOnePointLocLiveData:LiveData<Pair<Int, Int>> = _autoOnePointLocLiveData

    fun loadAutoOnePointMs() {
        Globals.mainScope.launch {
            _autoOnePointClickMsLiveData.postValue(AppDataStore.read("autoOnePointClickMs", 250))
        }
    }

    fun saveAutoOnePointMs(ms:Int) {
        Globals.mainScope.launch {
            AppDataStore.save("autoOnePointClickMs", ms)
            _autoOnePointClickMsLiveData.postValue(ms)
        }
    }

    fun loadAutoOnePoint() {
        Globals.mainScope.launch {
            val x:Int = AppDataStore.read("auto_continuous_click_point_x", 0)
            val y:Int = AppDataStore.read("auto_continuous_click_point_y", 0)
            val locx:Int = AppDataStore.read("auto_continuous_click_point_loc_x", 0)
            val locy:Int = AppDataStore.read("auto_continuous_click_point_loc_y", 0)
            _autoOnePointLiveData.postValue(x to y)
            _autoOnePointLocLiveData.postValue(locx to locy)
        }
    }

    fun saveAutoOnePoint(x:Int, y:Int, locationX:Int, locationY:Int) {
        Globals.mainScope.launch {
            AppDataStore.save(
                "auto_continuous_click_point_x" to x,
                "auto_continuous_click_point_y" to y)

            AppDataStore.save(
                "auto_continuous_click_point_loc_x" to locationX,
                "auto_continuous_click_point_loc_y" to locationY)

            _autoOnePointLiveData.postValue(x to y)
            _autoOnePointLocLiveData.postValue(locationX to locationY)
        }
    }
}