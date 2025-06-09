package com.allan.mydroid.globals

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner

open class LifeSimpleNetworkObserver(owner: LifecycleOwner) : SimpleNetworkObserver(), DefaultLifecycleObserver {
    init {
        owner.lifecycle.addObserver(this)
    }

    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)
        netRegister()
    }

    override fun onStop(owner: LifecycleOwner) {
        super.onStop(owner)
        netUnregister()
    }
}