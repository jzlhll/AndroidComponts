package com.allan.androidlearning.transfer.benas

enum class MyDroidMode {
    None,
    Receiver,
    Send,
    Middle,
    Image,
    Video,
}

/**
 * 这是翻译给前端用的
 */
fun MyDroidMode.toCNName() : String {
    return when(this) {
        MyDroidMode.Receiver -> "发送给手机"
        MyDroidMode.Send -> "从手机接收"
        MyDroidMode.Middle -> "中转"
        MyDroidMode.None -> "无"
        MyDroidMode.Image -> "图片浏览"
        MyDroidMode.Video -> "视频点播"
    }
}