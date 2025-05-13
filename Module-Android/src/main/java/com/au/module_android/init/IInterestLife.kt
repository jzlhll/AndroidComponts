package com.au.module_android.init

interface IInterestLife {
    /**
     * 当第一个感兴趣的，onStart触发。后续onStart不触发。
     */
    fun onLifeOpen()

    /**
     * 每次有任意一个感兴趣的activity的onStart触发，都会触发。在onLifeOpen之前执行。
     */
    fun onLifeOpenEach()

    /**
     * 当所有感兴趣的activity都onStop，才会触发。
     */
    fun onLifeClose()
}