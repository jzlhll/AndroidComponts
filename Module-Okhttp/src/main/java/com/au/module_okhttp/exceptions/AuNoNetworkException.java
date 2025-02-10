package com.au.module_okhttp.exceptions;

import androidx.annotation.Keep;

/**
 * Description 定义了一个没有网络的Exception
 */
@Keep
public final class AuNoNetworkException extends Exception {
    public AuNoNetworkException() {
        super("Network not available!");
    }

    public AuNoNetworkException(String s) {
        super(s);
    }
}