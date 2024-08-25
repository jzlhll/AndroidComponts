package com.au.module_android.selectlist

/**
 * @author allan
 * @date :2024/7/29 11:00
 * @description: 每一份的数据。比如实现equal。
 */
abstract class SelectListItem {
    abstract val itemName:String

    override fun equals(other: Any?): Boolean {
        if (other is SelectListItem) {
            return this.itemName == other.itemName
        }
        return false
    }

    override fun hashCode(): Int {
        return itemName.hashCode()
    }
}