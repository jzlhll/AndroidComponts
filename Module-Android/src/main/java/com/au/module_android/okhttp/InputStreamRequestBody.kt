package com.au.module_android.okhttp

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import okhttp3.MediaType
import okhttp3.RequestBody
import okio.BufferedSink
import okio.source
import java.io.IOException

class InputStreamRequestBody(contentType: MediaType?, contentResolver: ContentResolver, uri: Uri?) : RequestBody() {
    companion object {
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
    }

    private val contentType: MediaType?
    private val contentResolver: ContentResolver
    private val uri: Uri

    init {
        if (uri == null) throw NullPointerException("uri == null")
        this.contentType = contentType
        this.contentResolver = contentResolver
        this.uri = uri
    }

    override fun contentType(): MediaType? {
        return contentType
    }

    @Throws(IOException::class)
    override fun contentLength(): Long {
        return -1
    }

    @Throws(IOException::class)
    override fun writeTo(sink: BufferedSink) {
        sink.writeAll(contentResolver.openInputStream(uri)!!.source())
    }
}