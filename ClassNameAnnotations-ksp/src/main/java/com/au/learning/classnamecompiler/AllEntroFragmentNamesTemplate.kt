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
    fun insert(javaClass:String, priority: Int, customName:String?) {
        if (customName.isNullOrEmpty()) {
            list.add("list.add(Triple($javaClass::class.java, $priority, null))" to priority)
        } else {
            list.add("list.add(Triple($javaClass::class.java, $priority, \"$customName\"))" to priority)
        }
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
    fun getEntroList(): List<Triple<Class<out Fragment>, Int, String?>> {
        val list = ArrayList<Triple<Class<out Fragment>, Int, String?>>()
        //insert001
        return list
    }
}
    """.trimIndent()
}