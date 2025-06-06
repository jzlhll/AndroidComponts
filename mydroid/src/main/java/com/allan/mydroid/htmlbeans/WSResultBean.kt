package com.allan.mydroid.htmlbeans

import androidx.annotation.Keep
import com.au.module_android.api.ResultBean

@Keep
class WSResultBean<T>(code: String,
                      msg: String?,
                      val api:String,
                      data:T?)
    : ResultBean<T>(code, msg, data)