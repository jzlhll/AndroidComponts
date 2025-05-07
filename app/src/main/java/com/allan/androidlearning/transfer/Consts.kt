package com.allan.androidlearning.transfer

import com.au.module_android.api.ResultBean
import com.au.module_android.json.toJsonString
import fi.iki.elonen.NanoHTTPD.Response
import fi.iki.elonen.NanoHTTPD.Response.Status
import fi.iki.elonen.NanoHTTPD.newFixedLengthResponse

const val TEMP_CACHE_DIR = "nanoTmp"
const val TEMP_CACHE_CHUNKS_DIR = "nanoChunksTmp"
const val TEMP_CACHE_MERGED_DIR = "nanoMerged"
const val MIME_TYPE_JSON = "application/json; charset=UTF-8"

const val CODE_SUC = "0"
const val CODE_FAIL = "-1"
const val CODE_FAIL_RECEIVER_CHUNK = "-101"
const val CODE_FAIL_MERGE_CHUNK = "-102"
const val CODE_FAIL_MD5_CHECK = "-103"

fun ResultBean<*>.okJsonResponse() : Response{
    return newFixedLengthResponse(
        Status.OK,
        MIME_TYPE_JSON,
        this.toJsonString()
    )
}

fun ResultBean<*>.badRequestJsonResponse() : Response{
    return newFixedLengthResponse(
        Status.BAD_REQUEST,
        MIME_TYPE_JSON,
        this.toJsonString()
    )
}

fun ResultBean<*>.jsonResponse(status: Response.IStatus) : Response{
    return newFixedLengthResponse(
        status,
        MIME_TYPE_JSON,
        this.toJsonString()
    )
}