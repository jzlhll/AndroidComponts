package com.au.module_android.simpleflow

sealed class StatusState<out T> {
    internal var index = 0

    object Loading : StatusState<Nothing>()

    //不使用data class，避免同内容equals
    class Success<T>(val data :T) : StatusState<T>()
    class Error(val message: String?) : StatusState<Nothing>()
}