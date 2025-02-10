package com.au.module_okhttp.beans.base

import androidx.annotation.Keep
import com.au.module_okhttp.beans.CODE_OK

@Keep
abstract class BaseBean(val code:String, val msg:String?) {
    val isSuccess: Boolean
        get() = CODE_OK == code
}