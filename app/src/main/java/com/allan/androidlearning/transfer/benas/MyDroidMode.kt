package com.allan.androidlearning.transfer.benas

enum class MyDroidMode {
    Receiver,
    Send,
    Middle
}

fun MyDroidMode.toCNName() : String {
    return when(this) {
        MyDroidMode.Receiver -> "发送给手机"
        MyDroidMode.Send -> "从手机接收"
        MyDroidMode.Middle -> "中转"
    }
}