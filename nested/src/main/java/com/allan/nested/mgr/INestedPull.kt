package com.allan.nested.mgr

/**
 * @author allan.jiang
 * Date: 2023/2/27
 */
interface INestedPull {
    fun loadingData():Boolean
    fun pullDownIsTargetTranslated():Boolean
}