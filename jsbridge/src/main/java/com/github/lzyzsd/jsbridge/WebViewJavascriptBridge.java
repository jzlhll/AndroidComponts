package com.github.lzyzsd.jsbridge;


import androidx.annotation.NonNull;

public interface WebViewJavascriptBridge {

    public void send(String data);
    public void send(String data, CallBackFunction responseCallback);
    public void sendPage(String data, @NonNull String pageMask, CallBackFunction responseCallback);
//
//    /**
//     * 发送前面的不需要response.pageIndex从1开始
//     */
//    public void sendPageNotLast(String data, int pageTotal, int pageIndex, String pageMask);
//    /**
//     * 发送最后一个需要response
//     */
//    public void sendPageLast(String data, int pageTotal, String pageMask, CallBackFunction responseCallback);
}