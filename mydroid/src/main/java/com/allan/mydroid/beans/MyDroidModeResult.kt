package com.allan.mydroid.beans

import androidx.annotation.Keep

data class MyDroidModeResult(@Keep val myDroidMode:String,
                             @Keep val clientName:String,
                             @Keep val color:Int,
                             @Keep val debugReceiver: Boolean,
                             @Keep val debugSend: Boolean)