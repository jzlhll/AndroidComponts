package com.au.module_android.okhttp
/**
 *响应错误
 */
class OKHttpResponseException(var errorCode: Int, msg: String) : RuntimeException(msg)