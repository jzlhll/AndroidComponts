package com.au.audiorecordplayer.bt.ble
// 定义回调监听器接口
interface BluetoothCallbackListener : IBluetoothCallbackListener {
    /**
     * 监听回调者，它关注哪些address
     */
    fun observerAddressList() : List<String>
}