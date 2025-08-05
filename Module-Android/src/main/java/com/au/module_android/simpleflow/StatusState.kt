package com.au.module_android.simpleflow

sealed class StatusState<out T> {
    object Loading : StatusState<Nothing>()

    //不使用data class，避免同内容equals
    class Success<T>(val data :T) : StatusState<T>()
    class Error(val message: String?) : StatusState<Nothing>()
}