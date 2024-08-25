### 协程



### 常规使用

runBlocking{} 启动一个阻塞式的协程。一般不用，测试代码用； 忽略。

GlobalScope.launch{}，全局的协程，与app生命周期一致。容易内存泄漏，现不推荐；除非你知道你在干什么。

实现CoroutineScope + launch{} 生命周期的绑定。



### MainScope

cancel（）

就不能再工作了？TODO
