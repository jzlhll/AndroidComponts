package com.au.module_okhttp.exceptions

import androidx.annotation.Keep

/**
 *响应错误
 */
@Keep
class AuResponseErrorException(var errorCode: Int, msg: String) : RuntimeException(msg)