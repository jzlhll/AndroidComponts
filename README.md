## androidComponts
大量android的基础框架架构，根据自己负责的公司项目，逐步形成的个人开发脚手架。
基于各个Module等基础模块可以快速开始大型项目开发。相信能在本工程中找到你感兴趣的点。

大致介绍框架：

### Module-Android

#### MVVM框架

基于activity + Fragment + 泛型 + `viewBinding` + kotlin实现的一个快速开发的框架。

涵盖了activity，fragment，dialog等支持。

兼容android15+沉浸式，从框架上进行约束开发。

#### 大量基础Widget

大量基础类。主要涉及文本封装类。自定义字体的需求。

`BgBuildLayout`，是实现了通过xml标签来设定`background`，`cornerRadius`等属性。这样就不用提供图片背景就实现背景。

附带可拷走的类：`ViewBackgroundBuilder`，通过链式调用创建一个背景。

#### `init`框架

* 兼容`android12+`的`SplashScreen`抽象类。参考文章：https://blog.csdn.net/jzlhll123/article/details/136628746

* `Globals`单例，持有大量全局需要使用的变量，比如：

  ```kotlin
  mainScope 
  mainHandler
  gson
  activityList //记录app的activity列表
  ```

* `InitApplication` 基类，用于提供给app主模块接入Application。

​	初始化包括：自主黑白主题，自选语言框架，异常拦截框架，全局activity监听，前后台切换，screenAdapter适配，头条SPHook等。

#### 日志框架

`logd{}` ` loge{} `等打印需求，打印出线程和类名。

`FileLog`可以作为本地日志缓存框架。单文件实现简约不简单。

#### font & icon Font

`font`: 参考`CustomFontText`，`TextView.checkBoldAndSetFont`实现的加载特定的字体的font。做成基础封装类，便于公司快速开发使用。

`iconFont`: 有没有想过怎么将一些小图标变成字体库ttf，来实现当做文字显示？`IconFontUtil`来帮你。

#### Uncaught Exception

拦截未做处理导致的app crash。activity的实现，做到99%永不crash。参考module_android/crash。

> 原理:
>
> ```
> Thread.setDefaultUncaughtExceptionHandler(UncaughtExceptionHandlerObj)
> //主线程异常拦截
> try {
>     Looper.loop()
> } catch (e: Throwable) {
>     ...
> }
> ```
> 通过自行追加Looper.loop()的方式来解决。网上会有很多帖子介绍该方案。

#### 小数据KV缓存property

* `AppDataStore`
* `SharedPreference`
* `MmkvUtil` （目前逐步取消依赖，注释掉，现阶段推荐基于`kotlin`协程+`DataStore`✨）

另外提供类似变量的property代理`AppDataStoreXXXCache`和`SharedPrefXXXCache`，示例：

```kotlin
 private var lastCheckAndStartSystemTs by AppDataStoreLongCache("lastCheckAndStartSystemTs", System.currentTimeMillis())

//...
lastCheckAndStartSystemTs = curTs
```

#### Permissions框架

对于拍照，拍视频，申请权限，和启动activity for Result等代码kotlin框架实现。

参考module_android/permission。

#### 非粘性LiveData和状态

实现了非粘性的LiveData分装`NoStickLiveData`。

 实现原理参考我的帖子：https://blog.csdn.net/jzlhll123/article/details/133725302

#### StateFlow

StateFlow的最佳开发实践。

参考：`simpleflow`和`FlowStudyFragment`。

#### `ImeUtil/ImeHelper`

键盘弹起的回调监听。总所周知，android上通过xml布局的属性设定的效果比较差。`View.onApplyWindowInsets, setOnApplyWindowInsetsListener`等函数提供了更可靠和兼容更强的方案。

#### 黑夜模式&多语言&自选

实现黑夜模式和自主选择。

实现自主选择当前语言环境。

参考:`AndroidUi2Fragment`

#### 工具集

* `协程函数`：常用的扩展。`launchOnUi`， `launchOnIOThread`等。

* `onClick`: 防抖；

* `MediaHelper`: 对于音视频文件的信息获取，比如时长，文件类型等；

* `Uri工具类`：android对于Uri的依赖越来越高，比如从picker拿到的图片、视频，都是uri。进行裁剪和压缩再上传等操作都依赖于Uri的解析。拷贝等操作。

* Glide简易封装使用，设置任意参数，比如File\Uri\String链接等，还可以附带默认显示的图片或颜色。支持简易的时间缓存过期，避免同链接图片永不替换新图的问题解决。参考`GlideUtil`。

* `JSON/Bundle`扩展。kotlin扩展，辅助解析类。使用gson，解析json string。参考`JsonUtils`, `BundleUtil`。

* `Html text`显示封装。参考`TextView.useSimpleHtmlText`扩展。
* 通知封装库。
* 反射类。`ReflectionUtils`。
* 状态栏沉浸式研究和总结最佳方案。`SystemBarUtil`。参考`Activity.transparentStatusBar`。

### Module-AndroidColor

基于View项目，做的黑白模式兼容。

涉及了主题（activity/application的主题），背景色，

颜色盘涵盖了所有android开发涉及的色值，

style(多种常用文案的style，比如标题，二级文字等，输入框，Tab...）。

### Module-AndroidUi

类似IOS的toast风格实现。

各种常用Dialog显示，底部的，居中的。

其他控件。

### Module-Nested

实现了基于recyclerView的大量框架。

下拉刷新，自动触底加载分页的框架。

ViewPager2的框架。

提供组合RecyclerView，indicator等的Layout。



### Module-Okhttp

参考`OkhttpGlobal`。

接入该模块，初始化常用的okhttpClient，可以额外定制参数。并加入了自行研发的拦截器，用于控制请求参数的变更，比如token的刷新，后台错误的拦截，时间戳修正，简化okhttp标准重试拦截器。



#### Module-ImageCompressed

主要是集成`luban`压缩和`android picker photo`，以及基于`module-android`的permission请求和Uri处理。

还封装了弹窗选择拍照和选择图片，一直到请求权限，选择回来。



### Module-Native

加密相关。

保护appId/AppKey。涉及到ndk读取sha1签名校验，移位算法，AES加密，字符串混淆还原等代码。



### Module-AndroidLogSystem

一个日志系统。

基于单文件的自主实现FileLog。比较简化。



### Gradle脚本

字符串混淆脚本。

蒲公英测试上传脚本。

assets文件混淆方案脚本。

内网下载文件脚本。方便减少集成仓库的大小。



### 其他
几个主工程的实现：

学习自定义编译时注解的方案。

基于之前的jsbridge2.0实现的学习。webView的通信。

学习ROOM数据库。

nanoHttpD实现局域网通信。

