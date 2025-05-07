package com.au.module_android.api

import androidx.annotation.Keep

/**
 * 分页数据的每一页数据结构
 * @param currentPage 当前页码
 * @param pages 总页数
 * @param size 每页大小
 * @param total 总数 = pages * size
 * @param records 当前页下的数据list
 */
@Keep
data class Page<T>(
    val currentPage: Int = 0,
    val pages: Int = 0,
    val size: Int = 0,
    val total: Int = 0,
    val records: List<T>? = null
)