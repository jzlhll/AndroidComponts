package com.au.learning.classnamecompiler

/**
 * @author allan
 * @date :2024/7/3 14:11
 * @description:
 */
class AllEntroFragmentNamesTemplate : AbsCodeTemplate() {
    private val list = ArrayList<Pair<String, Int>>()

    /**
     * com.allan.androidlearning.activities.LiveDataFragment::class.java
     */
    fun insert(javaClass:String, priority: Int) {
        list.add("list.add($javaClass::class.java to $priority)" to priority)
    }

    fun end() : String {
        val insertCode = StringBuilder()
        list.sortBy { -it.second }
        list.forEach {
            insertCode.append(it.first).appendLine()
        }
        return codeTemplate.replace("//insert001", insertCode.toString())
    }

    override val codeTemplate = """
package com.allan.androidlearning

import androidx.fragment.app.Fragment

class EntroList {
    fun getEntroList(): List<Pair<Class<out Fragment>, Int>> {
        val list = ArrayList<Pair<Class<out Fragment>, Int>>()
        //insert001
        return list
    }
}
    """.trimIndent()
}