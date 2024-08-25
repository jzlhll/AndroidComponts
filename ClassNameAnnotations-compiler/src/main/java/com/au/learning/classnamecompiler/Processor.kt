package com.au.learning.classnamecompiler

import com.allan.classnameanno.EntroFrgName
import java.io.IOException
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.ProcessingEnvironment
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.SourceVersion
import javax.lang.model.element.Element
import javax.lang.model.element.TypeElement
import javax.tools.Diagnostic
import javax.tools.JavaFileObject

class Processor : AbstractProcessor() {
    private var processingEnv:ProcessingEnvironment? = null

    override fun init(processingEnv: ProcessingEnvironment?) {
        super.init(processingEnv)
        this.processingEnv = processingEnv
        processingEnv?.messager?.printMessage(Diagnostic.Kind.WARNING, "init...!")
    }

    /**
     * 所支持的注解合集
     */
    override fun getSupportedAnnotationTypes(): MutableSet<String> {
        return mutableSetOf(EntroFrgName::class.java.canonicalName)
    }

    private fun isElementInAnnotations(target:Element, annotations: Set<TypeElement>) : Boolean {
        for (annotation in annotations) {
            //匹配注释
            if (target == annotation) {
                return true
            }
        }
        return false
    }

    //Element代表程序中的包名、类、方法。即注解所支持的作用类型。
    fun getMyElements(annotations: Set<TypeElement>, elements: Set<Element?>): Set<TypeElement> {
        val result: MutableSet<TypeElement> = HashSet()
        //遍历包含的 package class method
        for (element in elements) {
            //匹配 class or interface
            if (element is TypeElement) {
                for (annotationMirror in element.annotationMirrors) {
                    val found = isElementInAnnotations(annotationMirror.annotationType.asElement(), annotations)
                    if (found) {
                        result.add(element)
                        break
                    }
                }
            }
        }
        return result
    }

    /**
     * @param annotations 需要处理的注解 即getSupportedAnnotationTypes被系统解析得到的注解
     * @param roundEnv 注解处理器所需的环境，帮助进行解析注解。
     */
    override fun process(annotations: MutableSet<out TypeElement>?, roundEnv: RoundEnvironment?): Boolean {
        val elements = roundEnv?.rootElements?.let {
            if (annotations != null) {
                getMyElements(annotations, it)
            } else {
                null
            }
        }

        val names = AllEntroFragmentNamesTemplate()
        if (!elements.isNullOrEmpty()) {
            for (e in elements) {
                names.insert(e.qualifiedName.toString())
            }

            val code = names.end()
            processingEnv.filer?.let {
                try {
                    // 创建一个JavaFileObject来表示要生成的文件
                    val sourceFile: JavaFileObject = it.createSourceFile("com.allan.androidlearning.EntroList", null)
                    sourceFile.openWriter().use { writer ->
                        // 写入Java（或Kotlin）代码
                        writer.write(code)
                        writer.flush()
                    }
                } catch (e: IOException) {
                    processingEnv.messager.printMessage(Diagnostic.Kind.ERROR, "Failed to generate file: " + e.message)
                }
            }
        }

        return true
    }

    //一定要修改这里，避免无法生效
    override fun getSupportedSourceVersion(): SourceVersion {
        return SourceVersion.latestSupported()
    }
}