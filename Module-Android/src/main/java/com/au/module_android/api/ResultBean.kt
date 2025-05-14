package com.au.module_android.api

import androidx.annotation.Keep

/**
 * data是json对象
*/
@Keep
open class ResultBean<T>(code: String, msg: String?, val data:T? = null) : BaseBean(code, msg)