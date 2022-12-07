### ValueAnimator





### ProgressIndicator

CircularProgressIndicator & BaseProgressIndicator & LinearProgressIndicator

圆形和线性的进度条：

setIndeterminate 设置模糊模式。
app:indicatorColor="#1c64f2" 设置进度的指示色




[CoordinatorLayout系列（二）AppBarLayout_whoami_I的博客-CSDN博客](https://blog.csdn.net/whoami_I/article/details/103220104/)



### CoordinatorLayout&Behavior



通过实现Behavior接口，跟随dependency的动作进行变化。todo

### CoordinatorLayout&AppBarLayout

#### AppBarLayout

是一个线性布局

##### layout_scrollFlags

可以在

1. 不添加，就是noscroll 完全不动
2. "scroll"：RecyclerView向上滚动的时候，标记了的View也跟着滚动，等到标记了的View，滚出屏幕之后RecyclerView还在继续滚动，下拉的时候等到拉到RecyclerView的头部，标记了的View才会进入屏幕。可以多个view都添加。
3. "scroll:enterAlways": 只要往下拉，TabLayout就会显示出来，不必等着RecyclerView到顶部才可以显示。效果不太是预期。
4. 





[CoordinatorLayout+AppBarLayout顶部栏吸顶效果_Jason_Lee155的博客-CSDN博客_coordinatorlayout+appbarlayout](https://blog.csdn.net/Jason_Lee155/article/details/117233906)

### GestureDetector

https://zhuanlan.zhihu.com/p/144335238
https://blog.csdn.net/chennai1101/article/details/88055705

>  **接管View的点击事件。提供更细的动作回调。**

触摸事件监听有2种：

Activity层:

```java
@Override
public boolean onTouchEvent(MotionEvent event) {
    return super.onTouchEvent(event);
}
```

View层：设置View.OnTouchListener进去：

```java
@Override
public boolean onTouch(View v, MotionEvent event) {
    return false;
}
```

#### 内容

因为自行实现需要处理onTouch的MotionEvent的各种down，up，来进行大量的自行解析出各类动作，过于复杂。所以提供`GestureDector`类来帮你托管实现。

##### `GestureDector`类需要注意以下内容：

- `OnGestureListener`接口
- `OnDoubleTapListener`双击接口
- `SimpleOnGestureListener` 类（实现了类内的三个接口）
- `onTouchEvent()`方法
- `setOnDoubleTapListener()`双击监听

当屏幕上发生特定的触摸事件，就会去回调`GestureDector.OnGestureListener`和`GestureDetector.OnDoubleTapListener`接口中相应的回调函数来监测各样的手势和事件，并且通知用户。但是这些触发函数都没有具体的实现，我们必须实现这些接口，并重写这些方法的具体实现。

`GestureDetector`类中已经为我们定义了一个静态内部类`SimpleOnGestureListener`，它实现了`OnGestureListener`，`OnDoubleTapListener`，`OnContextClickListener`接口，定义为

```java
public static class SimpleOnGestureListener 
    implements OnGestureListener, OnDoubleTapListener, OnContextClickListener 
```

`SimpleOnGestureListener`类内重写接口中的所有方法，但是都是空实现，返回的布尔值都是`false`。主要作用是方便我们继承这个类有选择的复写回调方法，而不是实现接口去重写所有的方法。

`onTouchEvent()`方法用来分析传入的事件，如果匹配的话就去触发`OnGestureListener`中相应的回调方法。
如果要监听双击事件必须调用`GestureDector.setOnDoubleTapListener()`

##### GestureDector类的使用

第一步：实现接口

```java
class xxx implements GestureDetector.OnGestureListener
//或者
class xxxx extends SimpleOnGestureListener
```

第二步，创建对象并接管onTouch事件：

```java
val mGestureDetector = GestureDetector(Context context, OnGestureListener listener) //其他不建议

    
@Override
public boolean onTouch(View v, MotionEvent event) {
    return mGestureDetector.onTouchEvent(event); //也可以根据情况，接管onTouchEvent接口
}

//如果需要可以
mGestureDetector.setOnDoubleTapListener(xxx)
//context的实现
mGestureDetector.setContextClickListener(xxx)
```

快按屏幕:`onDown`
慢按屏幕：`onDown`–>`onShowPress`
按下屏幕等待一段时间`onDown`–>`onShowPress`–>`onLongPress`
拖动屏幕：`onDown`–>`onShowPress`–>`onScroll`(多个)–>`onFling`
快速滑动：`onDown`–>`onScroll`(多个)–>`onFling`

双击：`onDown`–>`onSingleTapUp`–>`onDoubleTap`–>`onDoubleTapEvent`–>`onDown`–>`onDoubleTapEvent`



>这时候我们会有两类疑问，这些相似的响应的方法是什么？他们和我们自带的那些onclick有什么区别？下边简单介绍一下
>
>1. `publicboolean onDown(MotionEvent e);`
>
>
>
>onDown是我们收到ACTION_DOWN时候就会被调用，一般来说，通过该事件处理我当前事件是否被消费了，然后返回的值通过detector.onTouchEvent(event)返回体现出来。
>
>1. `publicvoid onShowPress(MotionEvent e);`
>
>
>
>onShowPress是一个预按下的事件，在GestureDetector中是响应到ACTION_DOWN事件，100毫秒之后触发的。
>
>1. `publicboolean onSingleTapUp(MotionEvent e);`
>
>
>
>onSingleTapUp很好理解 每次点击抬起时调用。这里要注意，如果长按事件被执行那么onSingleTapUp就不执行。
>
>1. `publicboolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY);`
>
>
>
>onScroll 类似ACTION_MOVE手指发生移动之后调用，e1表示按下时候的时间，e2表示正在处理的事件，distanceX和distanceY表示移动距离。
>
>1. `publicvoid onLongPress(MotionEvent e);`
>
>
>
>onLongPress是长按时调用的默认事件是600毫秒。可以通过detector.setIsLongpressEnabled(false);将长按事件取消。
>
>1. `publicboolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY);`
>
>
>
>onFling是手指快速滑动时调用的，下边我会介绍。
>
>1. `publicboolean onDoubleTap(MotionEvent e);`
>2. `publicboolean onDoubleTapEvent(MotionEvent e);`
>
>
>
>onDoubleTap和onDoubleTapEvent都是双击点击事件，区别在于onDoubleTapEvent第二次点击可以滑动。



### **放大以及平移功能的实现**

### 放大功能实现

1. `@Override`
2. `public``boolean onDoubleTap(MotionEvent e)``{`
3. `isEnlarge =``!isEnlarge;`
4. `scaleAnimator =``ObjectAnimator.ofFloat(this,``"currentScale",``0);`
5. `if``(isEnlarge)``{`
6. `scaleAnimator.start();`
7. `}``else``{`
8. `scaleAnimator.reverse();`
9. `}`
10. `....`
11. `}`
12. `@Override`
13. `protected``void onDraw(Canvas canvas)``{`
14. `super.onDraw(canvas);`
15. `....`
16. `canvas.scale(currentScale, currentScale, getWidth()``/``2f, getHeight()``/``2f);`
17. `}`



onDoubleTap 我利用onDoubleTap双击事件 控制isEnlarge操作动画来实现图片的放大和缩小的效果。

接下来我们去看一下放到后如何进行移动的。

##### 平移功能实现

1. `@Override`
2. `protected``void onDraw(Canvas canvas)``{`
3. `super.onDraw(canvas);`
4. `....`
5. `canvas.translate(motionEventX * currentScale, motionEventY * currentScale);`
6. `....`
7. `}`



这个也很简单，通过canvas.translate 改变坐标系，然后随着手指对应图片的滑动的。那么motionEventX和motionEventY是如何计算出来的呢？这里 我们是通过GestureDetectorCompat中的onScroll这个方法计算出来的,为什么会有两个MotionEvent参数,下边简单叙述一下对应的参数的作用。

平滑过程的实现

1. `publicboolean onScroll(MotionEvent down, MotionEventevent, float distanceX, float distanceY);`



MotionEvent down 代表按下的事件，MotionEvent event代表当前的事件，distanceX代表上一个点到当前点的x轴距离，distanceY代表上一个点到当前点的Y轴距离。

1. `@Override`
2. `public``boolean onScroll(MotionEvent e1,``MotionEvent e2,``float distanceX,``float distanceY)``{`
3. `if``(isEnlarge)``{`
4. `motionEventX -= distanceX;`
5. `motionEventY -= distanceY;`
6. `....`
7. `invalidate();`
8. `}`
9. `return``false;`
10. `}`



上边是onScroll的具体实现，motionEventX和motionEventY是通过distanceX和distanceY计算出来的，但是这里为什么要用减法呢？手指向右移动图片，distanceX是正值，canvas.translate的坐标也要是负值图片才能要向右移动。同理distanceY也是如此。

这里要注意motionEventX和motionEventY的不要超过屏幕的边距。



##### **OverScroller实现快速滑动效果**

OverScroller及其滑动范围

运行了之后，我发现快滑的时候没有效果，这是因为我们没有对onFling进行处理，我们可以利用OverScroller中的scroller.fling方法进行计算，处理快速滑动的事件。

1. `private``OverScroller scroller;`
2. `@Override`
3. `public``boolean onFling(MotionEvent e1,``MotionEvent e2,``float velocityX,``float velocityY)``{`
4. `if``(isEnlarge)``{`
5. `scroller.fling((int) motionEventX,``(int) motionEventY,``(int) velocityX,``(int) velocityY,`
6. `-(int)``(bitmap.getWidth()``* bigScale - getWidth())``/``2,`
7. `(int)``(bitmap.getWidth()``* bigScale - getWidth())``/``2,`
8. `-(int)``(bitmap.getHeight()``* bigScale - getHeight())``/``2,`
9. `(int)``(bitmap.getHeight()``* bigScale - getHeight())``/``2);`
10. `postOnAnimation(motionRunnable);`
11. `}`
12. `return``false;`
13. `}`



scroller.fling的参数有点多 但是很好记。先看下八个参数是什么。

1. `publicvoid fling(int startX, int startY, int velocityX, int velocityY,`
2. `int minX, int maxX, int minY, int maxY);`



startX和startY代表初始的位置，在这个demo中就是我们手指点击的位置motionEventX,和motionEventY。velocityX和velocityY代表速度，手指抬起的时和之前的位移除以时间的计算，得出的结果。minX maxX minY maxY处理边距，这里以中心点为原点，放大后超过图片大小的部分除以2找到对应边界，如下图所示。



![img](https://pic4.zhimg.com/80/v2-87c93b394dc3191f7560f3081f94cfaf_720w.webp)


手指可滑动的范围是黑色框以外的部分。

上边说到scroller.fling是对于滑动方法的计算，但是我们需要对当前布局进行刷新。这里就用到了一个方法scroller.computeScrollOffset()判断是否完成滚动从而进行刷新。这里可以看一下postOnAnimation(motionRunnable);方法。

1. `MotionRunner motionRunnable =``new``MotionRunner();`
2. `....`
3. `postOnAnimation(motionRunnable);`



1. `class``MotionRunner``implements``Runnable``{`
2. `@Override`
3. `public``void run()``{`
4. `if``(scroller.computeScrollOffset())``{`
5. `motionEventX = scroller.getCurrX();`
6. `motionEventY = scroller.getCurrY();`
7. `invalidate();`
8. `postOnAnimation(this);`
9. `}`
10. `}`
11. `}`



这样我们每一帧动画执行完成后，不断调用postOnAnimation，直到scroller滚动完成。

post和postOnAnimation区别

这里postOnAnimation和post区别在于postOnAnimation在下一帧之后执行而post是立即在主线程中执行，有可能post执行要比postOnAnimation快，或者一帧中post会执行多次。这里使用ViewCompat.postOnAnimation兼容性更好。

### OverScroller和Scroller区别

在这个demo中我用的是OverScroller，但是他与Scroller有什么区别呢？简单说下overscrller会多两个参数

1. `publicvoid fling(int startX, int startY, int velocityX, int velocityY,`
2. `int minX, int maxX, int minY, int maxY, int overX, int overY);`



overX和overY代表超过范围的位置，在原来基础的距离上增加overx和overy。
