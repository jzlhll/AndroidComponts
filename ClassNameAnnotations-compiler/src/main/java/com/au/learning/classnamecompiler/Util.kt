package com.au.learning.classnamecompiler

import javax.lang.model.element.Element
import javax.lang.model.element.TypeElement

/**
 * @author allan
 * @date :2024/7/3 14:34
 *
 * Element：代表程序中的包名、类、方法。即注解所支持的作用类型。
 * getEnclosedElements()：获元素中的闭包的注解元素，比如MainActivity(TypeElement，Type代表Class)，
 *      而闭包的注解元素则为sName、sPhone、nameClick、phoneClick与onCreate。在这里简单的理解就是获取有注解的字段名、方法名。
 * getAnnotationMirrors()：获取上述闭包元素的所有注解。
 *      这里分别为sName与sPhone上的@BindeView、nameClick与phoneClick上的@OnClick、onCreate上的@Override。
 *
 */
class Util {
    companion object {
        //Element代表程序中的包名、类、方法。即注解所支持的作用类型。
        //
        private fun getTypeElementsByAnnotationType(annotations: Set<TypeElement>, elements: Set<Element?>): Set<TypeElement> {
            val result: MutableSet<TypeElement> = HashSet()
            //遍历包含的 package class method
            for (element in elements) {
                //匹配 class or interface
                if (element is TypeElement) {
                    var found = false
                    //遍历class中包含的 filed method constructors
                    for (subElement in element.getEnclosedElements()) {
                        //遍历element中包含的注释
                        for (annotationMirror in subElement.annotationMirrors) {
                            for (annotation in annotations) {
                                //匹配注释
                                if (annotationMirror.annotationType.asElement() == annotation) {
                                    result.add(element)
                                    found = true
                                    break
                                }
                            }
                            if (found) break
                        }
                        if (found) break
                    }
                }
            }
            return result
        }
    }
}