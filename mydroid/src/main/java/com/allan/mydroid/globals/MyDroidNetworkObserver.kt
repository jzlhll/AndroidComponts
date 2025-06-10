package com.allan.mydroid.globals

import com.allan.mydroid.beans.IpInfo
import com.au.module_android.init.IInterestLife
import com.au.module_android.utils.logt
import com.au.module_androidui.toast.ToastBuilder
import kotlinx.coroutines.launch

class MyDroidNetworkObserver :SimpleNetworkObserver(), IInterestLife {
    init {
        onChanged = { ip->
            if (ip.isNullOrEmpty()) {
                MyDroidConst.ipPortData.setValueSafe(null)
                // 网络丢失
                MyDroidGlobalService.stopServer()
            } else {
                getIpAddressAndStartServer(ip)
            }
        }
    }

    override fun onLifeOpen() {
        netRegister()
    }

    override fun onLifeOpenEach() {
    }

    override fun onLifeClose() {
        netUnregister()
    }

    fun getIpAddressAndStartServer(ip:String?) {
        val ipPortData = MyDroidConst.ipPortData
        if (ip.isNullOrEmpty()) {
            ipPortData.setValueSafe(null)
        } else {
            val v = ipPortData.realValue ?: IpInfo("", null, null)
            v.ip = ip
            logt { "get IpAddress set ip portData $v" }
            ipPortData.setValueSafe(v)

            if (!MyDroidConst.serverIsOpen) {
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