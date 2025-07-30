package com.au.module_android.simpleflow

import kotlinx.coroutines.flow.SharedFlow

sealed class StatusState<out T> {
    object Loading : StatusState<Nothing>()
    data class Success<out T>(val data: T) : StatusState<T>()
    data class Error(val message: String?) : StatusState<Nothing>()

    fun parse(loading: ()->Unit = {},
              success: (data:T) -> Unit,
              error: (exMsg:String?) -> Unit) {
        when (this) {
            is Loading -> {
                loading()
            }
            is Success -> {
                success(data)
            }
            is Error -> {
                error(message)
            }
        }
    }
}

/**
 * 架构要求请求Api，会抛出异常。因此这里做request的忽略和包装。
 */
suspend fun <T> flowStateApi(apiRequestBlock: suspend () -> T) : StatusState<T>{
    try {
        val data = apiRequestBlock()
        return StatusState.Success(data)
    } catch (e: Exception) {
        return StatusState.Error(e.message)
    }
}

suspend fun <T> SharedFlow<StatusState<T>>.collectStatusState(loading: ()->Unit = {},
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