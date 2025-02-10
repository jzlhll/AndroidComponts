package com.au.module_nested.bottom_nav

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes

/**
 * @author au
 * @date :2023/11/6 14:48
 * @description:
 */
data class BottomPageBean(
                          var isSelected:Boolean,
                          @StringRes val titleRes: Int,
                          @DrawableRes val iconRes: Int,
                          @DrawableRes val selectIconRes: Int,
                          val titleColor:Int,
                          val selectTitleColor:Int)