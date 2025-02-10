package com.au.module_okhttp.exceptions

import androidx.annotation.Keep

@Keep
class AuTokenExpiredException(msg:String) : Exception(msg)