package com.au.jobstudy.checkwith.pic

import java.io.File

/**
 * @author allan
 * @date :2024/7/16 9:33
 * @description:
 */
class Bean {
    var isAdd: Boolean = false
    var file: File? = null

    companion object {
        val ADD_BEAN = Bean().also { it.isAdd = true }
    }
}