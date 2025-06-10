package com.allan.mydroid.beans

data class IpInfo(var ip:String,
                  var httpPort:Int?,
                  var webSocketPort:Int?,
                  var wifiName:String = "") {
    override fun toString(): String {
        return "IpInfo: $ip, httpPort $httpPort, wsPort $webSocketPort"
    }
}