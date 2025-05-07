package com.au.learning.classnamecompiler

/**
 * @author allan
 * @date :2024/7/3 14:11
 * @description:
 */
class AllEntryFragmentNamesTemplate : AbsCodeTemplate() {
    private val list = ArrayList<Pair<String, Int>>()
    private var autoEnterClass:String? = null

    /**
     * com.allan.androidlearning.activities.LiveDataFragment::class.java
     */
    fun insert(javaClass:String, priority: Int, customName:String?, autoEnter: Boolean) {
        if (customName.isNullOrEmpty()) {
            list.add("list.add(Triple($javaClass::class.java, $priority, null))" to priority)
        } else {
            list.add("list.add(Triple($javaClass::class.java, $priority, \"$customName\"))" to priority)
        }

        if (autoEnter) {
            autoEnterClass = "$javaClass::class.java"
        }
    }

    fun end() : String {
        val insertCode = StringBuilder()
        list.sortBy { -it.second }
        list.forEach {
            insertCode.append(it.first).appendLine()
        }
        return codeTemplate.replace("//insert001", insertCode.toString())
            .replace("//insert002", autoEnterClass ?: "null")
    }

    override val codeTemplate = """
package com.allan.androidlearning

import androidx.fragment.app.Fragment

class EntryList {
    fun getEntryList(): List<Triple<Class<out Fragment>, Int, String?>> {
        val list = ArrayList<Triple<Class<out Fragment>, Int, String?>>()
        //insert001
        return list
    }
    
    fun getAutoEnterClass() : Class<out Fragment>? {
        return //insert002
    }
}
    """.trimIndent()
}