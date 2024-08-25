package com.au.jobstudy.checkwith.parent

import android.app.Activity
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.TextView
import com.au.module_android.click.onClick
import com.au.module_android.utils.setMaxLength
import com.au.module_android.utils.showImeNew

/**
 * @author allan
 * @date :2024/7/16 19:23
 * @description:
 */
class CodesManager(private val activity: Activity,
                   private val codeTextViewList:Array<TextView>,
                   private val hiddenEdit:EditText) {
    private val codeTextViewCount = codeTextViewList.size

    /**
     * 用于监听跳转。
     */
    var allEnterCodeListener:((String)->Unit)? = null

    @Volatile
    private var lastListenerTime = 0L

    private fun allEnterCodeListenerUpdate(str:String) {
        val cur = System.currentTimeMillis()
        if (lastListenerTime > cur - 3000L) { //later: 其实这样进行控制也存在一点小问题。因为可能输入后马上纠正，马上触发则不行。不过操作不太可能出现，暂时如此。
            return
        }
        lastListenerTime = cur
        allEnterCodeListener?.invoke(str)
    }

    init {
        hiddenEdit.inputType = EditorInfo.TYPE_CLASS_NUMBER
        hiddenEdit.setMaxLength(6)
        hiddenEdit.addTextChangedListener(CustomTextWatcher(this))
        hiddenEdit.setOnKeyListener { v, keyCode, event ->
            val hiddenEdit = v as EditText
            var r = false
            if (event.action == KeyEvent.ACTION_DOWN) {
                when (keyCode) {
                    KeyEvent.KEYCODE_DEL -> {
                        val curText = hiddenEdit.text.toString()
                        if (curText.isNotEmpty()) {
                            val cut1Len = curText.length - 1
                            hiddenEdit.setText(curText.substring(0, cut1Len))
                            hiddenEdit.setSelection(cut1Len)
                        }
                        r = true
                    }

                    KeyEvent.KEYCODE_ENTER -> {
                        if (isAllHasText()) {
                            allEnterCodeListenerUpdate(hiddenEdit.text.toString())
                        }
                        r = true
                    }
                }
            }

            r
        }

        val onTextViewClick = {
            showImeNew(activity.window, hiddenEdit)
        }

        codeTextViewList.forEach {
            it.onClick { onTextViewClick.invoke() }
        }
    }

    private fun isAllHasText():Boolean {
//        codeTextViewList.forEach {
//            val len = it.text?.length ?: 0
//            if (len == 0) {
//                return false
//            }
//        }
//        return true

        return hiddenEdit.text.length == codeTextViewCount
    }

    //更新所有的CodeTv的文字
    private fun updateTextList() {
        updateTextList(hiddenEdit.text.toString())
    }

    private fun updateTextList(words:String) {
        val charArray = words.toCharArray()
        val sz = charArray.size

        for (i in 0 until sz) {
            codeTextViewList[i].text = "${charArray[i]}"
        }

        if (sz < codeTextViewCount) {
            for (i in sz until codeTextViewCount) {
                codeTextViewList[i].text = ""
            }
        }
    }

    private class CustomTextWatcher(private val mgr: CodesManager) : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            mgr.updateTextList(s.toString())
            s?.let {
                if (it.length == mgr.codeTextViewCount) {
                    mgr.allEnterCodeListenerUpdate(it.toString())
                }
            }
        }

        override fun afterTextChanged(s: Editable?) {
        }

    }
}