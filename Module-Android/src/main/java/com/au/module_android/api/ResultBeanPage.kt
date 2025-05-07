package com.au.module_android.api

import androidx.annotation.Keep

/**
 * data是分页
 */
@Keep
class ResultBeanPage<T>(code: String, msg: String?, val data: Page<T>? = null) : BaseBean(code, msg)