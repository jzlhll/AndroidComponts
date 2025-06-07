package com.allan.mydroid.globals

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import com.au.module_android.Globals
import com.au.module_android.utils.logd
import java.net.Inet4Address
import java.net.NetworkInterface

open class SimpleNetworkObserver {
    var onChanged:((ip:String?)->Unit)? = null

    private val netObserver = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            logd { "network on Available" }
            onChanged?.invoke(getIpAddress())
        }
        override fun onLost(network: Network) {
            logd { "network on Lost" }
            onChanged?.invoke(getIpAddress()) //华为手机，先链接上wifi，再移动网络掉线，也会回调。
        }
    }

    private fun getIpAddress() : String? {
        try {
            val interfaces = NetworkInterface.getNetworkInterfaces()
            while (interfaces.hasMoreElements()) {
                val netInterface = interfaces.nextElement()
                if (netInterface.displayName.equals("wlan0") || netInterface.name.startsWith("ap")) {
                    for (addr in netInterface.getInterfaceAddresses()) {
                        val inetAddr = addr.address
                        if (inetAddr is Inet4Address) {
                            val ip = inetAddr.hostAddress
                            return ip
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    fun netRegister() {
        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()

        val manager = Globals.app.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        manager.registerNetworkCallback(request, netObserver)
    }

    fun netUnregister() {
        val manager = Globals.app.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        manager.unregisterNetworkCallback(netObserver)
    }
}