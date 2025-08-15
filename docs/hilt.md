todo:

WorkManager
Navigation
Paging3
Compose
retrofit

Arouter 已经过时无用。



## andorid Hilt快速易懂入门与原理浅析2025

https://developer.android.google.cn/training/dependency-injection?hl=zh-cn

https://blog.csdn.net/Jason_Lee155/article/details/116169709

https://blog.csdn.net/qq_26296197/article/details/146989195



### 简介

* **依赖注入**（Dependency Injection）

依赖注入(DI)是一种设计模式，也是实现控制反转(IoC)原则的一种方式。它通过"注入"依赖对象而不是在类内部创建它们，来解耦组件之间的依赖关系。

1. ‌**提高可测试性**‌：可以轻松替换模拟对象进行单元测试
2. ‌**降低耦合度**‌：类不直接创建依赖对象，只关注使用
3. ‌**提高代码复用**‌：依赖对象可以在多个类之间共享
4. ‌**简化配置**‌：通过框架可以集中管理依赖关系

Square公司，闻名遐迩，开发了okhttp,retrofit,leakcanary等优秀框架。他们开发了dragger1.0，通过反射做了依赖注入的功能。google接着开发了dargger2.0，通过编译时注解解决了初始化反射的耗时问题。但是复杂度太高，在android项目上有时候，用起来适得其反，把简单东西复杂化。

于是google又简化推出了Hilt。

* 简化依赖注入

  不需要手写复杂的` Dagger `代码，Hilt 提供了简单易懂的注解。

* 管理对象的生命周期

  Hilt 会根据不同的组件（如 `Activity、ViewModel、Application`）自动管理依赖对象的创建和销毁。

* 提高代码的模块化

  通过 Hilt 提供的 `@Module`，可以让代码更加清晰，方便维护和测试。

### 快速入门

#### 第0步：引入

2025.07的android studio和它的gradle版本。

根build.gradle：

```groovy
plugins {
		...
    id 'com.google.dagger.hilt.android' version '2.57' apply false
}
```

App build.gradle 引入（Hilt是一个编译时框架，因此需要kapt/ksp引入，这里推荐使用ksp，其他教程偏老）：

```groovy
plugins {
    id 'com.google.devtools.ksp'
    id 'dagger.hilt.android.plugin'
}
android {
	//hilt
  implementation("com.google.dagger:hilt-android:2.57")
  ksp("com.google.dagger:hilt-android-compiler:2.57")
}
```

#### 第1步：Application标注

必须找到自己的application（没有也要创建一个并在AndroidManifest中申明），给他添加上注解`@HiltAndroidApp`。

目的是在编译期间修改你的代码，让注入代码有了初始化的地方。

```kotlin
@HiltAndroidApp
class App : InitApplication() {
    override fun initBeforeAttachBaseContext() {
        DarkModeAnd...
    }

    override fun onCreate() {
        super.onCreate()
        debug...
    }
}
```

> 见：`实现原理浅析` `@HiltAndroidApp`部分。

#### 第2步：在生命周期类中最简单的使用

##### 2.1 生命周期类注入无参对象

如果如下这些类的子类，

```
Activity
Fragment 
View
Service
BroadcastReceiver
ViewModel（通过使用 @HiltViewModel）
```

> 注意没有content provider，因为它的生命周期初始化比Application更快。不符合使用。后面再学习怎么在contentProvider中使用。

必须完成如下三步，**任意缺少将会编译不通过，或者运行时报错**。

1️⃣使用注解`@AndoidEntryPoint`/ `@HiltViewModel`标注；

2️⃣在类中编写`@Inject`注入对象，必须是public；

3️⃣给注入类添加`@Inject`的构造函数。

```kotlin
@AndroidEntryPoint //1. 标注类
class EntryActivity : XXXActivity() {
    @Inject   //2. 申明注入对象import javax.inject.Inject
    lateinit var mHelper : EntryHelper
  
  	onCreate() {
       mHelper.test()
    }
}

class EntryHelper @Inject constructor() { //3. 申明注入。
    fun test() {
        logd { "test inject for test" }
    }
}
```

到这里我们实现了在生命周期类中注入了一个无参普通类。

**如果是Fragment，一定要让所有承载它的Activity都加上@AndroidEntryPoint。**



##### 2.2 生命周期类注入有参对象

```kotlin
package com.allan.androidlearning;

import javax.inject.Inject;

public class AnotherData {
    @Inject public AnotherData() {}
		//setter()/getter()
    private String data;
}

class EntryHelper @Inject constructor(private val data:AnotherData) { //这里允许private
    fun test() {
        data.data = "Another data 2025"
        logd { "test inject for test " + data.data }
    }
}
```

对于有参数的类`EntryHelper`需要注入一个`AnotherData`, 必须把它放在构造函数中，不能再是`lateinit var`。

也很简单对吧。

我们常常其实是需要把Activity传递给`EntryHelper`的。因此这么做：

##### 2.3 把生命周期类对象当做参数传递

* `@ActivityContext`

```kotlin
class EntryHelper @Inject constructor(val data:AnotherData,
                                      @ActivityContext private val context: Context
) {
    private val activity get() = context as EntryActivity

    fun test() {
        data.data = "Another data 2025"
        logd { "test inject for test " + data.data  + " " + activity}
    }
}
```

