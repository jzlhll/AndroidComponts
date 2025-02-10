package com.au.module_okhttp.exceptions

import androidx.annotation.Keep

@Keep
class AuApiException(val code:String, msg:String, val data:String?) : Exception(msg)