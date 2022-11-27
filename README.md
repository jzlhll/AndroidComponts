# Android常用自定义View，组件和自用框架
### [recyclerView](./docs/recylerView.md)

需要单独的一个文档来介绍它的方方面面

### [systemViews](./docs/systemViews.md)

* 
  * PaddingItemDecoration 用于添加item之间的space，和最上面的padding和最下面的padding
* CoordinatorLayout&AppBarLayout
* CoordinatorLayout&CollapsingToolbarLayout
* CoordinatorLayout&Behavior
* GestureDetector接管onTouch事件处理更多细节事件 & OverScroller
* CircularProgressIndicator & BaseProgressIndicator


###  [自定义](./docs/customViews.md)

* DrawableTextView

  > 可以定制图标大小的TextView，因为标准的AppCompatTextView只能设置位置，不能设置大小。实现此自定义View。

* SplashScreen:  todo

* PaddingItemDecoration

  



### 1. NestedScrolling接口

学习如何使用这个接口去实现嵌套约束。

父View需要实现 NestedScrollingParent 接口,而子View需要实现 NestedScrollingChild 接口。

todo 今天研究下。

[Material Design之NestedScrolling嵌套滑动机制源码分析_独饮敌敌畏丶的博客-CSDN博客](https://blog.csdn.net/afdafvdaa/article/details/115600602)

事件从Activity传来，传给ViewGroup，如果onInterceptTouchEvent返回true则说明ViewGroup对事件进行拦截，则走ViewGroup的onTouchEvent方法，如果返回false则说明不拦截，则调用子View的DispatchTouchEvent，然后调用onTouchEvent进行事件处理。如果处理了则返回true，如果不处理则返回false，然后再回到ViewGroup，如此循环往复。

流程顺序
事件的分发流程是Activity-ViewGroup-View。消费流程正好反过来，是View-ViewGroup-Activity。

事件序列
down ->   一系列的move  ->  up/cancel
如果View的down事件没有消费，那么后续的move事件是没办法接收到的。

> **为了解决传统事件滑动机制的bug。**
> 我们滑动的是子View的内容区域，而移动却是外部的ViewGroup，所以按照传统的方式，肯定是外部的Parent拦截了内部的Child的事件；但是，如果要实现Parent滑动到一定程度时，Child又开始滑动，中间整个过程是没有间断的这样的效果，从正常的事件分发（传统机制）角度去做是不可能的，因为当Parent拦截之后，是没有办法再把事件交给Child的，事件分发，对于拦截，相当于一锤子买卖，只要拦截了，当前手势接下来的事件都会交给Parent(拦截者)来处理。
> 所以NestedScrolling机制就应运而生，完美的解决了这个问题。



初始阶段，预滚阶段，滚动阶段，结束阶段。





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

