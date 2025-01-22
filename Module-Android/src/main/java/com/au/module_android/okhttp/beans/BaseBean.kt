package com.au.module_android.okhttp.beans

import androidx.annotation.Keep

@Keep
abstract class BaseBean(val code:String, val msg:String?) {
    val isSuccess: Boolean
        get() = CODE_OK == code
}