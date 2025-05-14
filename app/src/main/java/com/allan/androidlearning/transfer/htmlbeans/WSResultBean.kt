package com.allan.androidlearning.transfer.htmlbeans

import com.au.module_android.api.ResultBean

class WSResultBean<T>(code: String,
                      msg: String?,
                      api:String,
                      data:T?)
    : ResultBean<T>(code, msg, data)