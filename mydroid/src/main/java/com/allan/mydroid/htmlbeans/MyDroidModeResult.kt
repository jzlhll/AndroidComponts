package com.allan.mydroid.htmlbeans

import androidx.annotation.Keep

data class MyDroidModeResult(@Keep val myDroidMode:String,
                             @Keep val clientName:String,
                             @Keep val debugReceiver: Boolean, @Keep val debugSend: Boolean)