package com.au.plugins

import com.android.build.api.variant.AndroidComponentsExtension
import org.gradle.api.Plugin
import org.gradle.api.Project

class PreProcessPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        // 注册字节码转换工厂
        val androidComponents: AndroidComponentsExtension<*, *, *>
                = target.extensions.getByType(AndroidComponentsExtension::class.java)

        androidComponents.onVariants { variant ->

        }
    }
}