package com.au.module_android.okhttp.beans

import androidx.annotation.Keep

/**
 * data是json对象
*/
@Keep
class ApiBean<T>(code: String, msg: String?, val data:T? = null) : BaseBean(code, msg)