// encryptor-plugin/src/main/java/com/example/encryptor/StringEncryptorFactory.java
package com.au.plugins

import com.android.build.api.instrumentation.AsmClassVisitorFactory
import com.android.build.api.instrumentation.ClassContext
import com.android.build.api.instrumentation.ClassData
import com.android.build.api.instrumentation.InstrumentationParameters
import org.gradle.api.NonNullApi
import org.objectweb.asm.ClassVisitor

@NonNullApi
abstract class StringEncryptFactory : AsmClassVisitorFactory<InstrumentationParameters.None> {
    override fun createClassVisitor(
        classContext: ClassContext,
        nextClassVisitor: ClassVisitor
    ): ClassVisitor {
        //isInstrumentable 判断true才会执行这里
        val log = classContext.currentClassData.className
        pluginPrintln("encrypt string class: $log")
        return StringEncryptClassVisitor(nextClassVisitor)
    }

    // 判断类是否需要处理
    override fun isInstrumentable(classData: ClassData): Boolean {
        classData.classAnnotations.forEach {
            if (it == ANNOTATION_NAME) {
                return true
            }
        }
        return false
    }
}