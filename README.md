# Android常用自定义View，组件和自用框架
### [RecyclerView](./docs/recylerView.md)

需要单独的一个文档来介绍它的方方面面。

### [systemViews](./docs/systemViews.md)

* CoordinatorLayout&AppBarLayout
* CoordinatorLayout&CollapsingToolbarLayout
* CoordinatorLayout&Behavior
* GestureDetector接管onTouch事件处理更多细节事件 & OverScroller
* CircularProgressIndicator & BaseProgressIndicator
* ConstraintLayout，ImageView不支持layout_constraintBaseline_toBaselineOf



* ShapeableImageView

  [Android ShapeableImageView使用详解，告别shape、三方库_yechaoa的博客-CSDN博客_shapeableimageview](https://blog.csdn.net/yechaoa/article/details/117339632)


###  [自定义](./docs/customViews.md)

* DrawableTextView

  > 可以定制图标大小的TextView，因为标准的AppCompatTextView只能设置位置，不能设置大小。实现此自定义View。

* SplashScreen:  todo

* PaddingItemDecoration

  



[CoordinatorLayout详解二：_星月黎明的博客-CSDN博客](https://blog.csdn.net/qq_33209777/article/details/105141612)

**CoordinatorLayout与AppBarLayout**

**CoordinatorLayout与CollapsingToolbarLayout** 

**CoordinatorLayout与Behavior**

[Material Design之NestedScrolling嵌套滑动机制源码分析_独饮敌敌畏丶的博客-CSDN博客](https://blog.csdn.net/afdafvdaa/article/details/115600602)

```java
public interface NestedScrollingChild {
    /**
    * 启用或禁用嵌套滚动的方法，设置为true，并且当前界面的View的层次结构是支持嵌套滚动的
    * (也就是需要NestedScrollingParent嵌套NestedScrollingChild)，才会触发嵌套滚动。
    * 一般这个方法内部都是直接代理给NestedScrollingChildHelper的同名方法即可
    */
    void setNestedScrollingEnabled(boolean enabled);
    
    /**
    * 判断当前View是否支持嵌套滑动。一般也是直接代理给NestedScrollingChildHelper的同名方法即可
    */
    boolean isNestedScrollingEnabled();
    
    /**
    * 表示view开始滚动了,一般是在ACTION_DOWN中调用，如果返回true则表示父布局支持嵌套滚动。
    * 一般也是直接代理给NestedScrollingChildHelper的同名方法即可。这个时候正常情况会触发Parent的onStartNestedScroll()方法
    */
    boolean startNestedScroll(@ScrollAxis int axes);
    
    /**
    * 一般是在事件结束比如ACTION_UP或者ACTION_CANCLE中调用,告诉父布局滚动结束。一般也是直接代理给NestedScrollingChildHelper的同名方法即可
    */
    void stopNestedScroll();
    
    /**
    * 判断当前View是否有嵌套滑动的Parent。一般也是直接代理给NestedScrollingChildHelper的同名方法即可
    */
    boolean hasNestedScrollingParent();
    
    /**
    * 在当前View消费滚动距离之后。通过调用该方法，把剩下的滚动距离传给父布局。如果当前没有发生嵌套滚动，或者不支持嵌套滚动，调用该方法也没啥用。
    * 内部一般也是直接代理给NestedScrollingChildHelper的同名方法即可
    * dxConsumed：被当前View消费了的水平方向滑动距离
    * dyConsumed：被当前View消费了的垂直方向滑动距离
    * dxUnconsumed：未被消费的水平滑动距离
    * dyUnconsumed：未被消费的垂直滑动距离
    * offsetInWindow：输出可选参数。如果不是null，该方法完成返回时，
    * 会将该视图从该操作之前到该操作完成之后的本地视图坐标中的偏移量封装进该参数中，offsetInWindow[0]水平方向，offsetInWindow[1]垂直方向
    * @return true：表示滚动事件分发成功,fasle: 分发失败
    */
    boolean dispatchNestedScroll(int dxConsumed, int dyConsumed,
            int dxUnconsumed, int dyUnconsumed, @Nullable int[] offsetInWindow);
    
    /**
    * 在当前View消费滚动距离之前把滑动距离传给父布局。相当于把优先处理权交给Parent
    * 内部一般也是直接代理给NestedScrollingChildHelper的同名方法即可。
	* dx：当前水平方向滑动的距离
	* dy：当前垂直方向滑动的距离
	* consumed：输出参数，会将Parent消费掉的距离封装进该参数consumed[0]代表水平方向，consumed[1]代表垂直方向
	* @return true：代表Parent消费了滚动距离
    */
    boolean dispatchNestedPreScroll(int dx, int dy, @Nullable int[] consumed,
            @Nullable int[] offsetInWindow);
    
    /**
    *将惯性滑动的速度分发给Parent。内部一般也是直接代理给NestedScrollingChildHelper的同名方法即可
	* velocityX：表示水平滑动速度
	* velocityY：垂直滑动速度
	* consumed：true：表示当前View消费了滑动事件，否则传入false
	* @return true：表示Parent处理了滑动事件
	*/
    boolean dispatchNestedFling(float velocityX, float velocityY, boolean consumed);
    
    /**
    * 在当前View自己处理惯性滑动前，先将滑动事件分发给Parent,一般来说如果想自己处理惯性的滑动事件，
    * 就不应该调用该方法给Parent处理。如果给了Parent并且返回true，那表示Parent已经处理了，自己就不应该再做处理。
    * 返回false，代表Parent没有处理，但是不代表Parent后面就不用处理了
    * @return true：表示Parent处理了滑动事件
    */
    boolean dispatchNestedPreFling(float velocityX, float velocityY);
}
```
child2：type是用来区分事件类型的（touch和fling（惯性））
child3：consumed

```java
public interface NestedScrollingChild2 extends NestedScrollingChild {

    boolean startNestedScroll(@ScrollAxis int axes, @NestedScrollType int type);

    void stopNestedScroll(@NestedScrollType int type);

    boolean hasNestedScrollingParent(@NestedScrollType int type);

    boolean dispatchNestedScroll(int dxConsumed, int dyConsumed,
            int dxUnconsumed, int dyUnconsumed, @Nullable int[] offsetInWindow,
            @NestedScrollType int type);

    boolean dispatchNestedPreScroll(int dx, int dy, @Nullable int[] consumed,
            @Nullable int[] offsetInWindow, @NestedScrollType int type);
}

public interface NestedScrollingChild3 extends NestedScrollingChild2 {

    void dispatchNestedScroll(int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed,
            @Nullable int[] offsetInWindow, @ViewCompat.NestedScrollType int type,
            @NonNull int[] consumed);
}
```





### 2. CoordinatorLayout&AppBarLayout&CollapsingToolbarLayout

https://blog.csdn.net/gdutxiaoxu/article/details/52858598

https://www.jianshu.com/p/e8f14a1f16a3
https://www.jianshu.com/p/c6f67961285e


### 4. TabLayout小红点实现

### 5. ViewPager

跳转：[viewPagers](./docs/viewPagers.md)

### 8. IMEUtils

跳转：[IME](./docs/ime.md)

### 9. 2个UnitTest

### 10. WindowInsetsController/Compat
跳转：[windowCompat](./docs/windowCompat.md)

### 11. FragmentContainViewer



### 12. NavHostFragment

### 13. Notification
https://blog.csdn.net/yechaoa/article/details/125465158



### LifeCycle



### OnBackPressedCallback

getOnBackPressedDispatcher()

### DialogFragment

appCompactDialogFragment

BottomSheetDialog



### ToolBar

[Android：Toolbar的图标尺寸问题 - Endv - 博客园 (cnblogs.com)](https://www.cnblogs.com/endv/p/11312866.html)



### 数据库 Sqlite & LitePal & ROOM

数据库

### kotlin协程
[协程](./docs/kotlinScope.md)



### Fragment嵌套Fragment

返回层级；child,parentFragmentManager；FragmentContainerView

```
addToBackStack
```

```
setReorderingAllowed
```

FragmentContainerView注意事项：warning！！不能在Fragment里面使用重叠了

```
<!--  tools:parentTag="androidx.constraintlayout.widget.ConstraintLayout" --> 用来做merge的显示
```



style获取不到的问题



### TabLayout

[Android原生TabLayout使用全解析，看这篇就够了_yechaoa的博客-CSDN博客_android tablayout](https://blog.csdn.net/yechaoa/article/details/122270969)

### toolBar

overFlowButton，当图标过多的时候，右边的三个点。

[【Android】原来Toolbar还能这么用？Toolbar使用最全解析。网友：终于不用老是自定义标题栏啦_宾有为的博客-CSDN博客_toolbar使用](https://blog.csdn.net/baidu_41616022/article/details/117912975)

[activity  | Android 开发者  | Android Developers (google.cn)](https://developer.android.google.cn/jetpack/androidx/releases/activity?hl=zh-cn)
https://medium.com/tech-takeaways/how-to-migrate-the-deprecated-oncreateoptionsmenu-b59635d9fe10

```kotlin
toolbar?.let { it ->
    it.navigationIcon = null
    it.inflateMenu不生效了。无法工作。
}
```

```kotlin
override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    val menuHost = requireActivity()
    menuHost.addMenuProvider(object : MenuProvider {
        override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
            menuInflater.inflate(R.menu.close_menu, menu)
        }

        override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
            logd("todo impl")
            return true
        }
    }, viewLifecycleOwner, Lifecycle.State.RESUMED)

    super.onViewCreated(view, savedInstanceState)
}
```



### Handler

IdleHandler

同步消息，异步消息，屏障



### 小细节

* 子类必须调用父类。@CallSuper注解。

* 能使用recyclerView自身解决就不要嵌套ScrollView。

* kontlin被class by实现代理的类，函数，是不会走子类的。

* TextView gravity不生效，因为设置了width=wrap，就被居中了，导致无法设置

* java的bean：使用@Nullable注解，来标注，这样kotlin就知道他是？类型了。

* 即使Kotlin申明了非空类型，而Gson照样赋空。

* java初始化流程：

  > 最基类构造函数->子类构造函数->...->你的类构造函数
* Koltin初始化流程：
  
  > 最基类init()函数, 然后构造函数-> 子类init()函数，然后构造函数-> 你的类init函数，然后构造函数

​	   总结下来就是，init{}只是将代码执行在构造函数的super之后，构造代码之后。

* Kotlin init{}， 申明对象和构造函数的顺序：

```kotlin
constructor() {
    //number = 3 //构造函数永远最后。
}

var number:Int = 1 //申明的变量和init看谁在后面，就以谁为准
init {
    number = 2
}
```

init{}和申明变量，看谁放在后面以谁为准。

如果构造函数有赋值，则以构造函数为准。因为他最晚。

另外，如果是父类里面的变量则是以子类为准。

> 父类申明变量，然后构造函数 -> 子类 init{}或申明，然后构造函数->....




### Kotlin Scope

* GlobalScope: 全进程
* MainScope：Activity

### Shadow
https://inloop.github.io/shadow4android/

