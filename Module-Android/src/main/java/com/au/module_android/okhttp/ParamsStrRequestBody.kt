package com.au.module_android.okhttp

import androidx.annotation.Keep
import okhttp3.RequestBody

/**
 * 重构RequestBody便于后续取出paramsStr来使用。
 */
@Keep
abstract class ParamsStrRequestBody(val paramsStr:String) : RequestBody()