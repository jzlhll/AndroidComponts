package com.au.learning.classnamecompiler

import javax.annotation.processing.Filer
import javax.annotation.processing.Messager
import javax.lang.model.util.Elements

/**
 * @author allan
 * @date :2024/7/3 14:48
 * @description:
 */
object Globals {
    /**
     * JavaPoet示例
     * 可以使用filter来将代码写入到java文件中。如果遇到错误则使用messager发送出去
     * try {
     *     JavaFile.builder(packageName, typeBuilder.build()).build().writeTo(mFiler)
     * } catch (IOException e) {
     *     mMessager.printMessage(Diagnostic.Kind.ERROR, e.getMessage(), typeElement);
     * }
     */
    var mFiler: Filer? = null //创建源代码的filer。
    var mMessager: Messager? = null //创建消息发送器Messager，向外接发送错误消息
    var mElementUtils: Elements? = null //解析注解元素所需的通用方法
}