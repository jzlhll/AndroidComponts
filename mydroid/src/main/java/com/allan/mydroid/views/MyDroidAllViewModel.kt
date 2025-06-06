package com.allan.mydroid.views

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.au.module_android.Globals
import com.au.module_android.simplelivedata.NoStickLiveData
import com.au.module_android.utils.logdNoFile
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.net.Inet4Address
import java.net.NetworkInterface

class MyDroidAllViewModel() : ViewModel() {
    val ipData = NoStickLiveData<String?>()

    private val netObserver = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            logdNoFile { "network on Available" }
            // 网络可用
            ipData.setValueSafe(getIpAddress())
        }
        override fun onLost(network: Network) {
            logdNoFile { "network on Lost" }
            ipData.setValueSafe(null)
        }

        override fun onUnavailable() {
            super.onUnavailable()
            logdNoFile { "network on Unavailable" }
        }

        override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
            super.onCapabilitiesChanged(network, networkCapabilities)
            logdNoFile { "network no cap changed" }
        }
    }

    private fun netRegister() {
        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()

        val manager = Globals.app.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        manager.registerNetworkCallback(request, netObserver)
    }

    private fun netUnregister() {
        val manager = Globals.app.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        manager.unregisterNetworkCallback(netObserver)
    }

    init {
        netRegister()
        viewModelScope.launch {
            delay(2000)
            ipData.setValueSafe(getIpAddress())
        }
    }

    override fun onCleared() {
        super.onCleared()
        netUnregister()
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
}