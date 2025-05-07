package com.au.learning.classnamecompiler

import com.allan.classnameanno.EntryFrgName
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider
import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSDeclaration
import com.google.devtools.ksp.validate
import java.io.OutputStreamWriter

/**
 * @author allan
 * @date :2024/7/5 15:00
 * @description:
 */
class AllEntryFrgNamesProvider : SymbolProcessorProvider{
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        return TestKspSymbolProcessor(environment)
    }
}

/**
 * creator: lt  2022/10/20  lt.dygzs@qq.com
 * effect : ksp处理程序
 * warning:
 */
class TestKspSymbolProcessor(private val environment: SymbolProcessorEnvironment) : SymbolProcessor {
    // 使用一个集合来跟踪已经处理过的符号
    private val processedSymbols = mutableSetOf<KSDeclaration>()
    val allEntryFragmentNamesTemplate = AllEntryFragmentNamesTemplate()

    override fun process(resolver: Resolver): List<KSAnnotated> {
        environment.logger.warn("$this ksp process start....")

        val symbols = resolver.getSymbolsWithAnnotation(EntryFrgName::class.java.canonicalName)
        environment.logger.warn("ksp process symbol Size ${symbols.count()}")
        val ret = mutableListOf<KSAnnotated>()

        symbols.toList().forEach { symbol->
            environment.logger.warn("ksp process symbol $symbol")

            if (!symbol.validate())
                ret.add(symbol)
            else {
                if (symbol is KSClassDeclaration && symbol.classKind == ClassKind.CLASS) {
                    val qualifiedClassName = symbol.qualifiedName?.asString()
                    //解析priority
                    var priority = 0
                    var customName:String? = null
                    var autoEnter = false
                    symbol.annotations.forEach { an->
                        val pair = parseAnnotation(an, qualifiedClassName)
                        customName = pair.first
                        priority = pair.second
                        autoEnter = pair.third
                    }
                    allEntryFragmentNamesTemplate.insert(qualifiedClassName!!, priority, customName, autoEnter)
//                    symbol.accept(TestKspVisitor(environment), Unit)//处理符号
                } else {
                    ret.add(symbol)
                }
            }
        }

        //返回无法处理的符号
        return ret
    }

    private fun parseAnnotation(
        an: KSAnnotation,
        qualifiedClassName: String?,
    ): Triple<String?, Int, Boolean> {
        var priority = 0
        var customName:String? = null
        var autoEnter = false
        if (an.shortName.getShortName() == "EntryFrgName") {
            an.arguments.forEach { arg ->
                val argName = arg.name?.asString()
                when (argName) {
                    "priority" -> {
                        priority = arg.value.toString().toInt()
                        environment.logger.warn("ksp process $qualifiedClassName priority $priority")
                    }

                    "customName" -> {
                        customName = arg.value.toString()
                        environment.logger.warn("ksp process $qualifiedClassName customName $customName")
                    }

                    "autoEnter" -> {
                        autoEnter = arg.value.toString().toBoolean()
                        environment.logger.warn("ksp process $qualifiedClassName autoEnter $autoEnter")
                    }
                }
            }
        }
        return Triple(customName, priority, autoEnter)
    }

    override fun finish() {
        val code = allEntryFragmentNamesTemplate.end()

        // 生成文件
        val file = environment.codeGenerator.createNewFile(
            dependencies = Dependencies.ALL_FILES,
            packageName = "com.allan.androidlearning",
            fileName = "EntryList"
        )

        // 写入文件内容
        OutputStreamWriter(file).use { writer ->
            writer.write(code)
        }
    }

    override fun onError() {
        environment.logger.error("ksp process symbol error!")
    }
}