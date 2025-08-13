package com.au.audiorecordplayer.bt.ble

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.au.module_android.utils.SingleCoroutineTaskExecutor

object BleConnectorsManager : DefaultLifecycleObserver{
    private val taskExecutor = SingleCoroutineTaskExecutor("BleConnectorsManager")

    //key是设备的mac地址
    var bleConnectorMap = mutableMapOf<String, BleConnector>()

    fun addAndConnect(bleConnector: BleConnector) {
        taskExecutor.submit {
            synchronized(bleConnectorMap) {
                bleConnectorMap[bleConnector.address] = bleConnector
            }
            bleConnector.connect()
        }
    }

    fun getBleConnector(address: String): BleConnector? {
        return bleConnectorMap[address]
    }

    fun removeAndDisconnect(bleConnector: BleConnector) {
        taskExecutor.submit {
            synchronized(bleConnectorMap) {
                bleConnectorMap.remove(bleConnector.address)
            }
            bleConnector.disconnectAndRelease()
        }
    }

    fun removeAndDisconnect(address: String) {
        taskExecutor.submit {
            val bleConnector: BleConnector?
            synchronized(bleConnectorMap) {
                bleConnector = bleConnectorMap.remove(address)
            }
            bleConnector?.disconnectAndRelease()
        }
    }

    override fun onDestroy(owner: LifecycleOwner) {
        super.onDestroy(owner)
        synchronized(bleConnectorMap) {
            bleConnectorMap.forEach {
                it.value.disconnectAndRelease()
            }
            bleConnectorMap.clear()
        }
        taskExecutor.shutdown()
    }
}