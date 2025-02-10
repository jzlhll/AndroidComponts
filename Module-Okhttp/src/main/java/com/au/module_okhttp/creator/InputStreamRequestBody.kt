package com.au.module_okhttp.creator

import android.content.Context
import android.net.Uri
import okhttp3.MediaType
import okhttp3.RequestBody
import okio.BufferedSink
import okio.source

fun Uri.asInputStreamRequestBody(context:Context, length:Long = -1, contentType: MediaType? = null) : RequestBody {
    return object : RequestBody() {
        override fun contentType(): MediaType? {
            return contentType
        }

        override fun contentLength(): Long {
            return length
        }

        override fun writeTo(sink: BufferedSink) {
            context.contentResolver.openInputStream(this@asInputStreamRequestBody)?.run {
                source().use {
                    sink.writeAll(it)
                }
            }
        }
    }
}