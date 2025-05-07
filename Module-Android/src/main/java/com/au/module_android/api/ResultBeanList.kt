package com.au.module_android.api

import androidx.annotation.Keep

/**
 * data是json数组
 */
@Keep
class ResultBeanList<T>(code: String, msg: String?, val data: List<T>? = null) : BaseBean(code, msg)