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

        private fun String.nameFromBase64() = String(Base64.getDecoder().decode(this))
    }
}