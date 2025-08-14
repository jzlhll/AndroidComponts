package com.au.audiorecordplayer.bt.ble
// 定义回调监听器接口
interface IBluetoothCallbackListener {
    // **连接状态回调
    fun onConnectionStateChanged(address: String, state: Int)
    fun onPrepared(address: String)

    /**
     * 接收到了数据回调
     * @data 原始数据
     * @str 这是去除了前4个字节变成的str
     */
    fun onDataCharChanged(address: String, data: ByteArray, str:String)
    // 数据read回调 (特征值变化)
    fun onDataCharRead(address: String, data: ByteArray)
    // 添加MTU变更回调
    fun onMtuChanged(address: String, newMtu: Int, status: Int)
}