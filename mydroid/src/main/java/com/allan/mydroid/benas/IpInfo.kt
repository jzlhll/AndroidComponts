package com.allan.mydroid.benas

data class IpInfo(var ip:String,
                  var httpPort:Int?,
                  var webSocketPort:Int?,
                  var wifiName:String = "") {
    override fun toString(): String {
        return "IpInfo: $ip, httpPort $httpPort, wsPort $webSocketPort"
    }
}