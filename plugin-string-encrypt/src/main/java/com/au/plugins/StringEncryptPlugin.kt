package com.au.plugins

import com.android.build.api.instrumentation.FramesComputationMode
import com.android.build.api.instrumentation.InstrumentationScope
import com.android.build.api.variant.AndroidComponentsExtension
import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * graph TD
 * 源码；javac/kotlinc编译；生成.class文件；常量内联优化；字节码处理阶段；dexing; 打包apk
 * 我们需要在常量内联优化之前。
 */
class StringEncryptPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        pluginPrintln("StringEncrypt plugin start...")

        // 注册字节码转换工厂
        val androidComponents: AndroidComponentsExtension<*, *, *>
            = target.extensions.getByType(AndroidComponentsExtension::class.java)

        androidComponents.onVariants { variant ->
            pluginPrintln("StringEncrypt plugin variant: ${variant.name}")
            val inst = variant.instrumentation
            inst.transformClassesWith(
                StringEncryptFactory::class.java,
                InstrumentationScope.PROJECT) { paramT->
                pluginPrintln("StringEncrypt plugin instrumentationParamsConfig $paramT")
            }

            //确保在优化前处理
            inst.setAsmFramesComputationMode(
                FramesComputationMode.COMPUTE_FRAMES_FOR_INSTRUMENTED_CLASSES
            )
        }
    }
}