package com.au.jobstudy.check

import java.util.Base64


/**
 * @author allan
 * @date :2024/7/15 11:30
 * @description:
 */
class NameList {
    companion object {
        @JvmField
        val NAMES_JIANG_TJ = "6JKL5re76Z2W".nameFromBase64()

        @JvmField
        val HUAZHONG_SCROLL = "5Y2O5Lit5biI6IyD5aSn5a2m5a6d5a6J6ZmE5bGe5a2m5qCh".nameFromBase64()

        @JvmField
        val BEST_STUDENT_1 = "".nameFromBase64()
        @JvmField
        val BEST_STUDENT_2 = "".nameFromBase64()
        @JvmField
        val BEST_STUDENT_3 = "".nameFromBase64()
        @JvmField
        val BEST_STUDENT_4 = "".nameFromBase64()
        @JvmField
        val GOOD_STUDENT_1 = "".nameFromBase64()
        @JvmField
        val GOOD_STUDENT_2 = "".nameFromBase64()
        @JvmField
        val GOOD_STUDENT_3 = "".nameFromBase64()
        @JvmField
        val GOOD_STUDENT_4 = "".nameFromBase64()
        @JvmField
        val GOOD_STUDENT_5 = "".nameFromBase64()
        @JvmField
        val NORMAL_STUDENT_1 = "".nameFromBase64()
        @JvmField
        val NORMAL_STUDENT_2 = "".nameFromBase64()
        @JvmField
        val NORMAL_STUDENT_3 = "".nameFromBase64()
        @JvmField
        val NORMAL_STUDENT_4 = "".nameFromBase64()
        @JvmField
        val NORMAL_STUDENT_5 = "".nameFromBase64()
        @JvmField
        val NORMAL_STUDENT_6 = "".nameFromBase64()
        @JvmField
        val NORMAL_STUDENT_7 = "".nameFromBase64()
        @JvmField
        val NORMAL_STUDENT_8 = "".nameFromBase64()

        private fun String.nameFromBase64() = String(Base64.getDecoder().decode(this))
    }
}