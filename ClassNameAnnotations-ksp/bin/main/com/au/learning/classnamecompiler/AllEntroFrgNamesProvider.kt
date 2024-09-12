package com.au.learning.classnamecompiler

import com.allan.classnameanno.EntroFrgName
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider
import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSDeclaration
import com.google.devtools.ksp.validate
import java.io.OutputStreamWriter

/**
 * @author allan
 * @date :2024/7/5 15:00
 * @description:
 */
class AllEntroFrgNamesProvider : SymbolProcessorProvider{
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        return TestKspSymbolProcessor(environment)
    }
}

/**
 * effect : ksp处理程序
 * warning:
 */
class TestKspSymbolProcessor(private val environment: SymbolProcessorEnvironment) : SymbolProcessor {
    // 使用一个集合来跟踪已经处理过的符号
    private val processedSymbols = mutableSetOf<KSDeclaration>()
    val allEntroFragmentNamesTemplate = AllEntroFragmentNamesTemplate()

    override fun process(resolver: Resolver): List<KSAnnotated> {
        environment.logger.warn("$this ksp process start....")

        val symbols = resolver.getSymbolsWithAnnotation(EntroFrgName::class.java.canonicalName)
        environment.logger.warn("ksp process symbol Size ${symbols.count()}")
        val ret = mutableListOf<KSAnnotated>()

        symbols.toList().forEach { symbol->
            environment.logger.warn("ksp process symbol $symbol")

            if (!symbol.validate())
                ret.add(symbol)
            else {
                if (symbol is KSClassDeclaration && symbol.classKind == ClassKind.CLASS) {
                    val qualifiedClassName = symbol.qualifiedName?.asString()
                    // 访问注解的参数
                    allEntroFragmentNamesTemplate.insert(qualifiedClassName!!)
//                    symbol.accept(TestKspVisitor(environment), Unit)//处理符号
                } else {
                    ret.add(symbol)
                }
            }
        }

        //返回无法处理的符号
        return ret
    }

    override fun finish() {
        val code = allEntroFragmentNamesTemplate.end()

        // 生成文件
        val file = environment.codeGenerator.createNewFile(
            dependencies = Dependencies.ALL_FILES,
            packageName = "com.allan.androidlearning",
            fileName = "EntroList"
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