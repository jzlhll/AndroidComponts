package com.au.plugins;

import org.objectweb.asm.MethodVisitor;

class Base64InstructionWriter extends StringEncryptClassVisitor2.InstructionWriter {

        private Base64InstructionWriter(String fogClassName) {
            super(fogClassName);
        }

        @Override
        String write(byte[] key, byte[] value, MethodVisitor mv) {
            String base64Key = new String(Base64.encode(key, Base64.DEFAULT));
            String base64Value = new String(Base64.encode(value, Base64.DEFAULT));
            mv.visitLdcInsn(base64Value);
            mv.visitLdcInsn(base64Key);
            super.writeClass(mv, "(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;");
            return base64Value;
        }
    }