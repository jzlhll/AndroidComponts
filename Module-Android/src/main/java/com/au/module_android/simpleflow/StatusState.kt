package com.au.module_android.simpleflow

/**
 * 实现了Reducer方案。参考OneDeviceInviteUserListViewModel。
 * 第一步：让ViewModel实现，IActionDispatcher by ActionDispatcherImpl()
 * 第二步：定义UI调用ViewModel 的IStateAction的子类，比如RequestApiAction, DeleteItemAction等；
 * 第三步：init{}函数，注册reduce分函数并实现调用协程和返回StatusState；
 *    第四步：界面上collect并响应三类StatusState。
 */
sealed class StatusState<out T> {
    object Loading : StatusState<Nothing>()

    //不使用data class，避免同内容equals
    class Success<T>(val data :T) : StatusState<T>()
    class Error(val message: String?) : StatusState<Nothing>()
}