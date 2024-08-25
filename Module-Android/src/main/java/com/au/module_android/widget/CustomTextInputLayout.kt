package com.au.module_android.widget

import android.content.Context
import android.content.res.ColorStateList
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import com.au.module.android.R
import com.google.android.material.textfield.TextInputLayout
import java.util.regex.Pattern

/**
 * @author allan
 * TextInputLayout，android不支持outline模式的background颜色调整。
 * 因此实现。并且由于又有自定义的error样式。
 *
 * 如果需要使用三种board颜色，请务必style中的boxStrokeColor属性，并且引用为color.xml的state形式。
 * 更加复杂的可以考虑继续TextInputLayout内部的多个stroke颜色。
 */
open class CustomTextInputLayout : TextInputLayout {
    companion object {
        /**
         * 一种简单的密码要求检测函数：有字母，有数字，并且是6-16位。
         */
        fun matcherPassword(pass: String): Boolean {
            try {
                if (!TextUtils.isEmpty(pass)) {
                    val str = "^(?=.*[A-Za-z])(?=.*\\d).{6,16}$"
                    val pattern = Pattern.compile(str)
                    val matcher = pattern.matcher(pass)
                    return matcher.matches()
                }
                return false
            } catch (e: Exception) {
                return false
            }
        }

        /**
         * 邮箱检测函数。
         */
        fun matcherEmail(email: String): Boolean {
            try {
                if (TextUtils.isEmpty(email)) {
                    return false
                } else {
                    val splits = email.split("@".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                    if (splits.size != 2) {
                        return false
                    }

                    if (splits[0].isEmpty() || splits[0].length > 64 || splits[0].trim { it <= ' ' }.isEmpty()) {
                        return false
                    }
                    if (splits[1].isEmpty() || splits[1].length > 255 || splits[1].trim { it <= ' ' }.isEmpty()) {
                        return false
                    }

                    val emailPattern = "^.+@.+\\.[A-Za-z]+$"
                    val pattern = Pattern.compile(emailPattern)
                    val matcher = pattern.matcher(email)
                    return matcher.matches()
                }
            } catch (e: Exception) {
                return false
            }
        }
    }

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    /**
     * 外部变量：可以设置一个检查输入合法的函数进入。
     *
     * 可以使用CustomTextInputLayout.matcherEmail或者matcherPassword两个函数，检查email或者password的常规函数体。
     */
    var checkInputFun:((text:String)->Boolean) = {true}

    /**
     * 输入的内容是否合法
     */
    fun editCheckPattern() : Boolean {
        val m = mgr ?: return true
        return m.checkPattern()
    }

    //android 不支持透明度。设置boxBackgroundColor
    /**
     * 没被选中的颜色值
     */
    val defaultBoxBgColor : Int
        get() {
            return context.getColor(com.au.module_androidcolor.R.color.windowBackground)
        }

    /**
     * 被选中的颜色值
     */
    val focusedBoxBgColor : Int
        get() {
            return context.getColor(com.au.module_androidcolor.R.color.color_edit_background)
        }

    /**
     * 错误的背景色
     */
    val errorBoxBgColor : Int
        get() {
            return context.getColor(com.au.module_androidcolor.R.color.color_edit_background_error)
        }

    /**
     * 错误的board颜色
     */
    val errorBoardStrokeColor:ColorStateList by lazy(LazyThreadSafetyMode.NONE) {
        createUsefulColorStateList(context.getColor(com.au.module_androidcolor.R.color.color_edit_board_error))
    }

    var xmlBoxStrokeColorList:ColorStateList? = null
        private set

    private var mgr: LayoutAndInputManager? = null

    /**
     * 当focus的状态变化，其实是自定义的状态变化。
     */
    var afterStateCallback:((state:Boolean)->Unit)? = null

    override fun setBoxStrokeColorStateList(boxStrokeColorStateList: ColorStateList) {
        super.setBoxStrokeColorStateList(boxStrokeColorStateList)
        //第一次进来的，肯定是xml中的
        if (xmlBoxStrokeColorList == null) {
            xmlBoxStrokeColorList = boxStrokeColorStateList
        }
    }

    override fun addView(child: View, index: Int, params: ViewGroup.LayoutParams) {
        super.addView(child, index, params)
        //TextInputLayout 是将EditText添加到一个额外的FragmentLayout中
        if (child is EditText) {
            mgr = LayoutAndInputManager(this, child)
        }
    }

    private fun createUsefulColorStateList(color:Int) : ColorStateList {
        val states = arrayOf(
            //intArrayOf(-android.R.attr.state_enabled), //disabledColor
            //intArrayOf(android.R.attr.state_hovered, android.R.attr.state_enabled), //hoveredStrokeColor
            intArrayOf(android.R.attr.state_focused), //focusedStrokeColor , android.R.attr.state_enabled
            intArrayOf(), //default 空的default必须放在最后 isStateFul() 判断需要首个不为空。因此把别的放前面去
        )

        val colors = intArrayOf(
            color,
            //color,
           // color,
            color
        )
        return ColorStateList(states, colors)
    }

}

class LayoutAndInputManager(val inputLayout: CustomTextInputLayout,
                            val edit:EditText) {
    private var isGood = true

    fun checkPattern() : Boolean {
        return inputLayout.checkInputFun(edit.text.toString())
    }

    init {
        edit.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (DEBUG) Log.d("BaseTextInputLayout", "on InputChange Email")
                changeState(true)
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

        })

        edit.setOnFocusChangeListener { v, hasFocus ->
            val t = edit.text.toString()
            isGood = t.isBlank() || checkPattern()
            if(DEBUG) Log.d("BaseTextInputLayout", "lost focus isGood: $isGood")
            changeState()
        }
    }

    private var mState = 0

    private fun changeState(inputChange:Boolean = false) {
        val isFocused = inputLayout.isFocused || edit.isFocused
        val good:Boolean
        val newState = if (inputChange) { //如果是输入有变。不管如何都认为是正确的。只跟focus有关的效果。
            good = true
            if (isFocused) 1 else 0
        } else { //如果是focus变化，则校验所有的情况。并保证isGood提前告知。
            good = isGood
            if(isGood) {if (isFocused) 1 else 0} else 2
        }

        if (mState != newState) {
            mState = newState
            when (newState) {
                0-> { //not focus
                    inputLayout.boxBackgroundColor = inputLayout.defaultBoxBgColor
                    //如果想要使用这种转变特性；必须给xml中Style设置boxStrokeColor成可以选择的color.xml
                    inputLayout.xmlBoxStrokeColorList?.let { inputLayout.setBoxStrokeColorStateList(it) }
                }

                1-> { //focus
                    inputLayout.boxBackgroundColor = inputLayout.focusedBoxBgColor
                    //如果想要使用这种转变特性；必须给xml中Style设置boxStrokeColor成可以选择的color.xml
                    inputLayout.xmlBoxStrokeColorList?.let { inputLayout.setBoxStrokeColorStateList(it) }
                }

                2-> { //error
                    inputLayout.boxBackgroundColor = inputLayout.errorBoxBgColor
                    inputLayout.setBoxStrokeColorStateList(inputLayout.errorBoardStrokeColor)
                }
            }
        }

        inputLayout.afterStateCallback?.invoke(good)
    }

    companion object {
        const val DEBUG = true
    }
}