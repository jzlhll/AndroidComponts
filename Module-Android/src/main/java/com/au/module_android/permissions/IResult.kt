package com.au.module_android.permissions

import androidx.core.app.ActivityOptionsCompat

/**
 * @author allan.jiang
 * @date :2023/12/13 10:52
 * @description:
 */
interface IResult {
    /**
     * context指代的是Activity，Fragment，或者View
     */
    fun initAtOnCreate(context: Any) {}

    fun start(option: ActivityOptionsCompat?)
}