package com.au.plugins

import java.util.Base64

//todo: 在你的代码中，加密代码类的类名路径。添加防止混淆。建议直接人工乱七八糟的package
const val StringCryptoClass = "com.au.stringprotect.StringCrypto"
//todo: 在你的代码中，加密代码类的类名路径。添加防止混淆。建议直接人工乱七八糟的函数名字
const val StringCryptoDecryptMethod = "decrypt"
//todo： 注解类名。应该添加防止混淆。
const val ANNOTATION_NAME = "com.au.annos.EncryptString"

// todo 混淆函数。注意与你代码中的StringCryptoClass的代码解密函数保持一致
fun encrypt(origStr: String): String? {
    return Base64.getEncoder().encodeToString(origStr.toByteArray())
}