package com.au.module_android.okhttp;

import androidx.annotation.Keep;

/**
 * @author allan.jiang
 * Date: 2023/6/27
 * Description 定义了一个没有网络的Exception
 */
@Keep
public final class NoNetworkException extends Exception {
    public NoNetworkException() {
        super("Network not available!");
    }

    public NoNetworkException(String s) {
        super(s);
    }
}