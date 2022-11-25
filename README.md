# AndroidComponts
### [systemViews](./docs/systemViews.md)

* [recyclerView](./docs/recylerView.md)需要单独的一个文档来介绍它的方方面面
* CoordinatorLayout&AppBarLayout
* CoordinatorLayout&CollapsingToolbarLayout
* CoordinatorLayout&Behavior
* GestureDetector接管onTouch事件处理更多细节事件 & OverScroller
* CircularProgressIndicator & BaseProgressIndicator


###  [自定义View](./docs/customViews.md)

* DrawableTextView: 可以定制图标的大小的TextView，因为标准的AppCompatTextView只能设置位置不能设置大小。
* SplashScreen:  todo





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

