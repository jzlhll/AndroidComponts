package com.au.module_android.utils

import android.os.Build
import android.util.Log
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsAnimationCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner

/**
 * 监听键盘高度变化，兼容安卓11以下机型
 * 安卓11以下，不支持控制键盘移动
 *
 * 对话框使用，需要传入activity的window对象，如果传入dialog的window对象，会导致监听无效
 *
 * [Android软键盘的监听与高度控制的几种方案及常用效果 - 掘金 (juejin.cn)](https://juejin.cn/post/7150453629021847566)
android11以上可以监听ime高度不断回调
android11一下：简单点则直接设置state_

STATE组和ADJUST可以共用。

[Android-软键盘一招搞定(原理篇) - 简书 (jianshu.com)](https://www.jianshu.com/p/996dd93f8a48)
#### SOFT_INPUT_STATE_UNSPECIFIED,

#### SOFT_INPUT_STATE_UNCHANGED,
​			**当 Activity 转至前台时保留软键盘最后所处的任何状态，无论是可见还是隐藏。**

#### SOFT_INPUT_STATE_HIDDEN,

#### SOFT_INPUT_STATE_ALWAYS_HIDDEN,

​	**跳转到一个新页面而非返回到当前页面，stateHidden只有在向前跳转的时候才会去调用hideCurrentInputLocked方法隐藏输入法，而stateAlwaysHidden则没有这个判断，任何情况下窗口获取到焦点都会去隐藏输入法。**

目标是隐藏键盘。只是看看要不要跟随前面一个界面是否隐藏罢了。



#### SOFT_INPUT_STATE_VISIBLE,

#### SOFT_INPUT_STATE_ALWAYS_VISIBLE,

 **当 Activity 的主窗口有输入焦点时始终显示软键盘。**安卓9之后软键盘不能自动弹出了，
进入判断后会去判断输入法进程有没有开启，没有开启会去调用startInputUncheckedLocked开启，开启之后会去掉用showCurrentInputLocked显示软键盘。

#### SOFT_INPUT_ADJUST_UNSPECIFIED,

​	**默认的。**

#### SOFT_INPUT_ADJUST_RESIZE,

​	设置后影响了布局的高度，第一个Demo里ImageView高度是固定的，因此即使其父布局高度变小了也不会影响ImageView的展示。而第二个Demo里ImageView高度跟随父布局高度变化，因此当父布局高度变化时，ImageView也随着变化。

​     1、当设置SOFT_INPUT_ADJUST_RESIZE 时，DecorView的子布局padding会改变，最后影响子孙布局的高度。
2、父布局高度的变化并不一定会让子布局重新布局，因此针对上面的第一个Demo，我们需要监听键盘的变化从而调整输入框的位置。而对于上面的第二个Demo，不需要手动调整，父布局会自动调整。

 **API描述：**

不跟SOFT_INPUT_ADJUST_PAN一起使用。如果没有设置任何一个SOFT_INPUT_ADJUST_*，系统会选一个来适配；如果window是全屏的则不调整。

android11通过Call Window.setDecorFitsSystemWindows(boolean) with false and install an View.OnApplyWindowInsetsListener on your root content view that fits insets of type WindowInsets.Type.ime(). 来做。



#### SOFT_INPUT_ADJUST_PAN,

1、当设置SOFT_INPUT_ADJUST_PAN时，如果发现键盘遮住了当前有焦点的View，那么会对RootView(此处Demo里DecorView作为RootView)的Canvas进行平移，直至有焦点的View显示到可见区域为止。
2、这就是为什么点击输入框2的时候布局会整体向上移动的原因。

 **API描述：**不跟SOFT_INPUT_ADJUST_RESIZE 一起使用。如果没有设置任何一个SOFT_INPUT_ADJUST_*，系统会选一个来适配；如果window是全屏的则不调整。实践下来基本上是选择RESIZE或者PAN。



#### SOFT_INPUT_ADJUST_NOTHING,

不准调整布局。
 */

/**
 * 是否支持键盘动画，安卓11及以上才支持
 */
val supportImeAnim
    get() = Build.VERSION.SDK_INT >= 30

private const val DEBUG = false

//ViewCompat.getWindowInsetsController(this) 已经经常失效了。改成WindowCompat.getInsetsController(window, view)

fun hideImeNew(window: Window, view:View) {
    WindowCompat.getInsetsController(window, view).hide(WindowInsetsCompat.Type.ime())
}

/**
 * 当刚刚显示的时候，调用无效。
 * 需要postDelay一段时间才行。这个函数建议手动点击比如某些按钮，然后触发弹出。
 */
fun showImeNew(window: Window, view:EditText) {
    view.requestFocus()
    WindowCompat.getInsetsController(window, view).show(WindowInsetsCompat.Type.ime())
}

/**
 * 当显示的时候，直接显示
 */
fun showImeNewOnCreate(window:Window, et:EditText) {
    // 等价于xml中 android:windowSoftInputMode="stateVisible"
    //Visibility state for softInputMode:
    // please show the soft input area when normally appropriate (when the user is navigating forward to your window).
    //Applications that target Build.VERSION_CODES.P and later,
    // this flag is ignored unless there is a focused view
    // that returns true from View.onCheckIsTextEditor() when the window is focused.
    window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
    et.requestFocus()
}

/**
 * 键盘是否可见
 */
fun View.imeVisible(): Boolean? {
    return ViewCompat.getRootWindowInsets(this)?.isVisible(WindowInsetsCompat.Type.ime())
}