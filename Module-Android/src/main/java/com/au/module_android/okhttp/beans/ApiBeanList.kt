package com.au.module_android.okhttp.beans

import androidx.annotation.Keep

/**
 * data是json数组
 */
@Keep
class ApiBeanList<T>(code: String, msg: String?, val data: List<T>? = null) : BaseBean(code, msg)