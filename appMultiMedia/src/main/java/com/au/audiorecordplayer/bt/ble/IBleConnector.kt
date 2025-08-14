package com.au.audiorecordplayer.bt.ble

interface IBleConnector {
    /**
     * 返回值表示是否发送成功
     */
    suspend fun sendMessage(data: String, writeType: Int? = null) : Boolean
    fun address() : String
    fun connect()
    fun isConnected() : Boolean
    fun isPrepared() : Boolean
}