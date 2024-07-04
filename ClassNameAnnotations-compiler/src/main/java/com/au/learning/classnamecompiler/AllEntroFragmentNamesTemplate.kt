package com.au.learning.classnamecompiler

/**
 * @author allan
 * @date :2024/7/3 14:11
 * @description:
 */
class AllEntroFragmentNamesTemplate : AbsCodeTemplate() {
    private val insertCode = StringBuilder()

    /**
     * com.allan.androidlearning.activities.LiveDataFragment.class
     */
    fun insert(javaClass:String) {
        insertCode.append("list.add(").append(javaClass).append(".class);").appendLine()
    }

    fun end() : String {
        return codeTemplate.replace("//insert001", insertCode.toString())
    }

    override val codeTemplate = """
package com.allan.androidlearning;

import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.List;

public class EntroList {
    public List<Class<? extends Fragment>> getEntroList() {
        List<Class<? extends Fragment>> list = new ArrayList<>();
        //insert001
        return list;
    }
}
    """.trimIndent()
}