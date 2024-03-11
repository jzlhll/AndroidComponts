package com.allan.nested.recyclerview

/**
 * @author au
 * Date: 2023/2/21
 * Description 转成一个结果
 */
interface ICreator<T, R> {
    fun create(t:T) : R
}