package com.au.plugins

import org.objectweb.asm.AnnotationVisitor
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.FieldVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes

class StringEncryptClassVisitor(cv: ClassVisitor) : ClassVisitor(Opcodes.ASM9, cv) {
    private var className: String? = null
    private val fieldsToEncrypt: MutableList<FieldInfo> = ArrayList()
    private var isCompanionObject = false

    private val mStaticFinalFields = java.util.ArrayList<ClassStringField>()
    private val mStaticFields = java.util.ArrayList<ClassStringField>()
    private val mFinalFields = java.util.ArrayList<ClassStringField>()
    private val mFields = java.util.ArrayList<ClassStringField>()

    override fun visit(
        version: Int, access: Int, name: String?,
        signature: String?, superName: String?, interfaces: Array<String>
    ) {
        super.visit(version, access, name, signature, superName, interfaces)
        className = name
        // 检查是否是伴生对象类
        isCompanionObject = name?.endsWith("\$Companion") == true
        pluginPrintln("${className}: | Companion: $isCompanionObject")
    }

    override fun visitAnnotation(descriptor: String?, visible: Boolean): AnnotationVisitor? {
        return super.visitAnnotation(descriptor, visible)
    }

    override fun visitMethod(access: Int, name: String?, descriptor: String?, signature: String?, exceptions: Array<out String?>?): MethodVisitor? {
        val mv: MethodVisitor? = super.visitMethod(access, name, descriptor, signature, exceptions)
        if (mv == null) {
            return mv
        }
        pluginPrintln("${className}: visit method $name")
        return null
    }

    override fun visitField(access: Int, name: String?, desc: String?, signature: String?, value: Any?): FieldVisitor? {
        var value = value
        if (ClassStringField.STRING_DESC == desc && name != null) {
            // static final, in this condition, the value is null or not null.
            if ((access and Opcodes.ACC_STATIC) != 0 && (access and Opcodes.ACC_FINAL) != 0) {
                pluginPrintln("${className}: visit field $name static final")
                mStaticFinalFields.add(ClassStringField(name, value as String?))
                value = null
            }
            // static, in this condition, the value is null.
            if ((access and Opcodes.ACC_STATIC) != 0 && (access and Opcodes.ACC_FINAL) == 0) {
                pluginPrintln("${className}: visit field $name static ")
                mStaticFields.add(ClassStringField(name, value as String?))
                value = null
            }

            // final, in this condition, the value is null or not null.
            if ((access and Opcodes.ACC_STATIC) == 0 && (access and Opcodes.ACC_FINAL) != 0) {
                pluginPrintln("${className}: visit field $name final ")
                mFinalFields.add(ClassStringField(name, value as String?))
                value = null
            }

            // normal, in this condition, the value is null.
            if ((access and Opcodes.ACC_STATIC) == 0 && (access and Opcodes.ACC_FINAL) == 0) {
                pluginPrintln("${className}: visit field $name normal")
                mFields.add(ClassStringField(name, value as String?))
                value = null
            }
        }
        return super.visitField(access, name, desc, signature, value)
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