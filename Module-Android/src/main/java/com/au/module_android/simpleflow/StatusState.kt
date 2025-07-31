package com.au.module_android.simpleflow

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
