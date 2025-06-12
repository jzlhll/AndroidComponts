package com.allan.mydroid.beans.wsdata

import androidx.annotation.Keep

data class MyDroidModeData(@Keep val myDroidMode:String,
                           @Keep val clientName:String,
                           @Keep val color:String,
                           @Keep val debugReceiver: Boolean,
                           @Keep val debugSend: Boolean)