package com.allan.androidlearning.transfer

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import com.allan.androidlearning.transfer.benas.IpInfo
import com.au.module_android.Globals
import com.au.module_android.init.IInterestLife
import com.au.module_android.utils.logdNoFile
import com.au.module_android.utils.logt
import com.au.module_androidui.toast.ToastBuilder
import kotlinx.coroutines.launch
import java.net.Inet4Address
import java.net.NetworkInterface

class MyDroidNetworkObserver : IInterestLife {
    private val netObserver = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            logdNoFile { "network on Available" }
            // 网络可用
            getIpAddressAndStartServer()
        }
        override fun onLost(network: Network) {
            logdNoFile { "network on Lost" }
            MyDroidConst.ipPortData.setValueSafe(null)
            // 网络丢失
            MyDroidGlobalService.stopServer()
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

    override fun onLifeOpen() {
        netRegister()
    }

    override fun onLifeOpenEach() {
    }

    override fun onLifeClose() {
        netUnregister()
    }

    fun getIpAddress() : String? {
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

    fun getIpAddressAndStartServer() {
        val ip = getIpAddress()
        val ipPortData = MyDroidConst.ipPortData
        if (ip.isNullOrEmpty()) {
            ipPortData.setValueSafe(null)
        } else {
            val v = ipPortData.realValue ?: IpInfo("", null, null)
            v.ip = ip
            logt { "get IpAddress set ip portData $v" }
            ipPortData.setValueSafe(v)

            if (!MyDroidConst.serverIsOpen.realValueUnsafe) {
                MyDroidGlobalService.startServer{ msg->
                     MyDroidGlobalService.scope.launch {
                        ToastBuilder()
                            .setOnTop()
                            .setIcon("error")
                            .setMessage(msg)
                            .toast()
                    }
                }
            }
        }
    }
}