package com.github.lzyzsd.jsbridge;


import androidx.annotation.NonNull;

public interface WebViewJavascriptBridge {
    void send(String data);
    void send(String data, CallBackFunction responseCallback);
    void sendPage(String data, @NonNull String pageMask, CallBackFunction responseCallback);
}