package com.au.plugins

import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.FieldVisitor
import org.objectweb.asm.Opcodes

class StringEncryptClassVisitor(cv: ClassVisitor) : ClassVisitor(Opcodes.ASM9, cv) {
    private var className: String? = null
    private val fieldsToEncrypt: MutableList<FieldInfo> = ArrayList()
    private var isCompanionObject = false

    override fun visit(
        version: Int, access: Int, name: String?,
        signature: String?, superName: String?, interfaces: Array<String>
    ) {
        super.visit(version, access, name, signature, superName, interfaces)
        className = name
        // 检查是否是伴生对象类
        isCompanionObject = name?.endsWith("\$Companion") == true
        pluginPrintln("Class: $name | Companion: $isCompanionObject")
    }

    override fun visitField(
        access: Int, name: String?, descriptor: String?,
        signature: String?, value: Any?
    ): FieldVisitor {
        val isTarget = isTargetField(access, descriptor)
        pluginPrintln("$className: visitField descriptor $descriptor access $access isTarget $isTarget")
        // 收集需要加密的字段
        if (isTarget) {
            fieldsToEncrypt.add(FieldInfo(name, descriptor, value))
        }
        return super.visitField(access, name, descriptor, signature, value)
    }

    private fun isTargetField(access: Int, descriptor: String?): Boolean {
        // 步骤1: 类型检查
        if (descriptor != "Ljava/lang/String;") {
            return false
        }

        // 步骤2: final修饰符检查
        val isFinal = (access and Opcodes.ACC_FINAL) != 0
        if (!isFinal) {
            return false
        }

        // 步骤3: 静态/非静态区分
        val isStatic = (access and Opcodes.ACC_STATIC) != 0

        return when {
            // 情况1: 普通类中的静态字段
            !isCompanionObject && isStatic -> true

            // 情况2: 伴生类中的实例字段
            isCompanionObject && !isStatic -> true

            // 其他情况不处理
            else -> false
        }
    }

    override fun visitEnd() {
        if (!fieldsToEncrypt.isEmpty()) {
            // 创建静态初始化方法（如果不存在）
            cv.visitMethod(
                Opcodes.ACC_STATIC, "<clinit>", "()V", null, null
            )?.apply {
                visitCode()

                for (field in fieldsToEncrypt) {
                    // 1：在编译时进行加密
                    val original = field.value.toString()
                    val encrypted = encrypt(original) // 插件内部加密

                    // 2：加载加密后的值
                    visitLdcInsn(encrypted) // 加载加密后的字符串

                    // 3：调用运行时解密方法
                    visitMethodInsn(
                        Opcodes.INVOKESTATIC,
                        StringCryptoClass,
                        StringCryptoDecryptMethod, // 调用解密方法
                        "(Ljava/lang/String;)Ljava/lang/String;",
                        false
                    )

                    // 将解密后的值存入字段
                    visitFieldInsn(
                        Opcodes.PUTSTATIC,
                        className,
                        field.name,
                        "Ljava/lang/String;"
                    )
                }

                visitInsn(Opcodes.RETURN)
                visitMaxs(1, 0)
                visitEnd()
            }
        }

        super.visitEnd()
    }

}