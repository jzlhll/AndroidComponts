package com.au.learning.classnamecompiler

import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardOpenOption
import javax.tools.Diagnostic

private const val TAG = "myAptCompiler: "
private val lock = Any()

fun logw(s:String) {
    //不能调用Diagnostic.Kind.ERROR
    Globals.mMessager?.printMessage(Diagnostic.Kind.WARNING, TAG + s)
    environment.logger.warn("log")
    logfile(s)
}

fun logfile(s:String) {
    synchronized (lock) {
        val file = File("d:\\aptlog.txt")
        if (file.exists()) {
            Files.writeString(Paths.get("d:\\aptlog.txt"), s, StandardOpenOption.APPEND)
        } else {
            Files.writeString(Paths.get("d:\\aptlog.txt"), s)
        }
    }
}