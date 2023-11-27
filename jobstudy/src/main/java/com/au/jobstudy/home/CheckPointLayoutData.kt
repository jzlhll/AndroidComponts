package com.au.jobstudy.home

import com.au.jobstudy.bean.DataItem

/**
 * @author allan.jiang
 * @date :2023/11/27 16:11
 * @description:
 */
sealed class CheckPointUiData {
    class Title(val str:String) : CheckPointUiData()
    class Item(val dataItem: DataItem) : CheckPointUiData()
}