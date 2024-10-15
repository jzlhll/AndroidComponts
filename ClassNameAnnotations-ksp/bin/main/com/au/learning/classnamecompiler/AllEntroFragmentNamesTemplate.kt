package com.au.learning.classnamecompiler

/**
 * @author allan
 * @date :2024/7/3 14:11
 * @description:
 */
class AllEntryFragmentNamesTemplate : AbsCodeTemplate() {
    private val insertCode = StringBuilder()

    /**
     * com.allan.androidlearning.activities.LiveDataFragment::class.java
     */
    fun insert(javaClass:String) {
        insertCode.append("list.add(").append(javaClass).append("::class.java)").appendLine()
    }

    fun end() : String {
        return codeTemplate.replace("//insert001", insertCode.toString())
    }

    override val codeTemplate = """
package com.allan.androidlearning

import androidx.fragment.app.Fragment

class EntryList {
    fun getEntryList(): List<Class<out Fragment>> {
        val list = ArrayList<Class<out Fragment>>()
        //insert001
        return list
    }
}
    """.trimIndent()
}