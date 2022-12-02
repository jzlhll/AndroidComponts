package com.allan.androidlearning.livedata

import androidx.lifecycle.MutableLiveData

object StatusLiveData {
    var systemChanged = MutableLiveData<Unit>()
}