可以通过注解`@ActivityContext`即可引入Activity。我们在Activity中，仍然只需要保留如下就行：

```kotlin
@Inject
lateinit var mHelper : EntryHelper
```

* `@Provides`





回到我们最常见的一个场景，在Activity中添加一个ViewModel，我们都知道需要通过`ViewModelProvider(this).get(XXViewModel.class)`吧？

那么在Hilt下怎么做?





这里有几个问题，

第一，我不是在上述类中，使用怎么办？后续解答。

第二，注入类构造函数需要传参怎么办？后续解答。

第三，到底是怎么实现的？参考`实现原理浅析 注入编译结果追踪1`。

### 进阶用法

到了这里，就需要上些难度的东西。我也是现炒现卖。不做深入研究，遇到后，请参考别的帖子继续学习。



### 实现原理浅析

#### @HiltAndroidApp

添加了Application注解以后，编译后，反编译我们可以看到：

```kotlin
/* compiled from: App.kt */
@HiltAndroidApp
public final class App extends Hilt_App {
    @Override // com.au.module_android.InitApplication
    public void initBeforeAttachBaseContext() {
        DarkModeAn...
    }

    @Override // com.allan.androidlearning.Hilt_App, com.au.module_android.InitApplication, android.app.Application
    public void onCreate() {
        super.onCreate();
        debug...
    }
}

public abstract class Hilt_App extends InitApplication implements GeneratedComponentManagerHolder {
  private boolean injected = false;
    private final ApplicationComponentManager componentManager = new ApplicationComponentManager
  	...

    @Override // dagger.hilt.internal.GeneratedComponentManagerHolder
    public final ApplicationComponentManager componentManager() {
        return this.componentManager;
    }

    @Override // dagger.hilt.internal.GeneratedComponentManager
    public final Object generatedComponent() {
        return componentManager().generatedComponent();
    }

    @Override // com.au.module_android.InitApplication, android.app.Application
    public void onCreate() {
        hiltInternalInject();
        super.onCreate();
    }

    protected void hiltInternalInject() {
        if (!this.injected) {
            this.injected = true;
            ((App_GeneratedInjector) generatedComponent()).injectApp((App) UnsafeCasts.unsafeCast(this));
        }
    }
}
```

从这里，我们就可以看出，`HiltAndroidApp`注解的作用是把你的application进行了二次抽象，补充了实现了一个GeneratedComponentManagerHolder。具体细节不展开，其实就是收集了其他Hilt注解生成的代码做为注入的对象。

#### 注入编译结果追踪1

在编译路径：app/build/generated/ksp/下。可以找到相关的编译类。

```java
public abstract class Hilt_EntryActivity<VB extends ViewBinding> extends BindingActivity<VB> implements GeneratedComponentManagerHolder
```

你看我的activity类似Application那种做法，又给替换掉了。

```java
public abstract class Hilt_EntryActivity<VB extends ViewBinding> extends BindingActivity<VB> implements GeneratedComponentManagerHolder {
  private SavedStateHandleHolder savedStateHandleHolder;
  ...
}
```

结合编译和反编译来分析，简化来看，在替换掉的抽象类Hilt_EntryActivity中提供初始化以后立刻injectEntryActivity(this)的函数，这里其实写的很复杂，主要是前面的三步，还记得吗。

> 1️⃣使用注解`@AndoidEntryPoint`标注

对，就是实现了它。将整个Activity类包裹一层，注入。

然后，注入的实现在：

```java
// ./hilt/component_sources/debug/com/allan/androidlearning/DaggerApp_HiltComponents_SingletonC.java:361: 
EntryActivity_MembersInjector.injectMHelper(instance, new EntryHelper());  

public static void injectMHelper(EntryActivity instance, EntryHelper mHelper) {
  instance.mHelper = mHelper;                                                  
}                                                                              
```

EntryActivity_MembersInjector则就是实现了

> 2️⃣在类中编写`@Inject`注入对象。
>
> 将我们所有的注入变量，全部在这个生成类中注入。

```java
public abstract class Hilt_App extends InitApplication implements GeneratedComponentManagerHolder {                          private final ApplicationComponentManager componentManager = new ApplicationComponentManager(new ComponentSupplier() {   
    @Override
    public Object get() {     
      return DaggerApp_HiltComponents_SingletonC.builder()
          .applicationContextModule(new ApplicationContextModule(Hilt_App.this)).build();
    }
  });                                                                                                         
  @Override
  public final ApplicationComponentManager componentManager() {
    return componentManager;
  }
```

`DaggerApp_HiltComponents_SingletonC`则是在Application里面进行了builder创建。

更多的实现细节，可以通过ActivityComponentManager， ApplicationComponentManager,  EntryPoints等Hilt的源码去分析。

这里就不再做深入的研究了，可以大胆合理的猜测，逻辑就是：

**Hilt通过二层抽象类替代，添加了创建类中的提供辅助类(xxxManager, xxxProvider, xxxSingleTonC)，在Application里面初始化成对象。并通过Class名会取得插入辅助类(xxxMembersInjector)来初始化你的变量。**

![截屏2025-07-27 22.12.18](/Users/allan/Desktop/截屏2025-07-27 22.12.18.png)