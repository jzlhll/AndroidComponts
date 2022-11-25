package com.allan.androidlearning.rcv

interface ISubmit<DATA> {
    fun submitList(
        newList: List<DATA>?,
        isReplaceDatas: Boolean,
    )

    suspend fun submitListAsync(
        newList: List<DATA>?,
        isReplaceDatas: Boolean,
    )
}