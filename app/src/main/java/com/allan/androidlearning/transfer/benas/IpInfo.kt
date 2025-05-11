package com.allan.androidlearning.transfer.benas

data class IpInfo(var ip:String, var httpPort:Int?, var webSocketPort:Int?) {
    override fun toString(): String {
        return "IpInfo: $ip, httpPort $httpPort, wsPort $webSocketPort"
    }
}