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
        val BEST_STUDENT_1 = "6ZmI5aWV5ra1".nameFromBase64()
        @JvmField
        val BEST_STUDENT_2 = "5rGf5bCP54S2".nameFromBase64()
        @JvmField
        val BEST_STUDENT_3 = "546L5LiA6K+6".nameFromBase64()
        @JvmField
        val BEST_STUDENT_4 = "5ZC05Lic5Z+O".nameFromBase64()
        @JvmField
        val GOOD_STUDENT_1 = "5Yip6Iq35aaC".nameFromBase64()
        @JvmField
        val GOOD_STUDENT_2 = "5ZSQ5aWV5pmo".nameFromBase64()
        @JvmField
        val GOOD_STUDENT_3 = "57+f5pif6L6w".nameFromBase64()
        @JvmField
        val GOOD_STUDENT_4 = "572X5piV56CU".nameFromBase64()
        @JvmField
        val GOOD_STUDENT_5 = "5YiY5biI6JCM".nameFromBase64()
        @JvmField
        val GOOD_STUDENT_6 = "5YiY54Wc5p2w".nameFromBase64()
        @JvmField
        val GOOD_STUDENT_7 = "5p2o5pmo".nameFromBase64()
        @JvmField
        val GOOD_STUDENT_8 = "5a6L5bKp5rO9".nameFromBase64()
        @JvmField
        val NORMAL_STUDENT_1 = "6YOt5rO95bu2".nameFromBase64()
        @JvmField
        val NORMAL_STUDENT_2 = "5byg6Zuo5p6c".nameFromBase64()
        @JvmField
        val NORMAL_STUDENT_3 = "5p6X5pet5Lic".nameFromBase64()
        @JvmField
        val NORMAL_STUDENT_4 = "5L2V54Wc6L6w".nameFromBase64()
        @JvmField
        val NORMAL_STUDENT_5 = "5byg6ZuF5reH".nameFromBase64()

        private fun String.nameFromBase64() = String(Base64.getDecoder().decode(this))
    }
}