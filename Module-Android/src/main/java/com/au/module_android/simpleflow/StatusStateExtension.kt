package com.au.module_android.simpleflow

import kotlinx.coroutines.flow.SharedFlow

/**
 * 架构要求请求Api，会抛出异常。因此这里做request的忽略和包装。
 */
suspend fun <T> stateRequest(requestBlock: suspend () -> T) : StatusState<T>{
    try {
        val data = requestBlock()
        return StatusState.Success(data)
    } catch (e: Exception) {
        return StatusState.Error(e.message)
    }
}

suspend fun <T> SharedFlow<StatusState<T>>.collectState(loading: ()->Unit = {},
                                                        success: (data:T) -> Unit,
                                                        error: (exMsg:String?) -> Unit): Nothing {
    collect {
        when (it) {
            is StatusState.Loading -> {
                loading()
            }
            is StatusState.Success -> {
                success(it.data)
            }
            is StatusState.Error -> {
                error(it.message)
            }
        }
    }
}