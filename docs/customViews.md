### DrawableTextView

> 可以定制图标大小的TextView，因为标准的AppCompatTextView只能设置位置，不能设置大小。实现此自定义View。

androidx自带的CompatAPPTextView，只能设置

```xml
android:drawableStart="@drawable/ic_launcher_foreground"
android:drawablePadding="20dp"
```

通过自定义attrs，自定义DrawableTextView实现。

```xml
app:drawableStartHeight="23dp"
app:drawableStartWidth="25dp"
```

参考代码为：

`DrawableTextView.kt`  `test_drawable_textview.xml` `themes.xml `   `declare-styleable name="DrawableTextView"`





